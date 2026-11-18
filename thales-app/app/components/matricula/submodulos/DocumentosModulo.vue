<template>
  <div>
    <div class="d-flex justify-space-between align-center mb-4 flex-wrap ga-3">
      <div class="d-flex ga-2 flex-wrap">
        <v-btn variant="tonal" color="primary" :loading="loading" @click="cargar">
          <v-icon start>mdi-refresh</v-icon>
          Actualizar
        </v-btn>
        <v-btn color="primary" @click="abrirNuevo">
          <v-icon start>mdi-plus</v-icon>
          Nuevo documento
        </v-btn>
      </div>
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

    <v-alert
      v-if="successMsg"
      type="success"
      variant="tonal"
      class="mb-4"
      closable
      @click:close="successMsg = ''"
    >
      {{ successMsg }}
    </v-alert>

    <v-card class="main-card pa-4 mb-4">
      <v-row align="center">
        <v-col cols="12" md="4">
          <v-text-field
            v-model="busqueda"
            label="Buscar por estudiante, documento o archivo"
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            clearable
            hide-details
          />
        </v-col>
        <v-col cols="12" md="4">
          <v-select
            v-model="filtroMatricula"
            :items="matriculasFiltro"
            item-title="title"
            item-value="value"
            label="Matrícula"
            variant="outlined"
            density="compact"
            hide-details
          />
        </v-col>
        <v-col cols="12" md="2">
          <v-select
            v-model="filtroTipo"
            :items="['TODOS', ...tiposDocumento]"
            label="Tipo"
            variant="outlined"
            density="compact"
            hide-details
          />
        </v-col>
        <v-col cols="12" md="2">
          <v-select
            v-model="filtroEstado"
            :items="['TODOS', ...estados]"
            label="Estado"
            variant="outlined"
            density="compact"
            hide-details
          />
        </v-col>
      </v-row>
    </v-card>

    <v-row class="mb-4">
      <v-col cols="12" md="3">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Total documentos</p>
          <p class="text-h5 font-weight-bold mb-0">{{ documentosFiltrados.length }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="3">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Pendientes</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalPendientes }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="3">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Verificados</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalVerificados }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="3">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Obligatorios</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalObligatorios }}</p>
        </v-card>
      </v-col>
    </v-row>

    <v-card class="main-card">
      <v-data-table
        :headers="headers"
        :items="documentosFiltrados"
        :loading="loading"
        item-value="id"
        no-data-text="No hay documentos registrados"
        loading-text="Cargando documentos..."
      >
        <template #item.matriculaId="{ item }">
          <div>
            <p class="font-weight-bold mb-0">{{ nombreEstudiante(item.matriculaId) }}</p>
            <p class="text-caption text-grey-darken-1 mb-0">
              {{ resumenMatricula(item.matriculaId) }}
            </p>
          </div>
        </template>
        <template #item.tipoDocumento="{ item }">
          <v-chip size="small" color="primary" variant="tonal">
            {{ textoTipoDocumento(item.tipoDocumento) }}
          </v-chip>
        </template>
        <template #item.nombreArchivo="{ item }">
          <div>
            <p class="font-weight-bold mb-0">{{ item.nombreArchivo || 'Sin archivo' }}</p>
            <p class="text-caption text-grey-darken-1 mb-0">
              {{ item.mimeType || 'Sin tipo MIME' }}
            </p>
          </div>
        </template>
        <template #item.esObligatorio="{ item }">
          <v-chip
            size="small"
            :color="item.esObligatorio ? 'warning' : 'default'"
            variant="tonal"
          >
            {{ item.esObligatorio ? 'Sí' : 'No' }}
          </v-chip>
        </template>
        <template #item.estado="{ item }">
          <v-chip size="small" :color="colorEstado(item.estado)" variant="tonal">
            {{ item.estado || 'SIN ESTADO' }}
          </v-chip>
        </template>
        <template #item.actions="{ item }">
          <div class="d-flex ga-1 justify-end">
            <v-btn
              icon="mdi-eye-outline"
              variant="text"
              size="small"
              @click="verDetalle(item)"
            />
            <v-btn
              v-if="item.urlAcceso"
              icon="mdi-open-in-new"
              variant="text"
              size="small"
              :href="item.urlAcceso"
              target="_blank"
            />
            <v-btn
              icon="mdi-pencil"
              variant="text"
              size="small"
              @click="editar(item)"
            />
            <v-btn
              icon="mdi-delete"
              variant="text"
              color="error"
              size="small"
              @click="eliminar(item.id)"
            />
          </div>
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-model="dialog" max-width="1050" persistent>
      <v-card class="pa-5">
        <DialogHeader
          :title="form.id ? 'Editar documento' : 'Nuevo documento'"
          subtitle="Registre la información y ubicación del archivo de matrícula."
          @close="cerrarDialog"
        />

        <v-form ref="formRef">
          <v-card variant="outlined" class="pa-4 mb-4">
            <h3 class="text-subtitle-1 font-weight-bold mb-4">Datos del documento</h3>
            <v-row>
              <v-col cols="12" md="7">
                <v-select
                  v-model="form.matriculaId"
                  :items="matriculasSelect"
                  item-title="title"
                  item-value="value"
                  label="Matrícula"
                  variant="outlined"
                  density="compact"
                  :loading="loadingCatalogos"
                  :disabled="Boolean(form.id)"
                  :rules="[rules.required]"
                />
              </v-col>
              <v-col cols="12" md="5">
                <v-select
                  v-model="form.tipoDocumento"
                  :items="tiposDocumentoSelect"
                  item-title="title"
                  item-value="value"
                  label="Tipo de documento"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.required]"
                />
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model.trim="form.nombreArchivo"
                  label="Nombre del archivo"
                  variant="outlined"
                  density="compact"
                  maxlength="180"
                  counter="180"
                  :rules="[rules.required]"
                />
              </v-col>
              <v-col cols="12" md="3">
                <v-text-field
                  v-model.trim="form.mimeType"
                  label="Tipo MIME"
                  variant="outlined"
                  density="compact"
                  hint="Ejemplo: application/pdf"
                />
              </v-col>
              <v-col cols="12" md="3">
                <v-text-field
                  v-model.number="form.tamañoBytes"
                  label="Tamaño en bytes"
                  type="number"
                  min="0"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.noNegativo]"
                />
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model.trim="form.rutaAlmacenamiento"
                  label="Ruta de almacenamiento"
                  variant="outlined"
                  density="compact"
                />
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model.trim="form.urlAcceso"
                  label="URL de acceso"
                  variant="outlined"
                  density="compact"
                  type="url"
                />
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model="form.fechaEmision"
                  label="Fecha de emisión"
                  type="date"
                  variant="outlined"
                  density="compact"
                />
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model="form.fechaVencimiento"
                  label="Fecha de vencimiento"
                  type="date"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.fechaVencimiento]"
                />
              </v-col>
              <v-col cols="12" md="4">
                <v-switch
                  v-model="form.esObligatorio"
                  label="Documento obligatorio"
                  color="primary"
                  hide-details
                />
              </v-col>
            </v-row>
          </v-card>

          <v-card variant="outlined" class="pa-4 mb-4">
            <h3 class="text-subtitle-1 font-weight-bold mb-4">Verificación</h3>
            <v-row>
              <v-col cols="12" md="4">
                <v-select
                  v-model="form.estado"
                  :items="estados"
                  label="Estado"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.required]"
                />
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model.trim="form.usuarioVerificacion"
                  label="Usuario de verificación"
                  variant="outlined"
                  density="compact"
                />
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model="form.fechaVerificacion"
                  label="Fecha de verificación"
                  type="datetime-local"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.fechaVerificacion]"
                />
              </v-col>
              <v-col cols="12">
                <v-textarea
                  v-model.trim="form.observaciones"
                  label="Observaciones"
                  variant="outlined"
                  density="compact"
                  rows="3"
                  maxlength="500"
                  counter="500"
                />
              </v-col>
            </v-row>
          </v-card>
        </v-form>

        <div class="d-flex justify-end ga-2 mt-3">
          <v-btn variant="text" @click="cerrarDialog">Cancelar</v-btn>
          <v-btn color="primary" :loading="saving" @click="guardar">Guardar</v-btn>
        </div>
      </v-card>
    </v-dialog>

    <v-dialog v-model="dialogDetalle" max-width="820">
      <v-card class="pa-5">
        <DialogHeader
          title="Detalle del documento"
          subtitle="Información del archivo y su proceso de verificación."
          @close="dialogDetalle = false"
        />

        <v-row v-if="documentoSeleccionado">
          <v-col cols="12">
            <p class="text-caption text-grey-darken-1 mb-1">Estudiante</p>
            <p class="font-weight-bold mb-0">
              {{ nombreEstudiante(documentoSeleccionado.matriculaId) }}
            </p>
            <p class="text-caption text-grey-darken-1">
              {{ resumenMatricula(documentoSeleccionado.matriculaId) }}
            </p>
          </v-col>
          <v-col cols="12" md="5">
            <p class="text-caption text-grey-darken-1 mb-1">Tipo</p>
            <v-chip size="small" color="primary" variant="tonal">
              {{ textoTipoDocumento(documentoSeleccionado.tipoDocumento) }}
            </v-chip>
          </v-col>
          <v-col cols="12" md="7">
            <p class="text-caption text-grey-darken-1 mb-1">Archivo</p>
            <p class="font-weight-bold">
              {{ documentoSeleccionado.nombreArchivo || '-' }}
            </p>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Tipo MIME</p>
            <p>{{ documentoSeleccionado.mimeType || '-' }}</p>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Tamaño</p>
            <p>{{ formatearTamaño(documentoSeleccionado.tamañoBytes) }}</p>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Obligatorio</p>
            <p>{{ documentoSeleccionado.esObligatorio ? 'Sí' : 'No' }}</p>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Fecha de emisión</p>
            <p>{{ formatearFecha(documentoSeleccionado.fechaEmision) }}</p>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Vencimiento</p>
            <p>{{ formatearFecha(documentoSeleccionado.fechaVencimiento) }}</p>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Estado</p>
            <v-chip
              size="small"
              :color="colorEstado(documentoSeleccionado.estado)"
              variant="tonal"
            >
              {{ documentoSeleccionado.estado || 'SIN ESTADO' }}
            </v-chip>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Verificado por</p>
            <p>{{ documentoSeleccionado.usuarioVerificacion || '-' }}</p>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Fecha de verificación</p>
            <p>{{ formatearFechaHora(documentoSeleccionado.fechaVerificacion) }}</p>
          </v-col>
          <v-col cols="12">
            <p class="text-caption text-grey-darken-1 mb-1">Observaciones</p>
            <p>{{ documentoSeleccionado.observaciones || '-' }}</p>
          </v-col>
          <v-col cols="12" v-if="documentoSeleccionado.urlAcceso">
            <v-btn
              color="primary"
              variant="tonal"
              :href="documentoSeleccionado.urlAcceso"
              target="_blank"
            >
              <v-icon start>mdi-open-in-new</v-icon>
              Abrir documento
            </v-btn>
          </v-col>
        </v-row>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { useMatriculaHttp } from '~/composables/useMatriculaHttp'
