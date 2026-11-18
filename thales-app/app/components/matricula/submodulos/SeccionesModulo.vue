<template>
  <div>
    <div class="d-flex justify-space-between align-center mb-4 flex-wrap ga-3">
      <div class="d-flex ga-2 flex-wrap">
        <v-btn
          variant="tonal"
          color="primary"
          :loading="loading"
          @click="cargar"
        >
          <v-icon start>mdi-refresh</v-icon>
          Actualizar
        </v-btn>
        <v-btn color="primary" @click="abrirNuevo">
          <v-icon start>mdi-plus</v-icon>
          Nueva sección
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
            label="Buscar por nombre, letra, aula o tutor"
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            clearable
            hide-details
          />
        </v-col>
        <v-col cols="12" md="3">
          <v-select
            v-model="filtroGrado"
            :items="gradosFiltro"
            item-title="title"
            item-value="value"
            label="Grado"
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
      <v-col cols="12" md="4">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Total de secciones</p>
          <p class="text-h5 font-weight-bold mb-0">{{ secciones.length }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Secciones activas</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalActivas }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Capacidad total</p>
          <p class="text-h5 font-weight-bold mb-0">{{ capacidadTotal }}</p>
        </v-card>
      </v-col>
    </v-row>
    <v-card class="main-card">
      <v-data-table
        :headers="headers"
        :items="seccionesFiltradas"
        :loading="loading"
        item-value="id"
        no-data-text="No hay secciones registradas"
        loading-text="Cargando secciones..."
      >
        <template #item.gradoId="{ item }">
          <div>
            <p class="font-weight-bold mb-0">{{ nombreGrado(item.gradoId) }}</p>
            <p class="text-caption text-grey-darken-1 mb-0">
              {{ nombreProgramaPorGrado(item.gradoId) }}
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
        <template #item.letra="{ item }">
          <v-chip size="small" color="primary" variant="tonal">
            {{ item.letra }}
          </v-chip>
        </template>
        <template #item.turno="{ item }">
          <v-chip size="small" color="secondary" variant="tonal">
            {{ item.turno || 'SIN TURNO' }}
          </v-chip>
        </template>
        <template #item.capacidadMaxima="{ item }">
          {{ item.capacidadMaxima ?? 0 }}
        </template>
        <template #item.estado="{ item }">
          <v-chip
            size="small"
            :color="colorEstado(item.estado)"
            variant="tonal"
          >
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
          :title="form.id ? 'Editar sección' : 'Nueva sección'"
          subtitle="Configure el grado, periodo, turno, capacidad y aula."
          @close="cerrarDialog"
        />
        <v-form ref="formRef">
          <v-row>
            <v-col cols="12" md="6">
              <v-select
                v-model="form.gradoId"
                :items="gradosSelect"
                item-title="title"
                item-value="value"
                label="Grado"
                variant="outlined"
                density="compact"
                :loading="loadingCatalogos"
                :rules="[rules.required]"
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
                :rules="[rules.required]"
              />
            </v-col>
            <v-col cols="12" md="3">
              <v-text-field
                v-model.trim="form.letra"
                label="Letra"
                variant="outlined"
                density="compact"
                maxlength="3"
                counter="3"
                :rules="[rules.required, rules.letra]"
                @update:model-value="form.letra = normalizarLetra(form.letra)"
              />
            </v-col>
            <v-col cols="12" md="5">
              <v-text-field
                v-model.trim="form.nombre"
                label="Nombre de la sección"
                variant="outlined"
                density="compact"
                maxlength="80"
                counter="80"
                :rules="[rules.required]"
                hint="Ejemplo: 1° A"
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-select
                v-model="form.turno"
                :items="turnos"
                label="Turno"
                variant="outlined"
                density="compact"
                :rules="[rules.required]"
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model.number="form.capacidadMaxima"
                label="Capacidad máxima"
                type="number"
                min="1"
                max="100"
                variant="outlined"
                density="compact"
                :rules="[rules.requiredNumber, rules.capacidad]"
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model.trim="form.aulaAsignada"
                label="Aula asignada"
                variant="outlined"
                density="compact"
                maxlength="50"
                counter="50"
              />
            </v-col>
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
            <v-col cols="12">
              <v-text-field
                v-model.trim="form.docenteTutorId"
                label="ID del docente tutor"
                variant="outlined"
                density="compact"
                clearable
                hint="Opcional mientras no exista un selector conectado al microservicio de docentes."
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
          title="Detalle de la sección"
          subtitle="Información académica y operativa registrada."
          @close="dialogDetalle = false"
        />
        <v-row v-if="seccionDetalle">
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Grado</p>
            <p class="font-weight-bold mb-0">
              {{ nombreGrado(seccionDetalle.gradoId) }}
            </p>
            <p class="text-caption text-grey-darken-1">
              {{ nombreProgramaPorGrado(seccionDetalle.gradoId) }}
            </p>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">
              Periodo académico
            </p>
            <p class="font-weight-bold mb-0">
              {{ nombrePeriodo(seccionDetalle.periodoAcademicoId) }}
            </p>
            <p class="text-caption text-grey-darken-1">
              {{ codigoPeriodo(seccionDetalle.periodoAcademicoId) }}
            </p>
          </v-col>
          <v-col cols="12" md="3">
            <p class="text-caption text-grey-darken-1 mb-1">Letra</p>
            <v-chip size="small" color="primary" variant="tonal">
              {{ seccionDetalle.letra }}
            </v-chip>
          </v-col>
          <v-col cols="12" md="5">
            <p class="text-caption text-grey-darken-1 mb-1">Nombre</p>
            <p class="font-weight-bold">{{ seccionDetalle.nombre }}</p>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Turno</p>
            <v-chip size="small" color="secondary" variant="tonal">
              {{ seccionDetalle.turno || 'SIN TURNO' }}
            </v-chip>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">
              Capacidad máxima
            </p>
            <p class="font-weight-bold">
              {{ seccionDetalle.capacidadMaxima ?? 0 }}
            </p>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Aula</p>
            <p>{{ seccionDetalle.aulaAsignada || '-' }}</p>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Estado</p>
            <v-chip
              size="small"
              :color="colorEstado(seccionDetalle.estado)"
              variant="tonal"
            >
              {{ seccionDetalle.estado || 'SIN ESTADO' }}
            </v-chip>
          </v-col>
          <v-col cols="12">
            <p class="text-caption text-grey-darken-1 mb-1">
              Docente tutor
            </p>
            <p>{{ seccionDetalle.docenteTutorId || 'Sin asignar' }}</p>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">
              Usuario registro
            </p>
            <p>{{ seccionDetalle.usuarioRegistro || '-' }}</p>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">
              Fecha de registro
            </p>
            <p>{{ formatearFecha(seccionDetalle.fechaRegistro) }}</p>
          </v-col>
        </v-row>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { useMatriculaHttp } from '~/composables/useMatriculaHttp'
