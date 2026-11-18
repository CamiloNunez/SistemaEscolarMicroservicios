import { readdir, writeFile } from 'node:fs/promises'
import { createHash } from 'node:crypto'
import { pathToFileURL } from 'node:url'
import { resolve, sep } from 'node:path'
export async function generatePwaAssets(publicDir) {
  const root = pathToFileURL(resolve(publicDir) + sep)
  async function walk(path = '_nuxt') {
    const entries = await readdir(new URL(path + '/', root), { withFileTypes: true })
    return (await Promise.all(entries.map(entry => entry.isDirectory() ? walk(`${path}/${entry.name}`) : `/${path}/${entry.name}`))).flat()
  }
  const assets = [...await walk(), '/offline.html', '/icon.svg', '/manifest.webmanifest', '/dashboard/academico', '/dashboard/academico?modulo=asistencias'].sort()
  const build = createHash('sha256').update(JSON.stringify(assets)).digest('hex').slice(0, 16)
  await writeFile(new URL('pwa-assets.js', root), `self.THALES_BUILD=${JSON.stringify(build)};\nself.THALES_ASSETS=${JSON.stringify(assets)};\n`)
  console.log(`PWA: ${assets.length} recursos incluidos en la precaché (${build}).`)
}