const { confirm: confirmAction } = useUiFeedback()

interface Matricula {
  id: string
  estudianteId: string
  nombreEstudiante: string
  grado: string
  seccion: string
  periodo: string
  estado: string
}

interface DocumentoMatricula {
  id: string | null
  matriculaId: string
  tipoDocumento: string
  nombreArchivo: string
  rutaAlmacenamiento: string
  urlAcceso: string
  mimeType: string
  tamañoBytes: number | null
  fechaEmision: string
  fechaVencimiento: string
  esObligatorio: boolean
  estado: string
  observaciones: string
  usuarioVerificacion: string
  fechaVerificacion: string
  usuarioRegistro?: string
  fechaRegistro?: string
  fechaActualizacion?: string
}

const { matricula } = useMatriculaHttp()

const loading = ref(false)
const loadingCatalogos = ref(false)
const saving = ref(false)
const dialog = ref(false)
const dialogDetalle = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const formRef = ref()
const busqueda = ref('')
const filtroMatricula = ref('TODOS')
const filtroTipo = ref('TODOS')
const filtroEstado = ref('TODOS')
const documentoSeleccionado = ref<DocumentoMatricula | null>(null)

const documentos = ref<DocumentoMatricula[]>([])
const matriculas = ref<Matricula[]>([])