const { confirm: confirmAction } = useUiFeedback()

interface ProgramaAcademico {
  id: string
  codigo: string
  nombre: string
}

interface Grado {
  id: string
  programaAcademicoId: string
  numero: number
  nombre: string
  estado?: string
}

interface PeriodoAcademico {
  id: string
  codigo: string
  nombre: string
  estado?: string
}

interface Seccion {
  id: string | null
  gradoId: string
  periodoAcademicoId: string
  letra: string
  nombre: string
  turno: string
  capacidadMaxima: number | null
  aulaAsignada: string
  docenteTutorId: string
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
const filtroGrado = ref('TODOS')
const filtroPeriodo = ref('TODOS')
const filtroEstado = ref('TODOS')
const formRef = ref()
const seccionDetalle = ref<Seccion | null>(null)

const secciones = ref<Seccion[]>([])
const grados = ref<Grado[]>([])
const periodos = ref<PeriodoAcademico[]>([])
const programas = ref<ProgramaAcademico[]>([])

const turnos = ['MAÑANA', 'TARDE', 'NOCHE']
const estados = ['ACTIVO', 'INACTIVO', 'COMPLETA']

const headers = [
  { title: 'Grado', key: 'gradoId' },
  { title: 'Periodo', key: 'periodoAcademicoId' },
  { title: 'Letra', key: 'letra' },
  { title: 'Nombre', key: 'nombre' },
  { title: 'Turno', key: 'turno' },
  { title: 'Capacidad', key: 'capacidadMaxima' },
  { title: 'Aula', key: 'aulaAsignada' },
  { title: 'Estado', key: 'estado' },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'end' as const },
]

