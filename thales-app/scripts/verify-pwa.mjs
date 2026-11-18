import vm from 'node:vm'
import assert from 'node:assert/strict'

const origin = process.env.PWA_TEST_ORIGIN || 'http://localhost:3000'
const response = await fetch(`${origin}/pwa-assets.js`)
assert.equal(response.status, 200)
const context = { self: {} }
vm.runInNewContext(await response.text(), context)
assert.ok(context.self.THALES_ASSETS.length > 3, 'Debe usar el manifiesto de producción')
for (const path of ['/sw.js', ...context.self.THALES_ASSETS]) {
  const asset = await fetch(origin + path)
  assert.equal(asset.status, 200, path)
  const bytes = await asset.arrayBuffer()
  assert.ok(bytes.byteLength > 0, path)
  if (path.startsWith('/dashboard/')) {
    assert.equal(new URL(asset.url).pathname, '/dashboard/academico', 'La precaché no debe almacenar una redirección al login')
    assert.match(new TextDecoder().decode(bytes), /__nuxt/)
  }
}
console.log(`PWA HTTP OK: Service Worker y ${context.self.THALES_ASSETS.length} recursos accesibles, incluyendo la página de asistencias.`)
