<template>
  <div>
    <div class="d-flex justify-space-between align-center mb-4 flex-wrap ga-3">
      <v-btn variant="tonal" color="primary" :loading="loading" @click="cargar">
        <v-icon start>mdi-refresh</v-icon>
        Actualizar
      </v-btn>
    </div>

    <v-alert
      v-if="errorMsg"
      type="error"
      variant="tonal"
      closable
      class="mb-4"
      @click:close="errorMsg = ''"
    >
      {{ errorMsg }}
    </v-alert>

    <v-row class="mb-5">
      <v-col
        v-for="item in indicadoresResumen"
        :key="item.codigo"
        cols="12"
        sm="6"
        lg="3"
      >
        <v-card class="main-card pa-5 h-100">
          <div class="d-flex justify-space-between align-start ga-3">
            <div>
              <p class="text-caption text-grey-darken-1 mb-2">{{ item.nombre }}</p>
              <p class="text-h4 font-weight-bold mb-1">
                {{ formatearValor(item) }}
              </p>
              <p class="text-caption mb-0">
                {{ item.periodo || 'Sin periodo' }}
              </p>
            </div>
            <v-avatar color="primary" variant="tonal" size="44">
              <v-icon>{{ item.icon }}</v-icon>
            </v-avatar>
          </div>
        </v-card>
      </v-col>
    </v-row>

    <v-row>
      <v-col cols="12" lg="7">
        <v-card class="main-card pa-5 h-100">
          <div class="d-flex justify-space-between align-center mb-4">
            <h3 class="text-h6 font-weight-bold">Actividad reciente</h3>
            <v-btn
              size="small"
              variant="text"
              :to="{ path: '/dashboard/reportes', query: { modulo: 'auditoria' } }"
            >
              Ver auditoría
            </v-btn>
          </div>

          <v-list v-if="actividad.length" density="compact">
            <v-list-item
              v-for="evento in actividad"
              :key="evento.id"
              class="px-0"
              :prepend-icon="iconoEvento(evento)"
            >
              <v-list-item-title class="font-weight-medium">
                {{ tituloEvento(evento) }}
              </v-list-item-title>
              <v-list-item-subtitle>
                {{ evento.microservicio || '-' }} · {{ fecha(evento.fecha) }}
              </v-list-item-subtitle>
            </v-list-item>
          </v-list>

          <v-alert v-else type="info" variant="tonal">
            No hay eventos de auditoría registrados.
          </v-alert>
        </v-card>
      </v-col>

      <v-col cols="12" lg="5">
        <v-card class="main-card pa-5 h-100">
          <h3 class="text-h6 font-weight-bold mb-4">Reportes generados</h3>

          <div class="d-flex flex-column ga-3">
            <div class="d-flex justify-space-between">
              <span>Total</span>
              <strong>{{ reportesGenerados.length }}</strong>
            </div>
            <div class="d-flex justify-space-between">
              <span>Solicitados</span>
              <strong>{{ contarEstado('SOLICITADO') }}</strong>
            </div>
            <div class="d-flex justify-space-between">
              <span>Generando</span>
              <strong>{{ contarEstado('GENERANDO') }}</strong>
            </div>
            <div class="d-flex justify-space-between">
              <span>Listos</span>
              <strong>{{ contarEstado('LISTO') }}</strong>
            </div>
            <div class="d-flex justify-space-between">
              <span>Con error</span>
              <strong>{{ contarEstado('ERROR') }}</strong>
            </div>
          </div>

          <v-btn
            class="mt-5"
            block
            color="primary"
            :to="{ path: '/dashboard/reportes', query: { modulo: 'generados' } }"
          >
            Gestionar reportes
          </v-btn>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script setup lang="ts">
const { reportes } = useReportesHttp()

const loading = ref(false)
const errorMsg = ref('')
const indicadores = ref<any[]>([])
const actividad = ref<any[]>([])
const reportesGenerados = ref<any[]>([])

const definiciones = [
  { codigo: 'DISPONIBILIDAD', nombre: 'Disponibilidad', icon: 'mdi-server-network', sufijo: '%' },
  { codigo: 'TIEMPO_RESPUESTA', nombre: 'Tiempo de respuesta', icon: 'mdi-timer-outline', sufijo: ' ms' },
  { codigo: 'TASA_ERROR', nombre: 'Tasa de error', icon: 'mdi-alert-circle-outline', sufijo: '%' },
  { codigo: 'OPERACIONES_PROCESADAS', nombre: 'Operaciones procesadas', icon: 'mdi-swap-horizontal-bold', sufijo: '' },
]

const indicadoresResumen = computed(() =>
  definiciones.map(def => {
    const encontrados = indicadores.value
      .filter(x => String(x.codigo || '').toUpperCase() === def.codigo)
      .sort((a, b) => new Date(b.fechaCalculo || 0).getTime() - new Date(a.fechaCalculo || 0).getTime())

    return {
      ...def,
      ...(encontrados[0] || {}),
      codigo: def.codigo,
      nombre: encontrados[0]?.nombre || def.nombre,
      sufijo: def.sufijo,
    }
  })
)

async function cargar() {
  loading.value = true
  errorMsg.value = ''

  try {
    const [i, e, r] = await Promise.all([
      reportes('/api/indicadores'),
      reportes('/api/eventos-auditoria'),
      reportes('/api/reportes-generados'),
    ])

    indicadores.value = Array.isArray(i) ? i : []
    actividad.value = (Array.isArray(e) ? e : [])
      .sort((a, b) => new Date(b.fecha || 0).getTime() - new Date(a.fecha || 0).getTime())
      .slice(0, 8)
    reportesGenerados.value = Array.isArray(r) ? r : []
  } catch (e: any) {
    errorMsg.value = e.message
  } finally {
    loading.value = false
  }
}

function formatearValor(item: any) {
  if (item.valor === null || item.valor === undefined) return '-'
  return `${item.valor}${item.sufijo || ''}`
}

function fecha(valor: any) {
  if (!valor) return '-'
  return new Date(valor).toLocaleString('es-PE')
}

function contarEstado(estado: string) {
  return reportesGenerados.value.filter(
    x => String(x.estado || '').toUpperCase() === estado
  ).length
}

function tituloEvento(evento: any) {
  return evento.tipoEvento || evento.accion || 'Evento de auditoría'
}

function iconoEvento(evento: any) {
  const accion = String(evento.accion || '').toUpperCase()
  if (accion === 'CREAR') return 'mdi-plus-circle-outline'
  if (accion === 'ACTUALIZAR') return 'mdi-pencil-outline'
  if (accion === 'ELIMINAR') return 'mdi-delete-outline'
  if (accion === 'CONSULTAR') return 'mdi-magnify'
  return 'mdi-history'
}

onMounted(cargar)
</script>
