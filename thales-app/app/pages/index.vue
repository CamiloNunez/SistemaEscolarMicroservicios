<template>
  <v-container class="py-8">
    <v-row class="mb-6">
      <v-col cols="12">
        <v-card class="main-card pa-6">
          <div class="d-flex align-center justify-space-between flex-wrap ga-4">
            <div class="d-flex align-center ga-4">
              <v-avatar size="64" color="primary">
                <v-icon size="36">mdi-school</v-icon>
              </v-avatar>
              <div>
                <h1 class="text-h5 font-weight-bold">
                  ¡Bienvenido, {{ authStore.user?.nombre }}!
                </h1>
                <p class="text-body-2 text-grey-darken-1 mb-0">
                  {{ authStore.user?.rol }} · {{ fechaActual }}
                </p>
              </div>
            </div>

            <v-btn
              variant="tonal"
              color="primary"
              :loading="loadingDashboard"
              @click="cargarDashboard"
            >
              <v-icon start>mdi-refresh</v-icon>
              Actualizar
            </v-btn>
          </div>
        </v-card>
      </v-col>
    </v-row>

    <v-alert
      v-if="dashboardError"
      type="warning"
      variant="tonal"
      closable
      class="mb-6"
      @click:close="dashboardError = ''"
    >
      {{ dashboardError }}
    </v-alert>

    <v-row class="mb-6">
      <v-col
        v-for="stat in stats"
        :key="stat.codigo"
        cols="12"
        sm="6"
        md="3"
      >
        <v-card class="main-card pa-5 text-center h-100">
          <v-icon size="40" class="mb-3">{{ stat.icon }}</v-icon>
          <p class="text-h4 font-weight-bold mb-1">
            {{ stat.value }}
          </p>
          <p class="text-body-2 text-grey-darken-1 mb-0">
            {{ stat.label }}
          </p>
          <p class="text-caption mt-1 mb-0">
            {{ stat.periodo }}
          </p>
        </v-card>
      </v-col>
    </v-row>

    <v-row class="mb-6">
      <v-col cols="12">
        <h2 class="text-h6 font-weight-bold mb-4">
          <v-icon class="mr-2">mdi-lightning-bolt</v-icon>
          Accesos rápidos
        </h2>
      </v-col>

      <v-col
        v-for="action in quickActions"
        :key="action.title"
        cols="12"
        sm="6"
        md="4"
      >
        <v-card
          class="main-card pa-5 d-flex align-center ga-4"
          :to="action.to"
          hover
          style="cursor:pointer;text-decoration:none"
        >
          <v-avatar size="48" color="primary" variant="tonal" class="flex-shrink-0">
            <v-icon>{{ action.icon }}</v-icon>
          </v-avatar>
          <div>
            <p class="font-weight-bold mb-0">{{ action.title }}</p>
            <p class="text-caption text-grey-darken-1 mb-0">{{ action.desc }}</p>
          </div>
          <v-icon class="ml-auto">mdi-chevron-right</v-icon>
        </v-card>
      </v-col>
    </v-row>

    <v-row>
      <v-col cols="12" md="8">
        <v-card class="main-card pa-5 h-100">
          <div class="d-flex justify-space-between align-center mb-4">
            <h2 class="text-h6 font-weight-bold">
              <v-icon class="mr-2">mdi-history</v-icon>
              Actividad reciente
            </h2>
            <v-btn
              size="small"
              variant="text"
              :to="{ path: '/dashboard/reportes', query: { modulo: 'auditoria' } }"
            >
              Ver todo
            </v-btn>
          </div>

          <v-list v-if="recentActivity.length" density="compact">
            <v-list-item
              v-for="item in recentActivity"
              :key="item.id"
              :prepend-icon="item.icon"
              :subtitle="item.time"
              class="px-0"
            >
              <v-list-item-title>{{ item.text }}</v-list-item-title>
            </v-list-item>
          </v-list>

          <v-alert v-else type="info" variant="tonal">
            No hay actividad de auditoría registrada.
          </v-alert>
        </v-card>
      </v-col>

      <v-col cols="12" md="4">
        <v-card class="main-card pa-5 h-100">
          <h2 class="text-h6 font-weight-bold mb-4">
            <v-icon class="mr-2">mdi-file-chart-outline</v-icon>
            Reportes
          </h2>

          <div class="d-flex justify-space-between mb-3">
            <span class="text-body-2">Total registrados</span>
            <strong>{{ reportesResumen.total }}</strong>
          </div>
          <div class="d-flex justify-space-between mb-3">
            <span class="text-body-2">Listos</span>
            <strong>{{ reportesResumen.listos }}</strong>
          </div>
          <div class="d-flex justify-space-between mb-3">
            <span class="text-body-2">En proceso</span>
            <strong>{{ reportesResumen.proceso }}</strong>
          </div>
          <div class="d-flex justify-space-between mb-5">
            <span class="text-body-2">Con error</span>
            <strong>{{ reportesResumen.error }}</strong>
          </div>

          <v-btn
            block
            color="primary"
            :to="{ path: '/dashboard/reportes', query: { modulo: 'generados' } }"
          >
            Abrir reportes
          </v-btn>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
