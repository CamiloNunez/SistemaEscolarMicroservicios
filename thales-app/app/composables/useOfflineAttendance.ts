export function useOfflineAttendance(service: 'academico' | 'matricula' = 'academico') {
  const auth = useAuthStore()
  const config = useRuntimeConfig()
  const base = String((service === 'matricula' ? config.public.matriculaApiBase : config.public.academicoApiBase)).replace(/\/+$/, '')
  const status = ref<{ rows: any[], history: any[] }>({ rows: [], history: [] })
  const online = ref(true)
  const cachedAt = ref('')
  const syncing = ref(false)
  const available = ref(false)
  const offlineError = ref('')
  async function rpc(type: string, payload: Record<string, any> = {}): Promise<any> {
    if (!navigator.serviceWorker?.controller) throw new Error('La PWA no está activa. Usa una compilación de producción, espera su instalación y recarga con conexión.')
    return new Promise((resolve, reject) => {
      const channel = new MessageChannel()
      const timer = setTimeout(() => { channel.port1.close(); reject(new Error('El Service Worker no respondió. Revisa la cola antes de volver a guardar.')) }, 60000)
      channel.port1.onmessage = ({ data }) => {
        clearTimeout(timer)
        channel.port1.close()
        data.error ? reject(new Error(data.error)) : resolve(data.result)
      }
      navigator.serviceWorker.controller!.postMessage({ type, base, ...payload }, [channel.port2])
    })
  }
  async function session() {
    await rpc('SESSION', { owner: auth.user?.id || auth.user?.username, token: auth.token, base })
  }
  async function refresh() {
    if (!available.value || !auth.token) { status.value = { rows: [], history: [] }; return }
    try { await session(); status.value = await rpc('STATUS') }
    catch (error: any) { offlineError.value = error.message }
  }
  async function sync() {
    if (!available.value || !auth.token || !navigator.onLine || syncing.value) return
    syncing.value = true
    offlineError.value = ''
    try { await session(); await rpc('SYNC'); await refresh() }
    catch (error: any) { offlineError.value = error.message }
    finally { syncing.value = false }
  }
  async function read() {
    await session()
    const snapshot = await rpc('SNAPSHOT_GET')
    if (!snapshot) throw new Error('No hay asistencias almacenadas. Carga el listado con conexión primero.')
    cachedAt.value = snapshot.savedAt
    return snapshot.rows
  }
  async function remember(rows: any[]) {
    if (!available.value) return
    try { await session(); await rpc('SNAPSHOT_SET', { rows }); cachedAt.value = '' }
    catch (error: any) { offlineError.value = `No se pudo guardar la copia offline: ${error.message}` }
  }
  async function enqueue(path: string, method: string, body?: string) {
    await session()
    await rpc('ENQUEUE', { path, method, body })
    await refresh()
    if (navigator.onLine) void sync()
  }
  async function discard(id: string) { await session(); await rpc('DISCARD', { id }); await refresh() }
  function changed() { void refresh() }
  function connectivity() { online.value = navigator.onLine; if (online.value) void sync() }
  function activated() { available.value = !!navigator.serviceWorker.controller; void refresh(); void sync() }
  let interval: ReturnType<typeof setInterval>
  onMounted(() => {
    online.value = navigator.onLine
    if (!('serviceWorker' in navigator)) return
    available.value = !!navigator.serviceWorker.controller
    navigator.serviceWorker.addEventListener('message', changed)
    navigator.serviceWorker.addEventListener('controllerchange', activated)
    window.addEventListener('online', connectivity)
    window.addEventListener('offline', connectivity)
    interval = setInterval(() => void sync(), 30000)
    void refresh(); void sync()
  })
  watch(() => auth.token, async () => {
    if (available.value) { await session(); await refresh(); void sync() }
  })
  onBeforeUnmount(() => {
    navigator.serviceWorker?.removeEventListener('message', changed)
    navigator.serviceWorker?.removeEventListener('controllerchange', activated)
    window.removeEventListener('online', connectivity)
    window.removeEventListener('offline', connectivity)
    clearInterval(interval)
  })
  return { status, online, cachedAt, syncing, available, offlineError, refresh, sync, read, remember, enqueue, discard }
}
