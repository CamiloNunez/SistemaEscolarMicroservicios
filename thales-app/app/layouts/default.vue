<template>
  <v-app class="app-shell">
    <v-navigation-drawer
      v-model="drawer"
      class="sidebar-nav"
      :rail="mdAndUp ? rail : false"
      :temporary="smAndDown"
      :permanent="mdAndUp"
      :touchless="mdAndUp"
      :width="300"
      :rail-width="84"
    >
      <div class="sidebar-top">
        <v-list-item class="sidebar-brand" @click="goHome">
          <template #prepend>
            <img
              src="~/assets/logo.png"
              alt="Logo"
              class="sidebar-logo"
            >
          </template>

          <div v-if="!rail || smAndDown" class="sidebar-brand-text">
            <v-list-item-title class="sidebar-brand-title">
              Thales de Mileto
            </v-list-item-title>

            <v-list-item-subtitle class="sidebar-brand-subtitle">
              Sistema Educativo
            </v-list-item-subtitle>
          </div>
        </v-list-item>

        <div v-if="!rail || smAndDown" class="sidebar-summary px-4 pb-2">
          <v-chip size="small" variant="tonal" color="primary" class="mr-2">
            Panel SaaS
          </v-chip>
        </div>
      </div>

      <v-divider />

      <v-list
        v-model:opened="openedGroups"
        density="compact"
        nav
        class="sidebar-menu"
      >
        <v-list-item
          to="/"
          prepend-icon="mdi-view-dashboard-outline"
          title="Dashboard"
          class="sidebar-item"
          active-class="sidebar-item--active"
        />

        <v-list-item
          to="/dashboard/usuarios"
          prepend-icon="mdi-account-group-outline"
          title="Usuarios"
          class="sidebar-item"
          active-class="sidebar-item--active"
        />

        <v-list-item
          to="/dashboard/roles"
          prepend-icon="mdi-shield-account-outline"
          title="Roles"
          class="sidebar-item"
          active-class="sidebar-item--active"
        />

        <v-list-group value="matricula">
          <template #activator="{ props }">
            <v-list-item
              v-bind="props"
              prepend-icon="mdi-school-outline"
              title="Matrícula"
              class="sidebar-item"
              :active="isMatriculaRoute"
              active-class="sidebar-item--active"
            />
          </template>

          <v-list-item
            v-for="item in matriculaItems"
            :key="item.modulo"
            :to="item.to"
            :prepend-icon="item.icon"
            :title="item.title"
            class="sidebar-item sidebar-subitem"
            :active="isModuloActivo(item.modulo)"
            active-class="sidebar-item--active"
          />
        </v-list-group>

        <v-list-group value="academico">
          <template #activator="{ props }">
            <v-list-item
              v-bind="props"
              prepend-icon="mdi-book-education-outline"
              title="Académico"
              class="sidebar-item"
              :active="isAcademicoRoute"
              active-class="sidebar-item--active"
            />
          </template>

          <v-list-item
            v-for="item in academicoItems"
            :key="item.modulo"
            :to="item.to"
            :prepend-icon="item.icon"
            :title="item.title"
            class="sidebar-item sidebar-subitem"
            :active="isAcademicoModuloActivo(item.modulo)"
            active-class="sidebar-item--active"
          />
        </v-list-group>

        <v-list-group value="pagos">
          <template #activator="{ props }">
            <v-list-item
              v-bind="props"
              prepend-icon="mdi-cash-multiple"
              title="Pagos"
              class="sidebar-item"
              :active="isPagosRoute"
              active-class="sidebar-item--active"
            />
          </template>

          <v-list-item
            v-for="item in pagosItems"
            :key="item.modulo"
            :to="item.to"
            :prepend-icon="item.icon"
            :title="item.title"
            class="sidebar-item sidebar-subitem"
            :active="isPagosModuloActivo(item.modulo)"
            active-class="sidebar-item--active"
          />
        </v-list-group>

        <v-list-group value="notificaciones">
          <template #activator="{ props }">
            <v-list-item
              v-bind="props"
              prepend-icon="mdi-bell-outline"
              title="Notificaciones"
              class="sidebar-item"
              :active="isNotificacionesRoute"
              active-class="sidebar-item--active"
            />
          </template>

          <v-list-item
            v-for="item in notificacionesItems"
            :key="item.modulo"
            :to="item.to"
            :prepend-icon="item.icon"
            :title="item.title"
            class="sidebar-item sidebar-subitem"
            :active="isNotificacionesModuloActivo(item.modulo)"
            active-class="sidebar-item--active"
          />
        </v-list-group>

        <v-list-group value="reportes">
          <template #activator="{ props }">
            <v-list-item
              v-bind="props"
              prepend-icon="mdi-chart-box-outline"
              title="Reportes"
              class="sidebar-item"
              :active="isReportesRoute"
              active-class="sidebar-item--active"
            />
          </template>

          <v-list-item
            v-for="item in reportesItems"
            :key="item.modulo"
            :to="item.to"
            :prepend-icon="item.icon"
            :title="item.title"
            class="sidebar-item sidebar-subitem"
            :active="isReportesModuloActivo(item.modulo)"
            active-class="sidebar-item--active"
          />
        </v-list-group>
      </v-list>

      <template #append>
        <v-divider />

        <v-list density="compact" nav class="sidebar-menu sidebar-menu--bottom pt-2">
          <v-list-item
            prepend-icon="mdi-account-circle-outline"
            title="Mi perfil"
            class="sidebar-item"
            @click="verPerfil"
          />
          <v-list-item
            prepend-icon="mdi-logout"
            title="Cerrar sesión"
            class="sidebar-item sidebar-item--danger"
            @click="handleLogout"
          />
        </v-list>
      </template>
    </v-navigation-drawer>

    <v-app-bar class="app-header" elevation="0">
      <v-app-bar-nav-icon
        class="header-menu-btn"
        @click="toggleNavigation"
      />

      <div class="d-flex flex-column justify-center min-w-0">
        <span class="header-eyebrow">Panel administrativo</span>
        <span class="header-title text-truncate">
          IEP Thales de Mileto
        </span>
      </div>

      <v-spacer />

      <v-chip
        v-if="mdAndUp"
        size="small"
        variant="tonal"
        color="primary"
        class="header-context-chip mr-3"
      >
        {{ currentSectionLabel }}
      </v-chip>

      <v-menu offset="12">
        <template #activator="{ props }">
          <button
            v-bind="props"
            type="button"
            class="header-user-trigger mr-2"
          >
            <v-avatar size="38" class="header-avatar">
              <v-icon>mdi-account</v-icon>
            </v-avatar>

            <div v-if="mdAndUp" class="header-user-meta text-left">
              <p class="mb-0 text-body-2 font-weight-bold header-user-name">
                {{ authStore.displayName }}
              </p>

              <p class="mb-0 text-caption header-user-role">
                {{ authStore.displayRole }}
              </p>
            </div>

            <v-icon size="18">mdi-chevron-down</v-icon>
          </button>
        </template>

        <v-card min-width="240" class="user-menu-card">
          <v-list density="compact">
            <v-list-item
              prepend-icon="mdi-view-dashboard-outline"
              title="Ir al inicio"
              @click="goHome"
            />
            <v-list-item
              prepend-icon="mdi-account-circle-outline"
              title="Ver perfil"
              @click="verPerfil"
            />
            <v-divider />
            <v-list-item
              prepend-icon="mdi-logout"
              title="Cerrar sesión"
              class="text-error"
              @click="handleLogout"
            />
          </v-list>
        </v-card>
      </v-menu>
    </v-app-bar>

    <v-main class="app-main">
      <div class="app-main__inner">
        <slot />
      </div>
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import { useDisplay } from 'vuetify'
import { useAuthStore } from '~/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const { mdAndUp, smAndDown } = useDisplay()

