<template>
  <div>
    <OfflineMatriculaStatus @synced="cargar" />
    <div class="d-flex justify-space-between align-center mb-4">
      <v-btn color="primary" @click="abrirNuevo">
        <v-icon start>mdi-plus</v-icon>
        Nueva matrícula
      </v-btn>
    </div>

    <v-alert
      v-if="errorMsg"
      type="error"
      variant="tonal"
      class="mb-4"
      closable
      @click:close="errorMsg = ''"
    >
      {{ errorMsg }}
    </v-alert>

    <v-card class="main-card">
      <v-data-table
        :headers="headers"
        :items="matriculas"
        :loading="loading"
        item-value="id"
      >
        <template #item.estado="{ item }">
          <v-chip
            size="small"
            variant="tonal"
            :color="colorEstado(item.estado)"
            class="font-weight-medium"
          >
            <v-icon start size="16">
              {{ iconoEstado(item.estado) }}
            </v-icon>
            {{ textoEstado(item.estado) }}
          </v-chip>
        </template>

        <template #item.actions="{ item }">
          <div class="d-flex ga-1 justify-end">
            <v-btn
              icon="mdi-pencil"
              variant="text"
              size="small"
              @click="editar(item)"
            />
            <v-btn
              icon="mdi-delete"
              variant="text"
              size="small"
              color="error"
              @click="eliminar(item.id)"
            />
          </div>
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-model="dialog" max-width="650" persistent>
      <v-card class="pa-4">
        <DialogHeader
          :title="form.id ? 'Editar matrícula' : 'Nueva matrícula'"
          subtitle="Solo datos propios de la matrícula. Los catálogos se manejan en sus propios submódulos."
          @close="cerrarDialog"
        />

        <v-alert
          v-if="errorDialog"
          type="error"
          variant="tonal"
          class="mb-4"
          closable
          @click:close="errorDialog = ''"
        >
          {{ errorDialog }}
        </v-alert>

        <v-row>
          <v-col cols="12">
            <v-autocomplete
              :model-value="estudianteSeleccionado"
              v-model:search="busquedaEstudiante"
              :items="estudiantesEncontrados"
              :loading="buscandoEstudiantes"
              item-title="nombreCompleto"
              item-value="id"
              return-object
              no-filter
              clearable
              hide-no-data
              label="Buscar estudiante"
              placeholder="Escribe nombres o apellidos (mín. 2 letras)"
              prepend-inner-icon="mdi-account-search"
              variant="outlined"
              density="compact"
              @update:model-value="seleccionarEstudiante"
            >
              <template #item="{ props: itemProps, item }">
                <v-list-item
                  v-bind="itemProps"
                  :subtitle="detalleEstudiante(item.raw)"
                  prepend-icon="mdi-account-school"
                />
              </template>
            </v-autocomplete>

            <v-card
              v-if="estudianteSeleccionado"
              variant="tonal"
              color="primary"
              class="pa-3"
            >
              <div class="text-subtitle-2 font-weight-bold">
                {{ estudianteSeleccionado.nombreCompleto }}
              </div>
              <div class="text-body-2">
                {{ detalleEstudiante(estudianteSeleccionado) || 'Sin DNI ni código registrado' }}
              </div>
              <div
                v-if="estudianteSeleccionado.estado"
                class="text-caption"
              >
                Estado: {{ estudianteSeleccionado.estado }}
              </div>
            </v-card>
          </v-col>

          <v-col cols="12" md="4">
            <v-text-field
              v-model="form.grado"
              label="Grado"
              variant="outlined"
              density="compact"
            />
          </v-col>

          <v-col cols="12" md="4">
            <v-text-field
              v-model="form.seccion"
              label="Sección"
              variant="outlined"
              density="compact"
            />
          </v-col>

          <v-col cols="12" md="4">
            <v-text-field
              v-model="form.periodo"
              label="Periodo"
              variant="outlined"
              density="compact"
            />
          </v-col>

          <v-col cols="12">
            <v-select
              v-model="form.estado"
              :items="[
                { title: 'Pendiente', value: 'PENDIENTE' },
                { title: 'Confirmada', value: 'CONFIRMADA' },
                { title: 'Anulada', value: 'ANULADA' }
              ]"
              label="Estado"
              variant="outlined"
              density="compact"
            />
          </v-col>
        </v-row>

        <div class="d-flex justify-end ga-2 mt-2">
          <v-btn variant="text" @click="cerrarDialog">
            Cancelar
          </v-btn>
          <v-btn color="primary" :loading="saving" @click="guardar">
            Guardar
          </v-btn>
        </div>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { useMatriculaHttp } from '~/composables/useMatriculaHttp'
