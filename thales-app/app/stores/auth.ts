import { defineStore } from 'pinia'

export interface UserProfile {
  id: string
  usuarioId: string
  tipoUsuario: string
  entidadId: string
  codigoPerfil: string
  origen: string
  activo: boolean
}

export interface User {
  id: string
  username: string
  email: string
  nombreCompleto: string
  tipoUsuario: string
  roles: string[]
  grado: string | null
  seccion: string | null
  perfil: UserProfile | null
}

export interface AuthState {
  user: User | null
  token: string | null
  isAuthenticated: boolean
}

function normalizarUsuario(data: any, usernameFallback = ''): User {
  return {
    id: String(data?.id ?? ''),
    username: data?.username ?? usernameFallback,
    email: data?.email ?? '',
    nombreCompleto: data?.nombreCompleto ?? data?.nombre ?? usernameFallback,
    tipoUsuario: data?.tipoUsuario ?? '',
    roles: Array.isArray(data?.roles)
      ? data.roles.map((rol: any) => typeof rol === 'string' ? rol : (rol?.nombre ?? rol?.name ?? '')).filter(Boolean)
      : [],
    grado: data?.grado ?? null,
    seccion: data?.seccion ?? null,
    perfil: data?.perfil ?? null,
  }
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: null,
    token: null,
    isAuthenticated: false,
  }),

  getters: {
    getUser: (state) => state.user,
    getToken: (state) => state.token,
    isLogged: (state) => state.isAuthenticated,
    displayName: (state) => state.user?.nombreCompleto || state.user?.username || 'Usuario',
    displayRole: (state) => {
      const user = state.user
      if (!user) return 'Sin rol'
      if (user.tipoUsuario) return user.tipoUsuario
      return user.roles[0]?.replace(/^ROLE_/, '') || 'Sin rol'
    },
  },

  actions: {
    async login(username: string, password: string, loginApi: string) {
      const base = loginApi.trim().replace(/\/+$/, '')
      const response = await fetch(`${base}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      })

      const data = await response.json().catch(() => null)

      if (!response.ok) {
        throw new Error(data?.mensaje || data?.message || 'Credenciales inválidas')
      }

      const token = data?.token || ''
      if (!token) {
        throw new Error('El backend no devolvió un token JWT.')
      }

      this.token = token
      this.user = normalizarUsuario(data, username)
      this.isAuthenticated = true

      // /me es la fuente autoritativa para el usuario autenticado y su perfil funcional.
      try {
        await this.refreshCurrentUser(base)
      } catch {
        // El login ya devuelve los datos básicos y el perfil cuando existe.
      }

      this.persistSession()
    },

    async refreshCurrentUser(loginApi?: string) {
      if (!this.token) throw new Error('No hay una sesión autenticada.')

      const config = useRuntimeConfig()
      const base = String(loginApi || config.public.loginApiBase).trim().replace(/\/+$/, '')
      const response = await fetch(`${base}/api/auth/me`, {
        headers: {
          Accept: 'application/json',
          Authorization: `Bearer ${this.token}`,
        },
      })

      const data = await response.json().catch(() => null)

      if (!response.ok) {
        if (response.status === 401 || response.status === 403) this.logout()
        throw new Error(data?.mensaje || data?.message || `Error HTTP ${response.status}`)
      }

      this.user = normalizarUsuario(data, this.user?.username || '')
      this.isAuthenticated = true
      this.persistSession()
      return this.user
    },

    // Se conserva únicamente para el modo demo existente de la pantalla de login.
    loginDemo(username: string) {
      this.user = {
        id: 'DEMO',
        username,
        email: '',
        nombreCompleto: username === 'admin' ? 'Administrador' : username,
        tipoUsuario: username === 'admin' ? 'ADMINISTRATIVO' : 'PROFESOR',
        roles: [username === 'admin' ? 'ROLE_ADMIN' : 'ROLE_PROFESOR'],
        grado: null,
        seccion: null,
        perfil: null,
      }
      this.token = 'demo-token-' + Date.now()
      this.isAuthenticated = true
      this.persistSession()
    },

    restoreSession() {
      if (!import.meta.client) return

      const raw = localStorage.getItem('thales_user')
      const token = localStorage.getItem('thales_token')

      if (!raw || !token) return

      try {
        this.user = normalizarUsuario(JSON.parse(raw))
        this.token = token
        this.isAuthenticated = true
      } catch {
        this.logout()
      }
    },

    persistSession() {
      if (!import.meta.client || !this.user || !this.token) return
      localStorage.setItem('thales_user', JSON.stringify(this.user))
      localStorage.setItem('thales_token', this.token)
    },

    logout() {
      this.user = null
      this.token = null
      this.isAuthenticated = false

      if (import.meta.client) {
        localStorage.removeItem('thales_user')
        localStorage.removeItem('thales_token')
      }
    },
  },
})
