<template>
  <div>
    <div class="d-flex justify-space-between align-center mb-4 flex-wrap ga-3">
      <div class="d-flex ga-2">
        <v-btn variant="tonal" color="primary" :loading="loading" @click="cargar">
          <v-icon start>mdi-refresh</v-icon>Actualizar
        </v-btn>
        <v-btn color="primary" @click="nuevo">
          <v-icon start>mdi-plus</v-icon>Nuevo indicador
        </v-btn>
      </div>
    </div>

    <v-alert v-if="errorMsg" type="error" variant="tonal" closable class="mb-4" @click:close="errorMsg=''">
      {{ errorMsg }}
    </v-alert>
    <v-alert v-if="successMsg" type="success" variant="tonal" closable class="mb-4" @click:close="successMsg=''">
      {{ successMsg }}
    </v-alert>

    <v-card class="main-card pa-4 mb-4">
      <v-row>
        <v-col cols="12" md="7">
          <v-text-field
            v-model="buscar"
            label="Buscar por código, nombre, tipo o periodo"
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            clearable
            hide-details
          />
        </v-col>
        <v-col cols="12" md="5">
          <v-select
            v-model="tipoFiltro"
            :items="tiposFiltro"
            label="Tipo"
            variant="outlined"
            density="compact"
            hide-details
          />
        </v-col>
      </v-row>
    </v-card>

    <v-card class="main-card">
      <v-data-table :headers="headers" :items="filtrados" :loading="loading">
        <template #item.valor="{ item }">
          <strong>{{ item.valor }}</strong>
        </template>
        <template #item.fechaCalculo="{ item }">
          {{ fecha(item.fechaCalculo) }}
        </template>
        <template #item.actions="{ item }">
          <div class="d-flex justify-end ga-1">
            <v-btn icon="mdi-pencil" size="small" variant="text" @click="editar(item)" />
            <v-btn icon="mdi-delete" size="small" variant="text" color="error" @click="eliminar(item)" />
          </div>
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-model="dialog" max-width="720">
      <v-card class="pa-5">
        <v-card-title>{{ form.id ? 'Editar indicador' : 'Nuevo indicador' }}</v-card-title>
        <v-card-text>
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.codigo" label="Código *" variant="outlined" />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.nombre" label="Nombre *" variant="outlined" />
            </v-col>
            <v-col cols="12" md="6">
              <v-select
                v-model="form.tipo"
                :items="['DISPONIBILIDAD','RENDIMIENTO','USO','NEGOCIO']"
                label="Tipo *"
                variant="outlined"
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field v-model.number="form.valor" type="number" label="Valor *" variant="outlined" />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.periodo" label="Periodo *" placeholder="2026-08" variant="outlined" />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.fechaCalculo" type="datetime-local" label="Fecha de cálculo" variant="outlined" />
            </v-col>
          </v-row>
        </v-card-text>
        <v-card-actions class="justify-end">
          <v-btn variant="text" @click="dialog=false">Cancelar</v-btn>
          <v-btn color="primary" :loading="saving" @click="guardar">Guardar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
const { confirm: confirmAction } = useUiFeedback()
const { reportes } = useReportesHttp()

const loading = ref(false)
const saving = ref(false)
const dialog = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const buscar = ref('')
const tipoFiltro = ref('TODOS')
const registros = ref<any[]>([])

const tiposFiltro = ['TODOS', 'DISPONIBILIDAD', 'RENDIMIENTO', 'USO', 'NEGOCIO']

const form = reactive<any>({
  id: null,
  codigo: '',
  nombre: '',
  tipo: 'RENDIMIENTO',
  valor: null,
  periodo: '',
  fechaCalculo: '',
})

const headers = [
  { title: 'Código', key: 'codigo' },
  { title: 'Nombre', key: 'nombre' },
  { title: 'Tipo', key: 'tipo' },
  { title: 'Valor', key: 'valor' },
  { title: 'Periodo', key: 'periodo' },
  { title: 'Fecha cálculo', key: 'fechaCalculo' },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'end' as const },
]

const filtrados = computed(() => {
  const q = buscar.value.toLowerCase().trim()

  return registros.value.filter(x =>
    (!q || [x.codigo, x.nombre, x.tipo, x.periodo]
      .some(v => String(v || '').toLowerCase().includes(q))) &&
    (tipoFiltro.value === 'TODOS' || x.tipo === tipoFiltro.value)
  )
})

function reset() {
  Object.assign(form, {
    id: null,
    codigo: '',
    nombre: '',
    tipo: 'RENDIMIENTO',
    valor: null,
    periodo: '',
    fechaCalculo: new Date().toISOString().slice(0, 16),
  })
}

function nuevo() {
  reset()
  dialog.value = true
}

function editar(item: any) {
  Object.assign(form, item, {
    fechaCalculo: String(item.fechaCalculo || '').slice(0, 16),
  })
  dialog.value = true
}

async function cargar() {
  loading.value = true
  errorMsg.value = ''

  try {
    const data = await reportes('/api/indicadores')
    registros.value = Array.isArray(data) ? data : []
  } catch (e: any) {
    errorMsg.value = e.message
  } finally {
    loading.value = false
  }
}

async function guardar() {
  if (!form.codigo || !form.nombre || !form.tipo || form.valor === null || !form.periodo) {
    errorMsg.value = 'Código, nombre, tipo, valor y periodo son obligatorios.'
    return
  }

  saving.value = true
  errorMsg.value = ''

  try {
    const payload = {
      codigo: form.codigo,
      nombre: form.nombre,
      tipo: form.tipo,
      valor: Number(form.valor),
      periodo: form.periodo,
      fechaCalculo: form.fechaCalculo || null,
    }

    await reportes(
      form.id ? `/api/indicadores/${form.id}` : '/api/indicadores',
      {
        method: form.id ? 'PUT' : 'POST',
        body: JSON.stringify(payload),
      }
    )

    dialog.value = false
    successMsg.value = form.id ? 'Indicador actualizado.' : 'Indicador registrado.'
    await cargar()
  } catch (e: any) {
    errorMsg.value = e.message
  } finally {
    saving.value = false
  }
}

async function eliminar(item: any) {
  if (!await confirmAction(`¿Eliminar el indicador ${item.codigo}?`)) return

  try {
    await reportes(`/api/indicadores/${item.id}`, { method: 'DELETE' })
    successMsg.value = 'Indicador eliminado.'
    await cargar()
  } catch (e: any) {
    errorMsg.value = e.message
  }
}

function fecha(valor: any) {
  return valor ? new Date(valor).toLocaleString('es-PE') : '-'
}

onMounted(cargar)
</script>
