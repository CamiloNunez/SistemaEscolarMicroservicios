import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import vm from 'node:vm'
import { randomUUID } from 'node:crypto'

// Transaction-completion mock: tests the worker protocol and replay policy.
// Browser persistence and Cache Storage must additionally be verified in DevTools.
async function worker() {
  const stores = new Map()
  const listeners = {}
  const client = { id: 'tab-1', postMessage() {} }
  let response = { ok: true }
  let calls = 0
  const database = {
    createObjectStore(name) { stores.set(name, new Map()) },
    transaction(name) {
      const tx = { objectStore() { return new Proxy({}, { get: (_, action) => value => {
        const request = {}
        queueMicrotask(() => {
          const store = stores.get(name)
          if (action === 'put') { store.set(value.id, structuredClone(value)); request.result = value.id }
          if (action === 'delete') store.delete(value)
          if (action === 'get') request.result = structuredClone(store.get(value))
          if (action === 'getAll') request.result = structuredClone([...store.values()])
          tx.oncomplete()
        })
        return request
      } }) } }
      return tx
    },
  }
  const self = {
    THALES_BUILD: 'test', THALES_ASSETS: [], location: { origin: 'https://thales.test' },
    clients: { matchAll: async () => [client] }, registration: {},
    addEventListener(type, handler) { listeners[type] = handler },
  }
  const context = vm.createContext({
    self, importScripts() {}, indexedDB: { open() {
      const request = { result: database }
      queueMicrotask(() => { request.onupgradeneeded(); request.onsuccess() })
      return request
    } }, crypto: { randomUUID }, URL, Date, Map, Set, AbortSignal,
    fetch: async () => { calls++; if (response instanceof Error) throw response; return response },
  })
  vm.runInContext(await readFile(new URL('../public/sw.js', import.meta.url), 'utf8'), context)
  async function command(type, payload = {}) {
    let result
    await new Promise((resolve, reject) => listeners.message({
      source: client, data: { type, ...payload },
      ports: [{ postMessage(data) { result = data } }], waitUntil(promise) { promise.then(resolve, reject) },
    }))
    if (result.error) throw new Error(result.error)
    return result.result
  }
  const session = (owner = 'teacher-1', token = 'token') => command('SESSION', { owner, token, base: 'https://api.test' })
  await session()
  const enqueue = () => command('ENQUEUE', { path: '/api/asistencias', method: 'POST', body: '{"estado":"PRESENTE"}' })
  return { command, session, enqueue, stores, setResponse(value) { response = value }, calls: () => calls }
}
test('successful replay records evidence and removes the pending write', async () => {
  const w = await worker()
  await w.enqueue()
  assert.equal((await w.command('STATUS')).rows.length, 1)
  await Promise.all([w.command('SYNC'), w.command('SYNC')])
  const status = await w.command('STATUS')
  assert.equal(w.calls(), 1)
  assert.equal(status.rows.length, 0)
  assert.equal(status.history[0].state, 'confirmed')
  assert.ok(status.history[0].syncedAt)
})
test('uncertain delivery is retained and never automatically replayed', async () => {
  const w = await worker()
  w.setResponse(new TypeError('network interrupted'))
  await w.enqueue()
  await w.command('SYNC')
  await w.enqueue()
  await w.command('SYNC')
  assert.equal(w.calls(), 1)
  assert.equal((await w.command('STATUS')).rows[0].state, 'uncertain')
})
test('HTTP conflicts block the queue without losing data', async () => {
  const w = await worker()
  w.setResponse({ ok: false, status: 409, text: async () => 'conflict' })
  await w.enqueue()
  await w.enqueue()
  await w.command('SYNC')
  await w.command('SYNC')
  assert.equal(w.calls(), 1)
  assert.equal((await w.command('STATUS')).rows.length, 2)
  assert.equal((await w.command('STATUS')).rows[0].state, 'blocked')
})
test('snapshots and outbox are separated by account; logout denies access', async () => {
  const w = await worker()
  await w.command('SNAPSHOT_SET', { rows: [{ id: 1 }] })
  await w.enqueue()
  await w.session('teacher-2')
  assert.equal((await w.command('STATUS')).rows.length, 0)
  assert.equal(await w.command('SNAPSHOT_GET'), undefined)
  await w.command('SYNC')
  assert.equal(w.calls(), 0)
  await w.command('SESSION', {})
  await assert.rejects(w.command('STATUS'), /Inicia sesión/)
})
test('expired credentials require a new token before replay', async () => {
  const w = await worker()
  w.setResponse({ ok: false, status: 401, text: async () => 'expired' })
  await w.enqueue()
  await w.command('SYNC')
  await w.session()
  await w.command('SYNC')
  assert.equal(w.calls(), 1)
  w.setResponse({ ok: true })
  await w.session('teacher-1', 'renewed-token')
  await w.command('SYNC')
  assert.equal(w.calls(), 2)
  assert.equal((await w.command('STATUS')).rows.length, 0)
})
test('worker restart leaves in-flight writes uncertain, and rejects other modules', async () => {
  const w = await worker()
  const row = await w.enqueue()
  w.stores.get('outbox').get(row.id).state = 'sending'
  assert.equal((await w.command('STATUS')).rows[0].state, 'uncertain')
  await assert.rejects(w.command('ENQUEUE', { path: '/api/pagos', method: 'POST' }), /no permitida/)
})
test('a confirmed write interrupted during cleanup is archived without resending', async () => {
  const w = await worker()
  const row = await w.enqueue()
  Object.assign(w.stores.get('outbox').get(row.id), { state: 'confirmed', syncedAt: '2026-11-16T00:00:00Z' })
  await w.command('SYNC')
  assert.equal(w.calls(), 0)
  assert.equal((await w.command('STATUS')).rows.length, 0)
  assert.equal((await w.command('STATUS')).history.length, 1)
})


