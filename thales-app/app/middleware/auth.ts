export default defineNuxtRouteMiddleware((to) => {
  /* Rutas públicas que no requieren auth */
  const publicRoutes = ['/login']
  if (publicRoutes.includes(to.path)) return

  const authStore = useAuthStore()

  /* Restaurar sesión si es que existe */
  if (!authStore.isAuthenticated) {
    authStore.restoreSession()
  }

  /* Redirigir al login si no hay sesión */
  if (!authStore.isAuthenticated) {
    return navigateTo('/login')
  }
})