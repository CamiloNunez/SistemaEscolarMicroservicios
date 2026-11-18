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
          Nueva vacante
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
            label="Buscar por sección, grado o periodo"
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            clearable
            hide-details
          />
        </v-col>
        <v-col cols="12" md="3">
          <v-select
            v-model="filtroSeccion"
            :items="seccionesFiltro"
            item-title="title"
            item-value="value"
            label="Sección"
            variant="outlined"
            density="compact"
            hide-details
          />
        </v-col>
        <v-col cols="12" md="3">
          <v-select
            v-model="filtroPeriodo"
            :items="periodosFiltro"
            item-title="title"
            item-value="value"
            label="Periodo"
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
          <p class="text-caption text-grey-darken-1 mb-1">Total registros</p>
          <p class="text-h5 font-weight-bold mb-0">{{ vacantes.length }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="3">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Vacantes totales</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalVacantesGlobal }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="3">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Ocupadas</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalOcupadasGlobal }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="3">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Disponibles</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalDisponiblesGlobal }}</p>
        </v-card>
      </v-col>
    </v-row>

    <v-card class="main-card">
      <v-data-table
        :headers="headers"
        :items="vacantesFiltradas"
        :loading="loading"
        item-value="id"
        no-data-text="No hay registros de vacantes"
        loading-text="Cargando vacantes..."
      >
        <template #item.seccionId="{ item }">
          <div>
            <p class="font-weight-bold mb-0">{{ nombreSeccion(item.seccionId) }}</p>
            <p class="text-caption text-grey-darken-1 mb-0">
              {{ nombreGrado(item.gradoId) }}
            </p>
          </div>
        </template>
        <template #item.periodoAcademicoId="{ item }">
          <div>
            <p class="font-weight-bold mb-0">
              {{ nombrePeriodo(item.periodoAcademicoId) }}
            </p>
            <p class="text-caption text-grey-darken-1 mb-0">
              {{ codigoPeriodo(item.periodoAcademicoId) }}
            </p>
          </div>
        </template>
        <template #item.totalVacantes="{ item }">
          <span class="font-weight-bold">{{ item.totalVacantes ?? 0 }}</span>
        </template>
        <template #item.vacantesOcupadas="{ item }">
          {{ item.vacantesOcupadas ?? 0 }}
        </template>
        <template #item.vacantesReservadas="{ item }">
          {{ item.vacantesReservadas ?? 0 }}
        </template>
        <template #item.disponibles="{ item }">
          <v-chip
            size="small"
            :color="colorDisponibilidad(calcularDisponibles(item))"
            variant="tonal"
          >
            {{ calcularDisponibles(item) }}
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

    <v-dialog v-model="dialog" max-width="900" persistent>
      <v-card class="pa-5">
        <DialogHeader
          :title="form.id ? 'Editar vacantes' : 'Nueva configuración de vacantes'"
          subtitle="Configure la disponibilidad para una sección y periodo académico."
          @close="cerrarDialog"
        />

        <v-form ref="formRef">
          <v-row>
            <v-col cols="12" md="6">
              <v-select
                v-model="form.seccionId"
                :items="seccionesSelect"
                item-title="title"
                item-value="value"
                label="Sección"
                variant="outlined"
                density="compact"
                :loading="loadingCatalogos"
                :disabled="Boolean(form.id)"
                :rules="[rules.required]"
                @update:model-value="sincronizarSeccion"
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-select
                v-model="form.periodoAcademicoId"
                :items="periodosSelect"
                item-title="title"
                item-value="value"
                label="Periodo académico"
                variant="outlined"
                density="compact"
                :loading="loadingCatalogos"
                :disabled="Boolean(form.id)"
                :rules="[rules.required]"
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="form.nombreSeccion"
                label="Nombre de sección"
                variant="outlined"
                density="compact"
                readonly
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                :model-value="nombreGrado(form.gradoId)"
                label="Grado"
                variant="outlined"
                density="compact"
                readonly
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model.number="form.totalVacantes"
                label="Total de vacantes"
                type="number"
                min="1"
                max="200"
                variant="outlined"
                density="compact"
                :rules="[rules.requiredNumber, rules.totalVacantes]"
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model.number="form.vacantesOcupadas"
                label="Vacantes ocupadas"
                type="number"
                min="0"
                max="200"
                variant="outlined"
                density="compact"
                :rules="[rules.requiredNumber, rules.noNegativo]"
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model.number="form.vacantesReservadas"
                label="Vacantes reservadas"
                type="number"
                min="0"
                max="200"
                variant="outlined"
                density="compact"
                :rules="[rules.requiredNumber, rules.noNegativo]"
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                :model-value="disponiblesFormulario"
                label="Vacantes disponibles"
                variant="outlined"
                density="compact"
                readonly
                :error="disponiblesFormulario < 0"
                :error-messages="
                  disponiblesFormulario < 0 ? ['Ocupadas y reservadas superan el total'] : []
                "
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-select
                v-model="form.estado"
                :items="estados"
                label="Estado"
                variant="outlined"
                density="compact"
                :rules="[rules.required]"
              />
            </v-col>
          </v-row>
        </v-form>

        <div class="d-flex justify-end ga-2 mt-3">
          <v-btn variant="text" @click="cerrarDialog">Cancelar</v-btn>
          <v-btn color="primary" :loading="saving" @click="guardar">Guardar</v-btn>
        </div>
      </v-card>
    </v-dialog>

    <v-dialog v-model="dialogDetalle" max-width="720">
      <v-card class="pa-5">
        <DialogHeader
          title="Detalle de vacantes"
          subtitle="Resumen de disponibilidad por sección."
          @close="dialogDetalle = false"
        />

        <v-row v-if="vacanteDetalle">
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Sección</p>
            <p class="font-weight-bold">{{ nombreSeccion(vacanteDetalle.seccionId) }}</p>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Grado</p>
            <p class="font-weight-bold">{{ nombreGrado(vacanteDetalle.gradoId) }}</p>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Periodo</p>
            <p class="font-weight-bold">
              {{ nombrePeriodo(vacanteDetalle.periodoAcademicoId) }}
            </p>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Estado</p>
            <v-chip
              size="small"
              :color="colorEstado(vacanteDetalle.estado)"
              variant="tonal"
            >
              {{ vacanteDetalle.estado || 'SIN ESTADO' }}
            </v-chip>
          </v-col>
          <v-col cols="12" md="3">
            <p class="text-caption text-grey-darken-1 mb-1">Total</p>
            <p class="text-h6 font-weight-bold">{{ vacanteDetalle.totalVacantes ?? 0 }}</p>
          </v-col>
          <v-col cols="12" md="3">
            <p class="text-caption text-grey-darken-1 mb-1">Ocupadas</p>
            <p class="text-h6 font-weight-bold">{{ vacanteDetalle.vacantesOcupadas ?? 0 }}</p>
          </v-col>
          <v-col cols="12" md="3">
            <p class="text-caption text-grey-darken-1 mb-1">Reservadas</p>
            <p class="text-h6 font-weight-bold">{{ vacanteDetalle.vacantesReservadas ?? 0 }}</p>
          </v-col>
          <v-col cols="12" md="3">
            <p class="text-caption text-grey-darken-1 mb-1">Disponibles</p>
            <v-chip
              size="large"
              :color="colorDisponibilidad(calcularDisponibles(vacanteDetalle))"
              variant="tonal"
            >
              {{ calcularDisponibles(vacanteDetalle) }}
            </v-chip>
          </v-col>
        </v-row>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { useMatriculaHttp } from '~/composables/useMatriculaHttp'
