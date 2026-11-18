<template>
  <div>
    <div class="d-flex justify-space-between align-center mb-4 flex-wrap ga-3">
      <div class="d-flex ga-2">
        <v-btn variant="tonal" color="primary" :loading="loading" @click="cargar">
          <v-icon start>mdi-refresh</v-icon>Actualizar
        </v-btn>
        <v-btn color="primary" @click="nuevo">
          <v-icon start>mdi-file-plus-outline</v-icon>Solicitar reporte
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
        <v-col cols="12" md="8">
          <v-text-field
            v-model="buscar"
            label="Buscar por tipo, usuario, formato o estado"
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            clearable
            hide-details
          />
        </v-col>
        <v-col cols="12" md="4">
          <v-select
            v-model="estadoFiltro"
            :items="['TODOS','SOLICITADO','GENERANDO','LISTO','ERROR']"
            label="Estado"
            variant="outlined"
            density="compact"
            hide-details
          />
        </v-col>
      </v-row>
    </v-card>

    <v-card class="main-card">
      <v-data-table :headers="headers" :items="filtrados" :loading="loading">
        <template #item.estado="{ item }">
          <v-chip size="small" :color="colorEstado(item.estado)">
            {{ item.estado }}
          </v-chip>
        </template>
        <template #item.fechaSolicitud="{ item }">
          {{ fecha(item.fechaSolicitud) }}
        </template>
        <template #item.fechaGeneracion="{ item }">
          {{ fecha(item.fechaGeneracion) }}
        </template>
        <template #item.rutaArchivo="{ item }">
          <span class="text-caption">{{ item.rutaArchivo || '-' }}</span>
        </template>
        <template #item.actions="{ item }">
          <v-btn
            icon="mdi-delete"
            size="small"
            variant="text"
            color="error"
            @click="eliminar(item)"
          />
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-model="dialog" max-width="700">
      <v-card class="pa-5">
        <v-card-title>Solicitar reporte</v-card-title>
        <v-card-text>
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="form.tipoReporte"
                label="Tipo de reporte *"
                placeholder="PAGOS_MENSUAL"
                variant="outlined"
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-select
                v-model="form.formato"
                :items="['PDF','EXCEL','CSV']"
                label="Formato *"
                variant="outlined"
              />
            </v-col>
            <v-col cols="12">
              <v-text-field
                v-model="form.usuarioSolicitanteId"
                label="Usuario solicitante ID *"
                variant="outlined"
              />
            </v-col>
            <v-col cols="12">
              <v-textarea
                v-model="parametrosJson"
                label="Parámetros JSON"
                variant="outlined"
                rows="5"
                placeholder='{"periodo":"2026-08"}'
              />
            </v-col>
          </v-row>
        </v-card-text>
        <v-card-actions class="justify-end">
          <v-btn variant="text" @click="dialog=false">Cancelar</v-btn>
          <v-btn color="primary" :loading="saving" @click="solicitar">Solicitar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
const { confirm: confirmAction } = useUiFeedback()

const { reportes } = useReportesHttp()
const authStore = useAuthStore()

const loading = ref(false)
const saving = ref(false)
const dialog = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const buscar = ref('')
const estadoFiltro = ref('TODOS')
const registros = ref<any[]>([])
const parametrosJson = ref('{}')

const form = reactive<any>({
  tipoReporte: '',
  usuarioSolicitanteId: '',
  formato: 'PDF',
})

const headers = [
  { title: 'Tipo', key: 'tipoReporte' },
  { title: 'Usuario', key: 'usuarioSolicitanteId' },
  { title: 'Formato', key: 'formato' },
  { title: 'Estado', key: 'estado' },
  { title: 'Solicitud', key: 'fechaSolicitud' },
  { title: 'Generación', key: 'fechaGeneracion' },
  { title: 'Ruta archivo', key: 'rutaArchivo' },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'end' as const },
]

const filtrados = computed(() => {
  const q = buscar.value.toLowerCase().trim()

  return registros.value
    .filter(x =>
      (!q || [x.tipoReporte, x.usuarioSolicitanteId, x.formato, x.estado]
        .some(v => String(v || '').toLowerCase().includes(q))) &&
      (estadoFiltro.value === 'TODOS' || x.estado === estadoFiltro.value)
    )
    .sort((a, b) =>
      new Date(b.fechaSolicitud || 0).getTime() - new Date(a.fechaSolicitud || 0).getTime()
    )
})

function nuevo() {
  authStore.restoreSession()

  form.tipoReporte = ''
  form.formato = 'PDF'
  form.usuarioSolicitanteId = String(authStore.user?.id ?? authStore.user?.username ?? '')
  parametrosJson.value = '{}'
  dialog.value = true
}

async function cargar() {
  loading.value = true
  errorMsg.value = ''

  try {
    const data = await reportes('/api/reportes-generados')
    registros.value = Array.isArray(data) ? data : []
  } catch (e: any) {
    errorMsg.value = e.message
  } finally {
    loading.value = false
  }
}

async function solicitar() {
  if (!form.tipoReporte || !form.usuarioSolicitanteId || !form.formato) {
    errorMsg.value = 'Tipo de reporte, usuario y formato son obligatorios.'
    return
  }

  let parametros: Record<string, any> = {}

  try {
    parametros = parametrosJson.value.trim()
      ? JSON.parse(parametrosJson.value)
      : {}
  } catch {
    errorMsg.value = 'Los parámetros deben ser un JSON válido.'
    return
  }

  saving.value = true
  errorMsg.value = ''

  try {
    await reportes('/api/reportes-generados/solicitar', {
      method: 'POST',
      body: JSON.stringify({
        tipoReporte: form.tipoReporte,
        usuarioSolicitanteId: form.usuarioSolicitanteId,
        parametros,
        formato: form.formato,
      }),
    })

    dialog.value = false
    successMsg.value = 'Solicitud de reporte registrada.'
    await cargar()
  } catch (e: any) {
    errorMsg.value = e.message
  } finally {
    saving.value = false
  }
}

async function eliminar(item: any) {
  if (!await confirmAction(`¿Eliminar el registro del reporte ${item.tipoReporte}?`)) return

  try {
    await reportes(`/api/reportes-generados/${item.id}`, { method: 'DELETE' })
    successMsg.value = 'Reporte eliminado.'
    await cargar()
  } catch (e: any) {
    errorMsg.value = e.message
  }
}

function fecha(valor: any) {
  return valor ? new Date(valor).toLocaleString('es-PE') : '-'
}

function colorEstado(estado: string) {
  if (estado === 'LISTO') return 'success'
  if (estado === 'ERROR') return 'error'
  if (estado === 'GENERANDO') return 'warning'
  return 'primary'
}

onMounted(cargar)
</script>
