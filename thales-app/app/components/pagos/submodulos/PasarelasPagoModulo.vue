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
        <template #item.activo="{ item }">
          <v-chip
            size="small"
            variant="tonal"
            :color="item.activo ? 'success' : 'default'"
          >
            {{ item.activo ? 'ACTIVO' : 'INACTIVO' }}
          </v-chip>
        </template>

        <template #item.modo="{ item }">
          <v-chip size="small" variant="tonal">
            {{ item.modo }}
          </v-chip>
        </template>

        <template #item.fechaRegistro="{ item }">
          {{ fecha(item.fechaRegistro) }}
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
  { title: 'Nombre', key: 'nombre' },
  { title: 'Proveedor', key: 'proveedor' },
  { title: 'Modo', key: 'modo' },
  { title: 'Estado', key: 'activo' },
  { title: 'Registro', key: 'fechaRegistro' },
]

async function cargar() {
  loading.value = true
  errorMsg.value = ''

  try {
    const data = await pagos('/api/pasarelas')
    registros.value = Array.isArray(data) ? data : []
  } catch (e: any) {
    errorMsg.value =
      e.message || 'No se pudieron cargar las pasarelas.'
  } finally {
    loading.value = false
  }
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