const { confirm: confirmAction } = useUiFeedback()

interface Grado {
  id: string
  nombre: string
}

interface Seccion {
  id: string
  gradoId: string
  periodoAcademicoId: string
  letra: string
  nombre: string
  estado?: string
}

interface PeriodoAcademico {
  id: string
  codigo: string
  nombre: string
}

interface Vacante {
  id: string | null
  seccionId: string
  periodoAcademicoId: string
  gradoId: string
  nombreSeccion: string
  totalVacantes: number | null
  vacantesOcupadas: number | null
  vacantesReservadas: number | null
  estado: string
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
const busqueda = ref('')
const filtroSeccion = ref('TODOS')
const filtroPeriodo = ref('TODOS')
const filtroEstado = ref('TODOS')
const formRef = ref()

const vacantes = ref<Vacante[]>([])
const secciones = ref<Seccion[]>([])
const periodos = ref<PeriodoAcademico[]>([])
const grados = ref<Grado[]>([])
const vacanteDetalle = ref<Vacante | null>(null)

const estados = ['DISPONIBLE', 'LLENA', 'CERRADA']

const headers = [
  { title: 'Sección', key: 'seccionId' },
  { title: 'Periodo', key: 'periodoAcademicoId' },
  { title: 'Total', key: 'totalVacantes' },
  { title: 'Ocupadas', key: 'vacantesOcupadas' },
  { title: 'Reservadas', key: 'vacantesReservadas' },
  { title: 'Disponibles', key: 'disponibles', sortable: false },
  { title: 'Estado', key: 'estado' },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'end' as const },
]