const rules = {
  required: (value: unknown) => !!String(value || '').trim() || 'Campo requerido',
  requiredNumber: (value: number | null) =>
    value !== null && value !== undefined || 'Campo requerido',
  letra: (value: string) =>
    /^[A-Z0-9]{1,3}$/.test(value || '') || 'Use entre 1 y 3 letras o números',
  capacidad: (value: number | null) =>
    (value !== null && Number(value) >= 1 && Number(value) <= 100) ||
    'Debe estar entre 1 y 100',
}

const form = reactive<Seccion>(crearFormVacio())

const gradosSelect = computed(() =>
  grados.value.map((grado) => ({
    title: `${grado.nombre} - ${nombrePrograma(grado.programaAcademicoId)}`,
    value: grado.id,
  }))
)

const gradosFiltro = computed(() => [
  { title: 'TODOS', value: 'TODOS' },
  ...gradosSelect.value,
])

const periodosSelect = computed(() =>
  periodos.value.map((periodo) => ({
    title: `${periodo.nombre} (${periodo.codigo})`,
    value: periodo.id,
  }))
)

const periodosFiltro = computed(() => [
  { title: 'TODOS', value: 'TODOS' },
  ...periodosSelect.value,
])

const seccionesFiltradas = computed(() => {
  const texto = (busqueda.value || '').toLowerCase().trim()

  return secciones.value.filter((seccion) => {
    const coincideTexto =
      !texto ||
      [
        seccion.letra,
        seccion.nombre,
        seccion.aulaAsignada,
        seccion.docenteTutorId,
        seccion.turno,
        nombreGrado(seccion.gradoId),
        nombrePeriodo(seccion.periodoAcademicoId),
      ].some((valor) => String(valor || '').toLowerCase().includes(texto))

    const coincideGrado =
      filtroGrado.value === 'TODOS' || seccion.gradoId === filtroGrado.value

    const coincidePeriodo =
      filtroPeriodo.value === 'TODOS' ||
      seccion.periodoAcademicoId === filtroPeriodo.value

    const coincideEstado =
      filtroEstado.value === 'TODOS' || seccion.estado === filtroEstado.value

    return coincideTexto && coincideGrado && coincidePeriodo && coincideEstado
  })
})

const totalActivas = computed(() =>
  secciones.value.filter((seccion) => seccion.estado === 'ACTIVO').length
)

const capacidadTotal = computed(() =>
  secciones.value.reduce(
    (total, seccion) => total + Number(seccion.capacidadMaxima || 0),
    0
  )
)

function crearFormVacio(): Seccion {
  return {
    id: null,
    gradoId: '',
    periodoAcademicoId: '',
    letra: '',
    nombre: '',
    turno: 'MAÑANA',
    capacidadMaxima: 30,
    aulaAsignada: '',
    docenteTutorId: '',
    estado: 'ACTIVO',
  }
}

function limpiarTexto(value: string | null | undefined) {
  const limpio = String(value ?? '').trim()
  return limpio || null
}

function normalizarLetra(value: string) {
  return String(value || '')
    .toUpperCase()
    .replace(/[^A-Z0-9]/g, '')
    .slice(0, 3)
}

function normalizarSeccion(item: any): Seccion {
  return {
    id: item.id ?? null,
    gradoId: item.gradoId ?? '',
    periodoAcademicoId: item.periodoAcademicoId ?? '',
    letra: item.letra ?? '',
    nombre: item.nombre ?? '',
    turno: item.turno ?? 'MAÑANA',
    capacidadMaxima: item.capacidadMaxima ?? 30,
    aulaAsignada: item.aulaAsignada ?? '',
    docenteTutorId: item.docenteTutorId ?? '',
    estado: item.estado ?? 'ACTIVO',
    usuarioRegistro: item.usuarioRegistro,
    fechaRegistro: item.fechaRegistro,
    fechaActualizacion: item.fechaActualizacion,
  }
}