const tiposDocumento = [
  'DNI',
  'PARTIDA_NACIMIENTO',
  'LIBRETA_NOTAS',
  'FOTO',
  'FICHA_SALUD',
  'CONSTANCIA_TRASLADO',
  'OTRO',
]

const estados = ['PENDIENTE', 'ENTREGADO', 'VERIFICADO', 'RECHAZADO']

const tiposDocumentoSelect = [
  { title: 'DNI', value: 'DNI' },
  { title: 'Partida de nacimiento', value: 'PARTIDA_NACIMIENTO' },
  { title: 'Libreta de notas', value: 'LIBRETA_NOTAS' },
  { title: 'Fotografía', value: 'FOTO' },
  { title: 'Ficha de salud', value: 'FICHA_SALUD' },
  { title: 'Constancia de traslado', value: 'CONSTANCIA_TRASLADO' },
  { title: 'Otro', value: 'OTRO' },
]

const headers = [
  { title: 'Matrícula', key: 'matriculaId' },
  { title: 'Tipo', key: 'tipoDocumento' },
  { title: 'Archivo', key: 'nombreArchivo' },
  { title: 'Obligatorio', key: 'esObligatorio' },
  { title: 'Estado', key: 'estado' },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'end' as const },
]

const rules = {
  required: (value: unknown) => !!String(value || '').trim() || 'Campo requerido',
  noNegativo: (value: number | null) =>
    value === null || value === undefined || Number(value) >= 0 || 'No puede ser negativo',
  fechaVencimiento: (value: string) =>
    !value || !form.fechaEmision || value >= form.fechaEmision ||
    'El vencimiento no puede ser anterior a la emisión',
  fechaVerificacion: (value: string) => {
    if (form.estado === 'VERIFICADO' && !value) {
      return 'Debe indicar la fecha de verificación cuando el documento está verificado'
    }
    return true
  },
}