const { confirm: confirmAction } = useUiFeedback()

const { matricula } = useMatriculaHttp()
const offlineMatricula = useOfflineAttendance('matricula')

const loading = ref(false)
const saving = ref(false)
const dialog = ref(false)
const errorMsg = ref('')
const errorDialog = ref('')

const matriculas = ref<any[]>([])

const form = reactive<any>({
  id: null,
  estudianteId: '',
  nombreEstudiante: '',
  grado: '',
  seccion: '',
  periodo: '2026',
  estado: 'PENDIENTE',
})

// ---- Buscador dinámico de estudiantes ----
const busquedaEstudiante = ref('')
const estudiantesEncontrados = ref<any[]>([])
const buscandoEstudiantes = ref(false)
const estudianteSeleccionado = ref<any | null>(null)
let temporizadorBusqueda: ReturnType<typeof setTimeout> | null = null
let idBusqueda = 0

function nombreCompleto(e: any) {
  return [e?.nombres, e?.apellidoPaterno, e?.apellidoMaterno]
    .filter(Boolean)
    .join(' ')
    .trim()
}

function detalleEstudiante(e: any) {
  return [
    e?.dni ? `DNI: ${e.dni}` : '',
    e?.codigoEstudiante ? `Código: ${e.codigoEstudiante}` : '',
  ]
    .filter(Boolean)
    .join(' · ')
}

async function buscarEstudiantes(texto: string) {
  const actual = ++idBusqueda
  buscandoEstudiantes.value = true

  try {
    const data = await matricula(
      `/api/estudiantes/buscar?nombre=${encodeURIComponent(texto)}`
    )
    if (actual !== idBusqueda) return // llegó una respuesta vieja

    estudiantesEncontrados.value = (Array.isArray(data) ? data : []).map(
      (e: any) => ({ ...e, nombreCompleto: nombreCompleto(e) })
    )
  } catch (error: any) {
    if (actual !== idBusqueda) return
    estudiantesEncontrados.value = []
    errorDialog.value = error.message || 'Error al buscar estudiantes'
  } finally {
    if (actual === idBusqueda) buscandoEstudiantes.value = false
  }
}

watch(busquedaEstudiante, (texto) => {
  if (temporizadorBusqueda) clearTimeout(temporizadorBusqueda)

  const valor = (texto ?? '').trim()

  // Al seleccionar, Vuetify pone el título del item en el campo de búsqueda: no volver a buscar.
  if (valor === estudianteSeleccionado.value?.nombreCompleto) return

  if (valor.length < 2) {
    idBusqueda++
    buscandoEstudiantes.value = false
    estudiantesEncontrados.value = estudianteSeleccionado.value
      ? [estudianteSeleccionado.value]
      : []
    return
  }

  temporizadorBusqueda = setTimeout(() => buscarEstudiantes(valor), 350)
})

function seleccionarEstudiante(estudiante: any | null) {
  estudianteSeleccionado.value = estudiante
  form.estudianteId = estudiante?.id ?? ''
  form.nombreEstudiante = estudiante?.nombreCompleto ?? ''
}

function limpiarBuscador() {
  if (temporizadorBusqueda) clearTimeout(temporizadorBusqueda)
  idBusqueda++
  buscandoEstudiantes.value = false
  busquedaEstudiante.value = ''
  estudianteSeleccionado.value = null
  estudiantesEncontrados.value = []
}

async function precargarEstudiante(id: string, nombre: string) {
  // Muestra de inmediato lo que ya trae la matrícula y luego completa con el detalle.
  const base = { id, nombreCompleto: nombre || id }
  estudianteSeleccionado.value = base
  estudiantesEncontrados.value = [base]

  try {
    const e = await matricula(`/api/estudiantes/${id}`)
    if (estudianteSeleccionado.value?.id !== id || !e) return

    const completo = { ...e, nombreCompleto: nombreCompleto(e) || base.nombreCompleto }
    estudianteSeleccionado.value = completo
    estudiantesEncontrados.value = [completo]
  } catch {
    // Si el estudiante ya no existe se mantiene el nombre guardado en la matrícula.
  }
}