test('matricula service outage persists a student and retries when the service returns', async () => {
  const w = await worker()
  w.setResponse(new TypeError('connection refused'))
  const row = await w.command('ENQUEUE', { path: '/api/estudiantes', method: 'POST', body: '{"dni":"12345678"}' })
  await w.command('SYNC')
  assert.equal((await w.command('STATUS')).rows[0].state, 'pending')
  w.setResponse({ ok: true })
  await w.command('SYNC')
  const status = await w.command('STATUS')
  assert.equal(w.calls(), 2)
  assert.equal(status.rows.length, 0)
  assert.equal(status.history[0].id, row.id)
})

test('temporary HTTP failure retries matriculas; validation failures remain visible', async () => {
  for (const code of [408, 429, 500, 502, 503, 504, 400, 409, 422]) {
    const w = await worker()
    await w.command('ENQUEUE', { path: '/api/matriculas', method: 'POST', body: '{}' })
    w.setResponse({ ok: false, status: code, text: async () => 'error' })
    await w.command('SYNC')
    const retryable = [408, 429, 500, 502, 503, 504].includes(code)
    assert.equal((await w.command('STATUS')).rows[0].state, retryable ? 'pending' : 'blocked')
    w.setResponse({ ok: true })
    await w.command('SYNC')
    assert.equal((await w.command('STATUS')).rows.length, retryable ? 0 : 1)
  }
})

test('academic and matricula sessions coexist for the same browser client', async () => {
  const w = await worker()
  await w.enqueue()
  await w.command('SESSION', { base: 'https://matricula.test', owner: 'teacher-1', token: 'token' })
  await w.command('ENQUEUE', { base: 'https://matricula.test', path: '/api/estudiantes', method: 'POST', body: '{}' })
  assert.equal((await w.command('STATUS', { base: 'https://api.test' })).rows.length, 1)
  assert.equal((await w.command('STATUS', { base: 'https://matricula.test' })).rows.length, 1)
  await w.command('SYNC')
  assert.equal(w.calls(), 2)
})

test('interrupted matricula writes are recovered after worker restart', async () => {
  const w = await worker()
  const row = await w.command('ENQUEUE', { path: '/api/matriculas', method: 'POST', body: '{}' })
  w.stores.get('outbox').get(row.id).state = 'sending'
  await w.command('SYNC')
  assert.equal((await w.command('STATUS')).rows.length, 0)
  assert.equal(w.calls(), 1)
})