const rules = {
  required: (value: unknown) => !!String(value || '').trim() || 'Campo requerido',
  requiredNumber: (value: number | null) =>
    value !== null && value !== undefined || 'Campo requerido',
  totalVacantes: (value: number | null) =>
    (value !== null && Number(value) >= 1 && Number(value) <= 200) ||
    'Debe estar entre 1 y 200',
  noNegativo: (value: number | null) =>
    (value !== null && Number(value) >= 0 && Number(value) <= 200) ||
    'Debe estar entre 0 y 200',
}

const form = reactive<Vacante>(crearFormVacio())

const seccionesSelect = computed(() =>
  secciones.value.map((item) => ({
    title: `${item.nombre} - ${nombreGrado(item.gradoId)}`,
    value: item.id,
  }))
)

const seccionesFiltro = computed(() => [
  { title: 'TODOS', value: 'TODOS' },
  ...seccionesSelect.value,
])

const periodosSelect = computed(() =>
  periodos.value.map((item) => ({
    title: `${item.nombre} (${item.codigo})`,
    value: item.id,
  }))
)

const periodosFiltro = computed(() => [
  { title: 'TODOS', value: 'TODOS' },
  ...periodosSelect.value,
])

const disponiblesFormulario = computed(() =>
  Number(form.totalVacantes || 0) -
  Number(form.vacantesOcupadas || 0) -
  Number(form.vacantesReservadas || 0)
)

const vacantesFiltradas = computed(() => {
  const texto = busqueda.value?.trim().toLowerCase() || ''

  return vacantes.value.filter((item) => {
    const coincideTexto =
      !texto ||
      [
        nombreSeccion(item.seccionId),
        nombreGrado(item.gradoId),
        nombrePeriodo(item.periodoAcademicoId),
        codigoPeriodo(item.periodoAcademicoId),
      ].some((valor) => String(valor || '').toLowerCase().includes(texto))

    const coincideSeccion =
      filtroSeccion.value === 'TODOS' || item.seccionId === filtroSeccion.value

    const coincidePeriodo =
      filtroPeriodo.value === 'TODOS' || item.periodoAcademicoId === filtroPeriodo.value

    const coincideEstado =
      filtroEstado.value === 'TODOS' || item.estado === filtroEstado.value

    return coincideTexto && coincideSeccion && coincidePeriodo && coincideEstado
  })
})

const totalVacantesGlobal = computed(() =>
  vacantes.value.reduce((total, item) => total + Number(item.totalVacantes || 0), 0)
)

const totalOcupadasGlobal = computed(() =>
  vacantes.value.reduce((total, item) => total + Number(item.vacantesOcupadas || 0), 0)
)

const totalDisponiblesGlobal = computed(() =>
  vacantes.value.reduce(
    (total, item) => total + Math.max(0, calcularDisponibles(item)),
    0
  )
)

function crearFormVacio(): Vacante {
  return {
    id: null,
    seccionId: '',
    periodoAcademicoId: '',
    gradoId: '',
    nombreSeccion: '',
    totalVacantes: 30,
    vacantesOcupadas: 0,
    vacantesReservadas: 0,
    estado: 'DISPONIBLE',
  }
}

function normalizarVacante(item: any): Vacante {
  return {
    id: item.id ?? null,
    seccionId: item.seccionId ?? '',
    periodoAcademicoId: item.periodoAcademicoId ?? '',
    gradoId: item.gradoId ?? '',
    nombreSeccion: item.nombreSeccion ?? '',
    totalVacantes: item.totalVacantes ?? 30,
    vacantesOcupadas: item.vacantesOcupadas ?? 0,
    vacantesReservadas: item.vacantesReservadas ?? 0,
    estado: item.estado ?? 'DISPONIBLE',
    usuarioRegistro: item.usuarioRegistro,
    fechaRegistro: item.fechaRegistro,
    fechaActualizacion: item.fechaActualizacion,
  }
}

function construirPayload() {
  return {
    seccionId: form.seccionId,
    periodoAcademicoId: form.periodoAcademicoId,
    gradoId: form.gradoId,
    nombreSeccion: form.nombreSeccion,
    totalVacantes: Number(form.totalVacantes),
    vacantesOcupadas: Number(form.vacantesOcupadas),
    vacantesReservadas: Number(form.vacantesReservadas),
    estado: calcularEstadoAutomatico(),
  }
}

function validarCantidades() {
  const total = Number(form.totalVacantes || 0)
  const ocupadas = Number(form.vacantesOcupadas || 0)
  const reservadas = Number(form.vacantesReservadas || 0)

  if (ocupadas + reservadas > total) {
    throw new Error('La suma de vacantes ocupadas y reservadas no puede superar el total.')
  }
}

