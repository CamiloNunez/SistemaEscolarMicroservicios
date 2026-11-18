<template>
  <div>
    <div class="d-flex justify-space-between align-center mb-4 flex-wrap ga-3">
      <v-btn variant="tonal" color="primary" :loading="loading" @click="cargar">
        <v-icon start>mdi-refresh</v-icon>Actualizar
      </v-btn>
    </div>

    <v-alert v-if="errorMsg" type="error" variant="tonal" closable class="mb-4" @click:close="errorMsg=''">
      {{ errorMsg }}
    </v-alert>

    <v-card class="main-card pa-4 mb-4">
      <v-row>
        <v-col cols="12" md="6">
          <v-text-field
            v-model="buscar"
            label="Buscar evento, usuario, entidad o correlation ID"
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            clearable
            hide-details
          />
        </v-col>
        <v-col cols="12" md="3">
          <v-select
            v-model="microservicioFiltro"
            :items="microservicios"
            label="Microservicio"
            variant="outlined"
            density="compact"
            hide-details
          />
        </v-col>
        <v-col cols="12" md="3">
          <v-select
            v-model="accionFiltro"
            :items="acciones"
            label="Acción"
            variant="outlined"
            density="compact"
            hide-details
          />
        </v-col>
      </v-row>
    </v-card>

    <v-card class="main-card">
      <v-data-table
        :headers="headers"
        :items="filtrados"
        :loading="loading"
        :items-per-page="15"
      >
        <template #item.fecha="{ item }">
          {{ fecha(item.fecha) }}
        </template>
        <template #item.microservicio="{ item }">
          <v-chip size="small">{{ item.microservicio || '-' }}</v-chip>
        </template>
        <template #item.accion="{ item }">
          <v-chip size="small" variant="tonal">{{ item.accion || '-' }}</v-chip>
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>

<script setup lang="ts">
const { reportes } = useReportesHttp()

const loading = ref(false)
const errorMsg = ref('')
const buscar = ref('')
const microservicioFiltro = ref('TODOS')
const accionFiltro = ref('TODAS')
const registros = ref<any[]>([])

const headers = [
  { title: 'Fecha', key: 'fecha' },
  { title: 'Microservicio', key: 'microservicio' },
  { title: 'Evento', key: 'tipoEvento' },
  { title: 'Acción', key: 'accion' },
  { title: 'Entidad', key: 'entidad' },
  { title: 'Usuario', key: 'usuarioId' },
  { title: 'Correlation ID', key: 'correlationId' },
]

const microservicios = computed(() => [
  'TODOS',
  ...Array.from(new Set(
    registros.value
      .map(x => String(x.microservicio || '').trim())
      .filter(Boolean)
  )).sort(),
])

const acciones = computed(() => [
  'TODAS',
  ...Array.from(new Set(
    registros.value
      .map(x => String(x.accion || '').trim())
      .filter(Boolean)
  )).sort(),
])

const filtrados = computed(() => {
  const q = buscar.value.toLowerCase().trim()

  return registros.value
    .filter(x =>
      (!q || [
        x.tipoEvento,
        x.usuarioId,
        x.entidad,
        x.entidadId,
        x.correlationId,
      ].some(v => String(v || '').toLowerCase().includes(q))) &&
      (microservicioFiltro.value === 'TODOS' || x.microservicio === microservicioFiltro.value) &&
      (accionFiltro.value === 'TODAS' || x.accion === accionFiltro.value)
    )
    .sort((a, b) =>
      new Date(b.fecha || 0).getTime() - new Date(a.fecha || 0).getTime()
    )
})

async function cargar() {
  loading.value = true
  errorMsg.value = ''

  try {
    const data = await reportes('/api/eventos-auditoria')
    registros.value = Array.isArray(data) ? data : []
  } catch (e: any) {
    errorMsg.value = e.message
  } finally {
    loading.value = false
  }
}

function fecha(valor: any) {
  return valor ? new Date(valor).toLocaleString('es-PE') : '-'
}

onMounted(cargar)
</script>