const headers = [
  { title: 'Estudiante', key: 'nombreEstudiante' },
  { title: 'Grado', key: 'grado' },
  { title: 'Sección', key: 'seccion' },
  { title: 'Periodo', key: 'periodo' },
  { title: 'Estado', key: 'estado' },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'end' as const },
]

function colorEstado(estado: string) {
  switch (estado) {
    case 'CONFIRMADA':
      return 'success'
    case 'PENDIENTE':
      return 'warning'
    case 'ANULADA':
      return 'error'
    case 'REGISTRADA':
      return 'info'
    default:
      return 'grey'
  }
}

function iconoEstado(estado: string) {
  switch (estado) {
    case 'CONFIRMADA':
      return 'mdi-check-circle'
    case 'PENDIENTE':
      return 'mdi-clock-outline'
    case 'ANULADA':
      return 'mdi-close-circle'
    case 'REGISTRADA':
      return 'mdi-file-document-edit-outline'
    default:
      return 'mdi-help-circle-outline'
  }
}

function textoEstado(estado: string) {
  switch (estado) {
    case 'CONFIRMADA':
      return 'Confirmada'
    case 'PENDIENTE':
      return 'Pendiente'
    case 'ANULADA':
      return 'Anulada'
    case 'REGISTRADA':
      return 'Registrada'
    default:
      return estado
  }
}

async function cargar() {
  loading.value = true
  errorMsg.value = ''

  try {
    const data = await matricula('/api/matriculas')

    matriculas.value = Array.isArray(data?.matriculas) ? data.matriculas : []
  } catch (error: any) {
    errorMsg.value = error.message || 'Error al cargar matrículas'
    matriculas.value = []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    id: null,
    estudianteId: '',
    nombreEstudiante: '',
    grado: '',
    seccion: '',
    periodo: '2026',
    estado: 'PENDIENTE',
  })
  errorDialog.value = ''
  limpiarBuscador()
}

function abrirNuevo() {
  resetForm()
  dialog.value = true
}

function editar(item: any) {
  Object.assign(form, {
    id: item.id ?? null,
    estudianteId: item.estudianteId ?? '',
    nombreEstudiante: item.nombreEstudiante ?? '',
    grado: item.grado ?? '',
    seccion: item.seccion ?? '',
    periodo: item.periodo ?? '2026',
    estado: item.estado ?? 'PENDIENTE',
  })
  limpiarBuscador()
  if (form.estudianteId) {
    precargarEstudiante(form.estudianteId, form.nombreEstudiante)
  }
  dialog.value = true
}

function cerrarDialog() {
  dialog.value = false
  resetForm()
}

async function guardar() {
  errorDialog.value = ''

  if (!form.estudianteId) {
    errorDialog.value = 'Selecciona un estudiante desde el buscador'
    return
  }

  saving.value = true

  try {
    const payload = {
      estudianteId: form.estudianteId,
      nombreEstudiante: form.nombreEstudiante,
      grado: form.grado,
      seccion: form.seccion,
      periodo: form.periodo,
      estado: form.estado,
    }

    if (form.id) {
      await offlineMatricula.enqueue(`/api/matriculas/${form.id}`, 'PUT', JSON.stringify(payload))
    } else {
      await offlineMatricula.enqueue('/api/matriculas', 'POST', JSON.stringify(payload))
    }

    cerrarDialog()

  } catch (error: any) {
    errorDialog.value = error.message || 'Error al guardar matrícula'
  } finally {
    saving.value = false
  }
}

async function eliminar(id: string) {
  if (!await confirmAction('¿Eliminar esta matrícula?')) return

  try {
    await offlineMatricula.enqueue(`/api/matriculas/${id}`, 'DELETE')


  } catch (error: any) {
    errorMsg.value = error.message || 'Error al eliminar matrícula'
  }
}

onMounted(() => {
  cargar()
})
</script>