const drawer = ref(true)
const rail = ref(false)

const openedGroups = ref([
  'matricula',
  'academico',
  'pagos',
  'notificaciones',
  'reportes',
])

const matriculaItems = [
  {
    modulo: 'matriculas',
    title: 'Matrículas',
    icon: 'mdi-clipboard-account-outline',
    to: { path: '/dashboard/matricula', query: { modulo: 'matriculas' } },
  },
  {
    modulo: 'estudiantes',
    title: 'Estudiantes',
    icon: 'mdi-account-school-outline',
    to: { path: '/dashboard/matricula', query: { modulo: 'estudiantes' } },
  },
  {
    modulo: 'apoderados',
    title: 'Apoderados',
    icon: 'mdi-account-child-outline',
    to: { path: '/dashboard/matricula', query: { modulo: 'apoderados' } },
  },
  {
    modulo: 'detalles-matricula',
    title: 'Detalle matrícula',
    icon: 'mdi-format-list-checks',
    to: { path: '/dashboard/matricula', query: { modulo: 'detalles-matricula' } },
  },
  {
    modulo: 'documentos-matricula',
    title: 'Documentos',
    icon: 'mdi-file-document-outline',
    to: { path: '/dashboard/matricula', query: { modulo: 'documentos-matricula' } },
  },
  {
    modulo: 'programas-academicos',
    title: 'Programas académicos',
    icon: 'mdi-school-outline',
    to: { path: '/dashboard/matricula', query: { modulo: 'programas-academicos' } },
  },
  {
    modulo: 'grados',
    title: 'Grados',
    icon: 'mdi-numeric',
    to: { path: '/dashboard/matricula', query: { modulo: 'grados' } },
  },
  {
    modulo: 'secciones',
    title: 'Secciones',
    icon: 'mdi-google-classroom',
    to: { path: '/dashboard/matricula', query: { modulo: 'secciones' } },
  },
  {
    modulo: 'periodos-academicos',
    title: 'Periodos académicos',
    icon: 'mdi-calendar-range',
    to: { path: '/dashboard/matricula', query: { modulo: 'periodos-academicos' } },
  },
  {
    modulo: 'cursos',
    title: 'Cursos',
    icon: 'mdi-book-open-variant',
    to: { path: '/dashboard/matricula', query: { modulo: 'cursos' } },
  },
  {
    modulo: 'planes-estudio',
    title: 'Planes de estudio',
    icon: 'mdi-book-cog-outline',
    to: { path: '/dashboard/matricula', query: { modulo: 'planes-estudio' } },
  },
  {
    modulo: 'vacantes',
    title: 'Vacantes',
    icon: 'mdi-counter',
    to: { path: '/dashboard/matricula', query: { modulo: 'vacantes' } },
  },
]