definePageMeta({ middleware: 'auth' })

const authStore = useAuthStore()
const { reportes } = useReportesHttp()

const loadingDashboard = ref(false)
const dashboardError = ref('')
const indicadores = ref<any[]>([])
const eventos = ref<any[]>([])
const reportesGenerados = ref<any[]>([])

const fechaActual = new Date().toLocaleDateString('es-PE', {
  weekday: 'long',
  year: 'numeric',
  month: 'long',
  day: 'numeric',
})

const definiciones = [
  { codigo: 'DISPONIBILIDAD', label: 'Disponibilidad', icon: 'mdi-server-network', sufijo: '%' },
  { codigo: 'TIEMPO_RESPUESTA', label: 'Tiempo respuesta', icon: 'mdi-timer-outline', sufijo: ' ms' },
  { codigo: 'TASA_ERROR', label: 'Tasa de error', icon: 'mdi-alert-circle-outline', sufijo: '%' },
  { codigo: 'OPERACIONES_PROCESADAS', label: 'Operaciones', icon: 'mdi-swap-horizontal-bold', sufijo: '' },
]

const stats = computed(() =>
  definiciones.map(def => {
    const ultimo = indicadores.value
      .filter(x => String(x.codigo || '').toUpperCase() === def.codigo)
      .sort((a, b) =>
        new Date(b.fechaCalculo || 0).getTime() - new Date(a.fechaCalculo || 0).getTime()
      )[0]

    return {
      ...def,
      value: ultimo?.valor === null || ultimo?.valor === undefined
        ? '-'
        : `${ultimo.valor}${def.sufijo}`,
      periodo: ultimo?.periodo || 'Sin datos',
    }
  })
)

const recentActivity = computed(() =>
  [...eventos.value]
    .sort((a, b) => new Date(b.fecha || 0).getTime() - new Date(a.fecha || 0).getTime())
    .slice(0, 7)
    .map(evento => ({
      id: evento.id,
      icon: iconoEvento(evento),
      text: evento.tipoEvento || `${evento.accion || 'EVENTO'} ${evento.entidad || ''}`.trim(),
      time: `${evento.microservicio || '-'} · ${formatearFecha(evento.fecha)}`,
    }))
)

const reportesResumen = computed(() => ({
  total: reportesGenerados.value.length,
  listos: contarEstado('LISTO'),
  proceso: contarEstado('SOLICITADO') + contarEstado('GENERANDO'),
  error: contarEstado('ERROR'),
}))

const quickActions = [
  {
    title: 'Reportes',
    desc: 'Indicadores, auditoría y archivos',
    icon: 'mdi-chart-box-outline',
    to: { path: '/dashboard/reportes', query: { modulo: 'resumen' } },
  },
  {
    title: 'Matrícula',
    desc: 'Gestionar inscripciones',
    icon: 'mdi-clipboard-account',
    to: '/dashboard/matricula',
  },
  {
    title: 'Académico',
    desc: 'Docentes, notas y asistencias',
    icon: 'mdi-book-education-outline',
    to: '/dashboard/academico',
  },
  {
    title: 'Pagos',
    desc: 'Deudas, pagos y comprobantes',
    icon: 'mdi-cash-multiple',
    to: '/dashboard/pagos',
  },
  {
    title: 'Notificaciones',
    desc: 'Mensajes, plantillas y entregas',
    icon: 'mdi-bell-outline',
    to: '/dashboard/notificaciones',
  },
  {
    title: 'Test API',
    desc: 'Probar endpoints del backend',
    icon: 'mdi-api',
    to: '/dashboard/api-test',
  },
]

async function cargarDashboard() {
  loadingDashboard.value = true
  dashboardError.value = ''

  try {
    const [i, e, r] = await Promise.all([
      reportes('/api/indicadores'),
      reportes('/api/eventos-auditoria'),
      reportes('/api/reportes-generados'),
    ])

    indicadores.value = Array.isArray(i) ? i : []
    eventos.value = Array.isArray(e) ? e : []
    reportesGenerados.value = Array.isArray(r) ? r : []
  } catch (e: any) {
    dashboardError.value = `No se pudieron cargar las métricas de Reportes: ${e.message}`
  } finally {
    loadingDashboard.value = false
  }
}

function contarEstado(estado: string) {
  return reportesGenerados.value.filter(
    x => String(x.estado || '').toUpperCase() === estado
  ).length
}

function formatearFecha(valor: any) {
  return valor ? new Date(valor).toLocaleString('es-PE') : '-'
}

function iconoEvento(evento: any) {
  const accion = String(evento.accion || '').toUpperCase()
  if (accion === 'CREAR') return 'mdi-plus-circle-outline'
  if (accion === 'ACTUALIZAR') return 'mdi-pencil-outline'
  if (accion === 'ELIMINAR') return 'mdi-delete-outline'
  if (accion === 'CONSULTAR') return 'mdi-magnify'
  return 'mdi-history'
}

onMounted(cargarDashboard)
</script>
