<template>
  <div>
    <div class="d-flex justify-space-between align-center mb-4">

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
      class="mb-4"
    >
      {{ errorMsg }}
    </v-alert>

    <v-card class="main-card">
      <v-data-table
        :headers="headers"
        :items="registros"
        :loading="loading"
      >
        <template #item.monto="{ item }">
          {{ moneda(item.monto) }}
        </template>

        <template #item.fechaIntento="{ item }">
          {{ fecha(item.fechaIntento) }}
        </template>

        <template #item.estado="{ item }">
          <v-chip
            size="small"
            variant="tonal"
            :color="color(item.estado)"
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
const registros = ref<any[]>([])

const headers = [
  { title: 'Pago', key: 'pagoId' },
  { title: 'Deuda', key: 'deudaId' },
  { title: 'Proveedor', key: 'proveedor' },
  { title: 'Payment Intent', key: 'paymentIntentId' },
  { title: 'Monto', key: 'monto' },
  { title: 'Estado', key: 'estado' },
  { title: 'Respuesta', key: 'mensajeRespuesta' },
  { title: 'Fecha', key: 'fechaIntento' },
]

async function cargar() {
  loading.value = true
  errorMsg.value = ''

  try {
    const data = await pagos('/api/intentos-pago')
    registros.value = Array.isArray(data) ? data : []
  } catch (e: any) {
    errorMsg.value =
      e.message || 'No se pudieron cargar los intentos de pago.'
  } finally {
    loading.value = false
  }
}

function moneda(value: number) {
  return new Intl.NumberFormat('es-PE', {
    style: 'currency',
    currency: 'PEN',
  }).format(Number(value || 0))
}

function fecha(value?: string) {
  if (!value) return '-'

  const d = new Date(value)

  if (Number.isNaN(d.getTime())) return value

  return new Intl.DateTimeFormat('es-PE', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(d)
}

function color(estado?: string) {
  if (estado === 'EXITOSO') return 'success'
  if (estado === 'FALLIDO') return 'error'
  if (estado === 'PROCESANDO') return 'info'

  return 'warning'
}

onMounted(cargar)
</script>
