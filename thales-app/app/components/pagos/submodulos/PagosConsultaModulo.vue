<template>
  <div>
    <div class="d-flex justify-space-between align-center mb-4 flex-wrap ga-3">
      <v-btn
        variant="tonal"
        color="primary"
        :loading="loading"
        @click="cargar"
      >
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

    <v-card class="main-card pa-4 mb-4">
      <v-row>
        <v-col cols="12" md="8">
          <v-text-field
            v-model="buscar"
            label="Buscar por estudiante, matrícula, referencia o método"
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            clearable
            hide-details
          />
        </v-col>

        <v-col cols="12" md="4">
          <v-select
            v-model="estado"
            :items="['TODOS', 'PENDIENTE', 'CONFIRMADO', 'RECHAZADO', 'REEMBOLSADO']"
            label="Estado"
            variant="outlined"
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
        no-data-text="No existen pagos registrados"
      >
        <template #item.monto="{ item }">
          <strong>{{ moneda(item.monto, item.moneda) }}</strong>
        </template>

        <template #item.fechaPago="{ item }">
          {{ fecha(item.fechaPago) }}
        </template>

        <template #item.estado="{ item }">
          <v-chip
            size="small"
            variant="tonal"
            :color="colorEstado(item.estado)"
          >
            {{ item.estado }}
          </v-chip>
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>

<script setup lang="ts">
const { pagos } = usePagosHttp()

const loading = ref(false)
const errorMsg = ref('')
const buscar = ref('')
const estado = ref('TODOS')

const registros = ref<any[]>([])

const headers = [
  { title: 'Estudiante', key: 'estudianteId' },
  { title: 'Matrícula', key: 'matriculaId' },
  { title: 'Deuda', key: 'deudaId' },
  { title: 'Monto', key: 'monto' },
  { title: 'Método', key: 'metodoPago' },
  { title: 'Referencia', key: 'referenciaExterna' },
  { title: 'Fecha', key: 'fechaPago' },
  { title: 'Estado', key: 'estado' },
]

const filtrados = computed(() => {
  const q = buscar.value.trim().toLowerCase()

  return registros.value.filter((p: any) => {
    const coincideTexto =
      !q ||
      [
        p.estudianteId,
        p.matriculaId,
        p.deudaId,
        p.metodoPago,
        p.referenciaExterna,
      ].some(x =>
        String(x || '').toLowerCase().includes(q)
      )

    const coincideEstado =
      estado.value === 'TODOS' ||
      p.estado === estado.value

    return coincideTexto && coincideEstado
  })
})

async function cargar() {
  loading.value = true
  errorMsg.value = ''

  try {
    const data = await pagos('/api/pagos')
    registros.value = Array.isArray(data) ? data : []
  } catch (e: any) {
    errorMsg.value =
      e.message || 'No se pudieron cargar los pagos.'
  } finally {
    loading.value = false
  }
}

function moneda(monto: number, moneda = 'PEN') {
  return new Intl.NumberFormat('es-PE', {
    style: 'currency',
    currency: moneda || 'PEN',
  }).format(Number(monto || 0))
}

function fecha(value?: string) {
  if (!value) return '-'

  const d = new Date(value)

  if (Number.isNaN(d.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('es-PE', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(d)
}

function colorEstado(estado?: string) {
  if (estado === 'CONFIRMADO') return 'success'
  if (estado === 'PENDIENTE') return 'warning'
  if (estado === 'REEMBOLSADO') return 'info'
  if (estado === 'RECHAZADO') return 'error'

  return 'default'
}

onMounted(cargar)
</script>