const form = reactive<DocumentoMatricula>(crearFormVacio())

const matriculasSelect = computed(() =>
  matriculas.value.map((item) => ({
    title: `${item.nombreEstudiante || 'Sin nombre'} - ${item.grado || '-'} ${item.seccion || ''} (${item.periodo || '-'})`,
    value: item.id,
  }))
)

const matriculasFiltro = computed(() => [
  { title: 'TODOS', value: 'TODOS' },
  ...matriculasSelect.value,
])

const documentosFiltrados = computed(() => {
  const texto = (busqueda.value || '').toLowerCase().trim()

  return documentos.value.filter((documento) => {
    const coincideTexto =
      !texto ||
      [
        documento.nombreArchivo,
        documento.tipoDocumento,
        documento.mimeType,
        nombreEstudiante(documento.matriculaId),
        resumenMatricula(documento.matriculaId),
      ].some((valor) => String(valor || '').toLowerCase().includes(texto))

    const coincideMatricula =
      filtroMatricula.value === 'TODOS' || documento.matriculaId === filtroMatricula.value

    const coincideTipo =
      filtroTipo.value === 'TODOS' || documento.tipoDocumento === filtroTipo.value

    const coincideEstado =
      filtroEstado.value === 'TODOS' || documento.estado === filtroEstado.value

    return coincideTexto && coincideMatricula && coincideTipo && coincideEstado
  })
})