const academicoItems = [
  {
    modulo: 'docentes',
    title: 'Docentes',
    icon: 'mdi-account-tie-outline',
    to: { path: '/dashboard/academico', query: { modulo: 'docentes' } },
  },
  {
    modulo: 'asignaciones',
    title: 'Asignaciones',
    icon: 'mdi-account-switch-outline',
    to: { path: '/dashboard/academico', query: { modulo: 'asignaciones' } },
  },
  {
    modulo: 'asistencias',
    title: 'Asistencias',
    icon: 'mdi-calendar-check-outline',
    to: { path: '/dashboard/academico', query: { modulo: 'asistencias' } },
  },
  {
    modulo: 'notas',
    title: 'Notas',
    icon: 'mdi-text-box-check-outline',
    to: { path: '/dashboard/academico', query: { modulo: 'notas' } },
  },
  {
    modulo: 'prerequisitos',
    title: 'Prerrequisitos',
    icon: 'mdi-source-branch-check',
    to: { path: '/dashboard/academico', query: { modulo: 'prerequisitos' } },
  },
]

const pagosItems = [
  {
    modulo: 'deudas',
    title: 'Deudas',
    icon: 'mdi-cash-clock',
    to: { path: '/dashboard/pagos', query: { modulo: 'deudas' } },
  },
  {
    modulo: 'pagos',
    title: 'Pagos registrados',
    icon: 'mdi-cash-check',
    to: { path: '/dashboard/pagos', query: { modulo: 'pagos' } },
  },
  {
    modulo: 'comprobantes',
    title: 'Comprobantes',
    icon: 'mdi-receipt-text-outline',
    to: { path: '/dashboard/pagos', query: { modulo: 'comprobantes' } },
  },
  {
    modulo: 'intentos',
    title: 'Intentos de pago',
    icon: 'mdi-credit-card-clock-outline',
    to: { path: '/dashboard/pagos', query: { modulo: 'intentos' } },
  },
  {
    modulo: 'pasarelas',
    title: 'Pasarelas',
    icon: 'mdi-credit-card-outline',
    to: { path: '/dashboard/pagos', query: { modulo: 'pasarelas' } },
  },
]

const notificacionesItems = [
  {
    modulo: 'notificaciones',
    title: 'Notificaciones',
    icon: 'mdi-bell-outline',
    to: { path: '/dashboard/notificaciones', query: { modulo: 'notificaciones' } },
  },
  {
    modulo: 'plantillas',
    title: 'Plantillas',
    icon: 'mdi-file-document-edit-outline',
    to: { path: '/dashboard/notificaciones', query: { modulo: 'plantillas' } },
  },
  {
    modulo: 'destinatarios',
    title: 'Destinatarios',
    icon: 'mdi-account-multiple-outline',
    to: { path: '/dashboard/notificaciones', query: { modulo: 'destinatarios' } },
  },
  {
    modulo: 'preferencias',
    title: 'Preferencias',
    icon: 'mdi-tune-variant',
    to: { path: '/dashboard/notificaciones', query: { modulo: 'preferencias' } },
  },
]

