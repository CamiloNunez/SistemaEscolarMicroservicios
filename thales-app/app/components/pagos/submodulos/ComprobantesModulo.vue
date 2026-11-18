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

    <v-card class="main-card pa-4 mb-4">
      <v-text-field
        v-model="buscar"
        label="Buscar comprobante, pago o tipo"
        prepend-inner-icon="mdi-magnify"
        variant="outlined"
        clearable
        hide-details
      />
    </v-card>

    <v-card class="main-card">
      <v-data-table
        :headers="headers"
        :items="filtrados"
        :loading="loading"
      >
        <template #item.monto="{ item }">
          {{ moneda(item.monto) }}
        </template>

        <template #item.fechaEmision="{ item }">
          {{ fecha(item.fechaEmision) }}
        </template>

        <template #item.estado="{ item }">
          <v-chip
            size="small"
            :color="item.estado === 'EMITIDO' ? 'success' : 'error'"
            variant="tonal"
          >
            {{ item.estado }}
          </v-chip>
        </template>

        <template #item.urlDocumento="{ item }">
          <v-btn
            v-if="item.urlDocumento"
            :href="item.urlDocumento"
            target="_blank"
            icon="mdi-file-eye-outline"
            size="small"
            variant="text"
          />
          <span v-else>-</span>
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>

<script setup lang="ts">
const { pagos } = usePagosHttp()

const registros = ref<any[]>([])
const loading = ref(false)
const errorMsg = ref('')
const buscar = ref('')

const headers = [
  { title: 'Número', key: 'numeroComprobante' },
  { title: 'Pago', key: 'pagoId' },
  { title: 'Tipo', key: 'tipoComprobante' },
  { title: 'Monto', key: 'monto' },
  { title: 'Emisión', key: 'fechaEmision' },
  { title: 'Estado', key: 'estado' },
  { title: 'Documento', key: 'urlDocumento' },
]

const filtrados = computed(() => {
  const q = buscar.value.toLowerCase().trim()

  return registros.value.filter(
    x =>
      !q ||
      [
        x.numeroComprobante,
        x.pagoId,
        x.tipoComprobante,
        x.estado,
      ].some(v =>
        String(v || '').toLowerCase().includes(q)
      )
  )
})

async function cargar() {
  loading.value = true
  errorMsg.value = ''

  try {
    const data = await pagos('/api/comprobantes')
    registros.value = Array.isArray(data) ? data : []
  } catch (e: any) {
    errorMsg.value = e.message
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

onMounted(cargar)
</script>