function calcularEstadoAutomatico() {
  if (form.estado === 'CERRADA') return 'CERRADA'
  return disponiblesFormulario.value <= 0 ? 'LLENA' : 'DISPONIBLE'
}

async function cargarCatalogos() {
  loadingCatalogos.value = true

  try {
    const [seccionesData, periodosData, gradosData]: any[] = await Promise.all([
      matricula('/api/secciones'),
      matricula('/api/periodos-academicos'),
      matricula('/api/grados'),
    ])

    secciones.value = Array.isArray(seccionesData) ? seccionesData : []
    periodos.value = Array.isArray(periodosData) ? periodosData : []
    grados.value = Array.isArray(gradosData) ? gradosData : []
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al cargar catálogos'
    secciones.value = []
    periodos.value = []
    grados.value = []
  } finally {
    loadingCatalogos.value = false
  }
}

async function cargarVacantes() {
  const data: any = await matricula('/api/vacantes')

  const lista = Array.isArray(data)
    ? data
    : Array.isArray(data?.vacantes)
    ? data.vacantes
    : []

  vacantes.value = lista.map(normalizarVacante)
}

async function cargar() {
  loading.value = true
  errorMsg.value = ''

  try {
    await Promise.all([cargarCatalogos(), cargarVacantes()])
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al cargar las vacantes'
    vacantes.value = []
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

function editar(item: Vacante) {
  resetForm()
  errorMsg.value = ''
  successMsg.value = ''

  Object.assign(form, normalizarVacante(item))
  dialog.value = true
}

function verDetalle(item: Vacante) {
  vacanteDetalle.value = normalizarVacante(item)
  dialogDetalle.value = true
}

function cerrarDialog() {
  dialog.value = false
  resetForm()
}

function sincronizarSeccion() {
  const seccion = secciones.value.find((item) => item.id === form.seccionId)

  if (!seccion) {
    form.gradoId = ''
    form.nombreSeccion = ''
    return
  }

  form.gradoId = seccion.gradoId
  form.nombreSeccion = seccion.nombre
  form.periodoAcademicoId = seccion.periodoAcademicoId
}

async function guardar() {
  const validation = await formRef.value?.validate?.()
  if (validation && validation.valid === false) return

  saving.value = true
  errorMsg.value = ''
  successMsg.value = ''

  try {
    validarCantidades()

    const payload = construirPayload()
    const esEdicion = Boolean(form.id)

    if (esEdicion) {
      await matricula(`/api/vacantes/${form.id}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      })
      successMsg.value = 'Vacantes actualizadas correctamente.'
    } else {
      await matricula('/api/vacantes', {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      successMsg.value = 'Vacantes registradas correctamente.'
    }

    cerrarDialog()
    await cargarVacantes()
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al guardar las vacantes'
  } finally {
    saving.value = false
  }
}

async function eliminar(id: string | null) {
  if (!id) return
  if (!await confirmAction('¿Eliminar esta configuración de vacantes? Esta acción no se puede deshacer.'))
    return

  errorMsg.value = ''
  successMsg.value = ''

  try {
    const data: any = await matricula(`/api/vacantes/${id}`, {
      method: 'DELETE',
    })

    successMsg.value = data?.mensaje || 'Configuración de vacantes eliminada correctamente.'
    await cargarVacantes()
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al eliminar las vacantes'
  }
}

function calcularDisponibles(item: Vacante) {
  return (
    Number(item.totalVacantes || 0) -
    Number(item.vacantesOcupadas || 0) -
    Number(item.vacantesReservadas || 0)
  )
}

function nombreSeccion(id: string) {
  return secciones.value.find((item) => item.id === id)?.nombre || 'Sección no encontrada'
}

function nombreGrado(id: string) {
  return grados.value.find((item) => item.id === id)?.nombre || 'Grado no encontrado'
}

function nombrePeriodo(id: string) {
  return periodos.value.find((item) => item.id === id)?.nombre || 'Periodo no encontrado'
}

function codigoPeriodo(id: string) {
  return periodos.value.find((item) => item.id === id)?.codigo || id || '-'
}

function colorEstado(estado?: string) {
  if (estado === 'DISPONIBLE') return 'success'
  if (estado === 'LLENA') return 'warning'
  if (estado === 'CERRADA') return 'error'
  return 'default'
}

function colorDisponibilidad(disponibles: number) {
  if (disponibles <= 0) return 'error'
  if (disponibles <= 5) return 'warning'
  return 'success'
}

onMounted(() => {
  cargar()
})
</script>