const reportesItems = [
  {
    modulo: 'resumen',
    title: 'Resumen',
    icon: 'mdi-view-dashboard-outline',
    to: { path: '/dashboard/reportes', query: { modulo: 'resumen' } },
  },
  {
    modulo: 'indicadores',
    title: 'Indicadores',
    icon: 'mdi-chart-line',
    to: { path: '/dashboard/reportes', query: { modulo: 'indicadores' } },
  },
  {
    modulo: 'auditoria',
    title: 'Auditoría',
    icon: 'mdi-history',
    to: { path: '/dashboard/reportes', query: { modulo: 'auditoria' } },
  },
  {
    modulo: 'generados',
    title: 'Reportes generados',
    icon: 'mdi-file-chart-outline',
    to: { path: '/dashboard/reportes', query: { modulo: 'generados' } },
  },
]

const currentSectionLabel = computed(() => {
  if (route.path === '/') return 'Resumen general'
  if (route.path.startsWith('/dashboard/matricula')) return 'Matrícula'
  if (route.path.startsWith('/dashboard/academico')) return 'Académico'
  if (route.path.startsWith('/dashboard/pagos')) return 'Pagos'
  if (route.path.startsWith('/dashboard/notificaciones')) return 'Notificaciones'
  if (route.path.startsWith('/dashboard/reportes')) return 'Reportes'
  if (route.path.startsWith('/dashboard/usuarios')) return 'Usuarios'
  if (route.path.startsWith('/dashboard/roles')) return 'Roles'
  if (route.path.startsWith('/dashboard/perfil')) return 'Mi perfil'
  if (route.path.startsWith('/dashboard/ajustes')) return 'Ajustes'
  return 'Panel administrativo'
})

const isAcademicoRoute = computed(() => route.path.startsWith('/dashboard/academico'))
const isMatriculaRoute = computed(() => route.path.startsWith('/dashboard/matricula'))
const isPagosRoute = computed(() => route.path.startsWith('/dashboard/pagos'))
const isNotificacionesRoute = computed(() => route.path.startsWith('/dashboard/notificaciones'))
const isReportesRoute = computed(() => route.path.startsWith('/dashboard/reportes'))

function academicoModuloActual() {
  return String(route.query.modulo || 'docentes')
}

function moduloActual() {
  return String(route.query.modulo || 'matriculas')
}

function pagosModuloActual() {
  return String(route.query.modulo || 'deudas')
}

function notificacionesModuloActual() {
  return String(route.query.modulo || 'notificaciones')
}

function isAcademicoModuloActivo(modulo: string) {
  return isAcademicoRoute.value && academicoModuloActual() === modulo
}

function isModuloActivo(modulo: string) {
  return isMatriculaRoute.value && moduloActual() === modulo
}

function isPagosModuloActivo(modulo: string) {
  return isPagosRoute.value && pagosModuloActual() === modulo
}

function isNotificacionesModuloActivo(modulo: string) {
  return isNotificacionesRoute.value && notificacionesModuloActual() === modulo
}

function isReportesModuloActivo(modulo: string) {
  return isReportesRoute.value && String(route.query.modulo || 'resumen') === modulo
}

function toggleNavigation() {
  if (smAndDown.value) {
    drawer.value = !drawer.value
    return
  }

  rail.value = !rail.value
}

function goHome() {
  router.push('/')
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}

function verPerfil() {
  router.push('/dashboard/perfil')
}

watch(
  () => smAndDown.value,
  (isMobile) => {
    drawer.value = !isMobile
    if (isMobile) rail.value = false
  },
  { immediate: true }
)

watch(
  () => route.fullPath,
  () => {
    if (smAndDown.value) drawer.value = false
  }
)
</script>

<style scoped>
.sidebar-subitem {
  margin-left: 14px !important;
  padding-inline-start: 10px !important;
}

.sidebar-subitem :deep(.v-list-item-title) {
  font-size: 13px;
}

.v-navigation-drawer--rail .sidebar-subitem {
  margin-left: 0 !important;
}

.sidebar-brand {
  cursor: pointer;
}

.header-user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  border: 0;
  background: transparent;
  border-radius: 16px;
  padding: 6px 10px;
  cursor: pointer;
}

.header-user-trigger:hover {
  background: rgba(109, 23, 48, 0.06);
}

.header-user-meta {
  min-width: 0;
}

.min-w-0 {
  min-width: 0;
}
</style>