const totalPendientes = computed(() =>
  documentosFiltrados.value.filter((documento) => documento.estado === 'PENDIENTE').length
)

const totalVerificados = computed(() =>
  documentosFiltrados.value.filter((documento) => documento.estado === 'VERIFICADO').length
)

const totalObligatorios = computed(() =>
  documentosFiltrados.value.filter((documento) => documento.esObligatorio).length
)

function crearFormVacio(): DocumentoMatricula {
  return {
    id: null,
    matriculaId: '',
    tipoDocumento: 'DNI',
    nombreArchivo: '',
    rutaAlmacenamiento: '',
    urlAcceso: '',
    mimeType: '',
    tamañoBytes: 0,
    fechaEmision: '',
    fechaVencimiento: '',
    esObligatorio: true,
    estado: 'PENDIENTE',
    observaciones: '',
    usuarioVerificacion: '',
    fechaVerificacion: '',
  }
}

function limpiarTexto(value: string | null | undefined) {
  const limpio = String(value ?? '').trim()
  return limpio || null
}

function normalizarDocumento(item: any): DocumentoMatricula {
  return {
    id: item.id ?? null,
    matriculaId: item.matriculaId ?? '',
    tipoDocumento: item.tipoDocumento ?? 'DNI',
    nombreArchivo: item.nombreArchivo ?? '',
    rutaAlmacenamiento: item.rutaAlmacenamiento ?? '',
    urlAcceso: item.urlAcceso ?? '',
    mimeType: item.mimeType ?? '',
    tamañoBytes: item.tamañoBytes ?? 0,
    fechaEmision: item.fechaEmision ?? '',
    fechaVencimiento: item.fechaVencimiento ?? '',
    esObligatorio: !!item.esObligatorio,
    estado: item.estado ?? 'PENDIENTE',
    observaciones: item.observaciones ?? '',
    usuarioVerificacion: item.usuarioVerificacion ?? '',
    fechaVerificacion: item.fechaVerificacion ?? '',
    usuarioRegistro: item.usuarioRegistro,
    fechaRegistro: item.fechaRegistro,
    fechaActualizacion: item.fechaActualizacion,
  }
}

function construirPayload() {
  return {
    matriculaId: form.matriculaId,
    tipoDocumento: form.tipoDocumento,
    nombreArchivo: limpiarTexto(form.nombreArchivo),
    rutaAlmacenamiento: limpiarTexto(form.rutaAlmacenamiento),
    urlAcceso: limpiarTexto(form.urlAcceso),
    mimeType: limpiarTexto(form.mimeType),
    tamañoBytes: Number(form.tamañoBytes || 0),
    fechaEmision: form.fechaEmision || null,
    fechaVencimiento: form.fechaVencimiento || null,
    esObligatorio: !!form.esObligatorio,
    estado: form.estado,
    observaciones: limpiarTexto(form.observaciones),
    usuarioVerificacion: limpiarTexto(form.usuarioVerificacion),
    fechaVerificacion: form.fechaVerificacion
      ? new Date(form.fechaVerificacion).toISOString()
      : null,
  }
}

async function cargarCatalogos() {
  loadingCatalogos.value = true

  try {
    const data: any = await matricula('/api/matriculas')

    matriculas.value = Array.isArray(data?.matriculas)
      ? data.matriculas
      : Array.isArray(data)
      ? data
      : []
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al cargar matrículas'
    matriculas.value = []
  } finally {
    loadingCatalogos.value = false
  }
}

async function cargarDocumentos() {
  const data: any = await matricula('/api/documentos-matricula')

  const lista = Array.isArray(data)
    ? data
    : Array.isArray(data?.documentos)
    ? data.documentos
    : []

  documentos.value = lista.map(normalizarDocumento)
}

async function cargar() {
  loading.value = true
  errorMsg.value = ''

  try {
    await Promise.all([cargarCatalogos(), cargarDocumentos()])
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al cargar los documentos'
    documentos.value = []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, crearFormVacio())
  formRef.value?.resetValidation?.()
}

