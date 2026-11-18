export default defineNuxtPlugin(() => {
  if (import.meta.dev || !('serviceWorker' in navigator)) return
  navigator.serviceWorker.register('/sw.js', { updateViaCache: 'none' }).catch(error => {
    console.error('No se pudo activar la PWA:', error)
  })
  const auth = useAuthStore()
  const config = useRuntimeConfig()
  function rpc(type: string, base: string, payload: Record<string, any> = {}) {
    return new Promise<void>((resolve, reject) => {
      const controller = navigator.serviceWorker.controller
      if (!controller) { resolve(); return }
      const channel = new MessageChannel()
      const timer = setTimeout(() => { channel.port1.close(); reject(new Error('Tiempo de sincronización agotado')) }, 60000)
      channel.port1.onmessage = ({ data }) => {
        clearTimeout(timer)
        channel.port1.close()
        data.error ? reject(new Error(data.error)) : resolve()
      }
      controller.postMessage({ type, base, ...payload }, [channel.port2])
    })
  }
  let running = false
  async function replay() {
    if (running) return
    running = true
    try {
      for (const value of [config.public.academicoApiBase, config.public.matriculaApiBase]) {
        const base = String(value || '').replace(/\/+$/, '')
        await rpc('SESSION', base, { owner: auth.user?.id || auth.user?.username, token: auth.token })
      }
      if (auth.token && navigator.onLine) await rpc('SYNC', String(config.public.matriculaApiBase || '').replace(/\/+$/, ''))
    } catch (error) { console.error('No se pudo sincronizar la cola:', error) }
    finally { running = false }
  }
  watch(() => [auth.token, auth.user?.id, auth.user?.username], replay, { immediate: true })
  navigator.serviceWorker.addEventListener('controllerchange', replay)
  window.addEventListener('online', replay)
  // A microservice can recover without an online event or a page reload.
  const timer = setInterval(replay, 15000)
  if (import.meta.hot) import.meta.hot.dispose(() => {
    clearInterval(timer)
    window.removeEventListener('online', replay)
    navigator.serviceWorker.removeEventListener('controllerchange', replay)
  })
})
