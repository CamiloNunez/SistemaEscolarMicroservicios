/* Thales PWA: persistent outbox and serial replay per account and service. */
importScripts('/pwa-assets.js')
const CACHE = `thales-shell-${self.THALES_BUILD}`
let sessions = new Map()
let running
const database = new Promise((resolve, reject) => {
  const request = indexedDB.open('thales-offline', 1)
  request.onupgradeneeded = () => {
    for (const name of ['snapshots', 'outbox', 'history']) request.result.createObjectStore(name, { keyPath: 'id' })
  }
  request.onsuccess = () => resolve(request.result)
  request.onerror = () => reject(request.error)
})
async function db(store, action, value) {
  const databaseInstance = await database
  return new Promise((resolve, reject) => {
    const tx = databaseInstance.transaction(store, action === 'get' || action === 'getAll' ? 'readonly' : 'readwrite')
    const request = tx.objectStore(store)[action](value)
    tx.oncomplete = () => resolve(request.result)
    tx.onerror = () => reject(tx.error)
    tx.onabort = () => reject(tx.error || new Error('Transacción cancelada'))
  })
}
async function notify() {
  for (const client of await self.clients.matchAll()) client.postMessage({ type: 'THALES_OFFLINE_CHANGED' })
}
self.addEventListener('install', event => {
  event.waitUntil(caches.open(CACHE).then(cache => cache.addAll(self.THALES_ASSETS)))
})
self.addEventListener('activate', event => {
  event.waitUntil((async () => {
    for (const key of await caches.keys()) if (key.startsWith('thales-shell-') && key !== CACHE) await caches.delete(key)
    await self.clients.claim()
  })())
})
self.addEventListener('fetch', event => {
  const url = new URL(event.request.url)
  if (event.request.method !== 'GET' || url.origin !== self.location.origin || url.pathname.startsWith('/api/')) return
  if (event.request.mode === 'navigate') {
    event.respondWith((async () => {
      const cache = await caches.open(CACHE)
      try {
        const response = await fetch(event.request)
        if (response.ok) await cache.put(event.request, response.clone())
        return response
      } catch {
        return await cache.match(event.request) || await cache.match('/offline.html')
      }
    })())
  } else if (self.THALES_ASSETS.includes(url.pathname)) {
    event.respondWith(caches.match(url.pathname).then(cached => cached || fetch(event.request)))
  }
})
function sessionKey(source, base) { return JSON.stringify([source.id, base]) }
async function sync() {
  if (running) return running
  running = (async () => {
    // Tokens live only in memory and are associated with a currently open client.
    const clients = await self.clients.matchAll()
    const active = new Set(clients.map(client => client.id))
    for (const id of sessions.keys()) if (!active.has(sessions.get(id).clientId)) sessions.delete(id)
    const rows = (await db('outbox', 'getAll')).sort((a, b) => a.createdAt - b.createdAt)
    const blockedOwners = new Set()
    let retryNeeded = false
    for (const row of rows) {
      const ownerKey = JSON.stringify([row.owner, row.base])
      const session = [...sessions.values()].find(s => s.owner === row.owner && s.base === row.base)
      if (blockedOwners.has(ownerKey) || !session) continue
      if (row.state === 'confirmed') {
        await db('history', 'put', row)
        await db('outbox', 'delete', row.id)
        continue
      }
      if (row.state === 'sending' && row.retryable) row.state = 'pending'
      if (row.state !== 'pending') { blockedOwners.add(ownerKey); continue }
      row.state = 'sending'
      await db('outbox', 'put', row)
      await notify()
      try {
        const response = await fetch(`${row.base}${row.path}`, {
          method: row.method,
          headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${session.token}` },
          body: row.body,
          signal: AbortSignal.timeout(15000),
        })
        if (!response.ok) {
          row.state = response.status === 401 || response.status === 403 ? 'auth'
            : row.retryable && [408, 429, 500, 502, 503, 504].includes(response.status) ? 'pending' : 'blocked'
          if (row.state === 'pending') retryNeeded = true
          row.error = `HTTP ${response.status}: ${ (await response.text()).slice(0, 200) }`
          await db('outbox', 'put', row)
          blockedOwners.add(ownerKey)
          continue
        }
        // Mark confirmed first: if the worker stops between transactions, never replay it.
        row.state = 'confirmed'
        row.syncedAt = new Date().toISOString()
        await db('outbox', 'put', row)
        await db('history', 'put', row)
        await db('outbox', 'delete', row.id)
      } catch {
        row.state = row.retryable ? 'pending' : 'uncertain'
        retryNeeded ||= row.retryable
        row.error = row.retryable ? 'Servicio no disponible. Se reintentara automaticamente.' : 'Envío interrumpido. Verifica el servidor antes de descartar y registrar nuevamente.'
        await db('outbox', 'put', row)
        blockedOwners.add(ownerKey)
      } finally { await notify() }
    }
    return !retryNeeded
  })().finally(() => { running = undefined })
  return running
}
self.addEventListener('sync', event => {
  if (event.tag === 'thales-asistencias') event.waitUntil(sync().then(done => { if (!done) throw new Error('Servicio no disponible') }))
})
async function command(data, source) {
  if (data.type === 'SESSION') {
    if (!data.base) {
      for (const [key, session] of sessions) if (session.clientId === source.id) sessions.delete(key)
      return true
    }
    const previous = sessions.get(sessionKey(source, data.base))
    sessions.delete(sessionKey(source, data.base))
    if (data.token && data.owner && /^https?:\/\//.test(data.base)) {
      sessions.set(sessionKey(source, data.base), { clientId: source.id, owner: data.owner, token: data.token, base: data.base })
      for (const row of await db('outbox', 'getAll')) {
        if (previous?.token !== data.token && row.owner === data.owner && row.base === data.base && row.state === 'auth') {
          row.state = 'pending'
          await db('outbox', 'put', row)
        }
      }
    }
    return true
  }
  const session = data.base ? sessions.get(sessionKey(source, data.base)) : [...sessions.values()].find(s => s.clientId === source.id)
  if (!session) throw new Error('Inicia sesión para usar el almacenamiento offline.')
  const belongs = row => row.owner === session.owner && row.base === session.base
  const snapshotId = JSON.stringify([session.owner, session.base])
  if (data.type === 'SNAPSHOT_GET') return await db('snapshots', 'get', snapshotId)
  if (data.type === 'SNAPSHOT_SET') return await db('snapshots', 'put', { id: snapshotId, rows: data.rows, savedAt: new Date().toISOString() })
  if (data.type === 'STATUS') {
    const rows = (await db('outbox', 'getAll')).filter(belongs)
    // A worker stopped mid-request cannot know if the server committed the write.
    if (!running) for (const row of rows) if (row.state === 'sending') {
      row.state = row.retryable ? 'pending' : 'uncertain'; row.error = 'El envío anterior se interrumpió; verifica el servidor.'
      await db('outbox', 'put', row)
    }
    return { rows, history: (await db('history', 'getAll')).filter(belongs).slice(-30) }
  }
  if (data.type === 'ENQUEUE') {
    if (!['POST', 'PUT', 'DELETE'].includes(data.method) || !/^\/api\/(?:asistencias|estudiantes|matriculas)(?:\/[^/?#]+)?$/.test(data.path)) throw new Error('Operación offline no permitida.')
    const row = { id: crypto.randomUUID(), owner: session.owner, base: session.base, path: data.path, method: data.method, body: data.body, createdAt: Date.now(), state: 'pending', retryable: /^\/api\/(?:estudiantes|matriculas)(?:\/|$)/.test(data.path) }
    await db('outbox', 'put', row)
    if (self.registration.sync) await self.registration.sync.register('thales-asistencias').catch(() => {})
    await notify()
    return row
  }
  if (data.type === 'DISCARD') {
    const row = await db('outbox', 'get', data.id)
    if (!row || !belongs(row) || row.state === 'sending') throw new Error('No se puede descartar esta operación.')
    await db('outbox', 'delete', row.id)
    await notify()
    return true
  }
  if (data.type === 'SYNC') { await sync(); return true }
  throw new Error('Comando desconocido')
}
self.addEventListener('message', event => {
  event.waitUntil((async () => {
    try { event.ports[0]?.postMessage({ result: await command(event.data, event.source) }) }
    catch (error) { event.ports[0]?.postMessage({ error: error.message }) }
  })())
})