function abrirNuevo() {
  resetForm()
  errorMsg.value = ''
  successMsg.value = ''
  dialog.value = true
}

function editar(item: DocumentoMatricula) {
  errorMsg.value = ''
  successMsg.value = ''

  Object.assign(form, normalizarDocumento(item))
  form.fechaVerificacion = normalizarFechaHoraInput(item.fechaVerificacion)

  dialog.value = true
}

function verDetalle(item: DocumentoMatricula) {
  documentoSeleccionado.value = normalizarDocumento(item)
  dialogDetalle.value = true
}

function cerrarDialog() {
  dialog.value = false
  resetForm()
}

async function guardar() {
  const validation = await formRef.value?.validate?.()
  if (validation && validation.valid === false) return

  saving.value = true
  errorMsg.value = ''
  successMsg.value = ''

  try {
    const payload = construirPayload()
    const esEdicion = Boolean(form.id)

    if (esEdicion) {
      await matricula(`/api/documentos-matricula/${form.id}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      })
      successMsg.value = 'Documento actualizado correctamente.'
    } else {
      await matricula('/api/documentos-matricula', {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      successMsg.value = 'Documento registrado correctamente.'
    }

    cerrarDialog()
    await cargarDocumentos()
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al guardar el documento'
  } finally {
    saving.value = false
  }
}

async function eliminar(id: string | null) {
  if (!id) return
  if (!await confirmAction('¿Eliminar este documento? Esta acción no se puede deshacer.')) return

  errorMsg.value = ''
  successMsg.value = ''

  try {
    const data: any = await matricula(`/api/documentos-matricula/${id}`, {
      method: 'DELETE',
    })

    successMsg.value = data?.mensaje || 'Documento eliminado correctamente.'
    await cargarDocumentos()
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al eliminar el documento'
  }
}

function nombreEstudiante(matriculaId: string) {
  return matriculas.value.find((item) => item.id === matriculaId)?.nombreEstudiante ||
    'Estudiante no encontrado'
}

function resumenMatricula(matriculaId: string) {
  const matriculaItem = matriculas.value.find((item) => item.id === matriculaId)

  if (!matriculaItem) return matriculaId || '-'

  return `${matriculaItem.grado || '-'} ${matriculaItem.seccion || ''} · ${matriculaItem.periodo || '-'}`
}

function textoTipoDocumento(tipo?: string) {
  return tiposDocumentoSelect.find((item) => item.value === tipo)?.title ||
    tipo ||
    'SIN TIPO'
}

function colorEstado(estado?: string) {
  const mapa: Record<string, string> = {
    VERIFICADO: 'success',
    RECHAZADO: 'error',
    ENTREGADO: 'info',
    PENDIENTE: 'warning',
  }
  return mapa[estado || ''] || 'default'
}

function formatearFecha(fecha?: string) {
  if (!fecha) return '-'

  const date = new Date(`${fecha}T00:00:00`)

  if (Number.isNaN(date.getTime())) return fecha

  return new Intl.DateTimeFormat('es-PE', {
    dateStyle: 'medium',
  }).format(date)
}

function formatearFechaHora(fecha?: string) {
  if (!fecha) return '-'

  const date = new Date(fecha)

  if (Number.isNaN(date.getTime())) return fecha

  return new Intl.DateTimeFormat('es-PE', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

function normalizarFechaHoraInput(fecha?: string) {
  if (!fecha) return ''

  const date = new Date(fecha)

  if (Number.isNaN(date.getTime())) return ''

  const pad = (value: number) => String(value).padStart(2, '0')

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function formatearTamaño(bytes?: number | null) {
  const valor = Number(bytes || 0)

  if (valor < 1024) return `${valor} B`
  if (valor < 1024 * 1024) return `${(valor / 1024).toFixed(1)} KB`
  return `${(valor / (1024 * 1024)).toFixed(1)} MB`
}

onMounted(() => {
  cargar()
})
</script>