function construirPayload() {
  return {
    gradoId: form.gradoId,
    periodoAcademicoId: form.periodoAcademicoId,
    letra: normalizarLetra(form.letra),
    nombre: limpiarTexto(form.nombre),
    turno: form.turno,
    capacidadMaxima: Number(form.capacidadMaxima),
    aulaAsignada: limpiarTexto(form.aulaAsignada),
    docenteTutorId: limpiarTexto(form.docenteTutorId),
    estado: form.estado,
  }
}

async function cargarCatalogos() {
  loadingCatalogos.value = true

  try {
    const [programasData, gradosData, periodosData]: any[] = await Promise.all([
      matricula('/api/programas-academicos'),
      matricula('/api/grados'),
      matricula('/api/periodos-academicos'),
    ])

    programas.value = Array.isArray(programasData) ? programasData : []
    grados.value = Array.isArray(gradosData) ? gradosData : []
    periodos.value = Array.isArray(periodosData) ? periodosData : []
  } catch (error: any) {
    throw new Error(
      error.data?.mensaje || error.message || 'Error al cargar los catálogos'
    )
  } finally {
    loadingCatalogos.value = false
  }
}

async function cargarSecciones() {
  const data: any = await matricula('/api/secciones')

  const lista = Array.isArray(data)
    ? data
    : Array.isArray(data?.secciones)
    ? data.secciones
    : []

  secciones.value = lista.map(normalizarSeccion)
}

async function cargar() {
  loading.value = true
  errorMsg.value = ''

  try {
    await Promise.all([cargarCatalogos(), cargarSecciones()])
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al cargar las secciones'
    secciones.value = []
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

function editar(item: Seccion) {
  errorMsg.value = ''
  successMsg.value = ''

  Object.assign(form, normalizarSeccion(item))
  dialog.value = true
}

function verDetalle(item: Seccion) {
  seccionDetalle.value = normalizarSeccion(item)
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
      await matricula(`/api/secciones/${form.id}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      })
      successMsg.value = 'Sección actualizada correctamente.'
    } else {
      await matricula('/api/secciones', {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      successMsg.value = 'Sección registrada correctamente.'
    }

    cerrarDialog()
    await cargarSecciones()
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al guardar la sección'
  } finally {
    saving.value = false
  }
}

async function eliminar(id: string | null) {
  if (!id) return
  if (!await confirmAction('¿Eliminar esta sección? Esta acción no se puede deshacer.')) return

  errorMsg.value = ''
  successMsg.value = ''

  try {
    const data: any = await matricula(`/api/secciones/${id}`, {
      method: 'DELETE',
    })

    successMsg.value = data?.mensaje || 'Sección eliminada correctamente.'
    await cargarSecciones()
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al eliminar la sección'
  }
}

function nombrePrograma(programaId: string) {
  return (
    programas.value.find((programa) => programa.id === programaId)?.nombre ||
    'Programa no encontrado'
  )
}

function nombreGrado(gradoId: string) {
  return (
    grados.value.find((grado) => grado.id === gradoId)?.nombre ||
    'Grado no encontrado'
  )
}

function nombreProgramaPorGrado(gradoId: string) {
  const grado = grados.value.find((item) => item.id === gradoId)
  return grado ? nombrePrograma(grado.programaAcademicoId) : 'Programa no encontrado'
}

function nombrePeriodo(periodoId: string) {
  return (
    periodos.value.find((periodo) => periodo.id === periodoId)?.nombre ||
    'Periodo no encontrado'
  )
}

function codigoPeriodo(periodoId: string) {
  return (
    periodos.value.find((periodo) => periodo.id === periodoId)?.codigo ||
    periodoId ||
    '-'
  )
}

function colorEstado(estado?: string) {
  if (estado === 'ACTIVO') return 'success'
  if (estado === 'COMPLETA') return 'warning'
  return 'default'
}

function formatearFecha(fecha?: string) {
  if (!fecha) return '-'

  const date = new Date(fecha)

  if (Number.isNaN(date.getTime())) return fecha

  return new Intl.DateTimeFormat('es-PE', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

onMounted(() => {
  cargar()
})
</script>