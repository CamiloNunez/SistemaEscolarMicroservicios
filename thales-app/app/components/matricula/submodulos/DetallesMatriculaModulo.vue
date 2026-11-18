<template>
  <div>
    <div class="d-flex justify-space-between align-center mb-4 flex-wrap ga-3">
      <div class="d-flex ga-2 flex-wrap">
        <v-btn
          variant="tonal"
          color="primary"
          :loading="loading"
          @click="cargarTodo"
        >
          <v-icon start>mdi-refresh</v-icon>
          Actualizar
        </v-btn>

        <v-btn color="primary" @click="abrirNuevo">
          <v-icon start>mdi-plus</v-icon>
          Agregar curso
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
            label="Buscar por estudiante, curso o código"
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
            :items="['TODOS', ...tiposInscripcion]"
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
          <p class="text-caption text-grey-darken-1 mb-1">Inscripciones</p>
          <p class="text-h5 font-weight-bold mb-0">{{ detallesFiltrados.length }}</p>
        </v-card>
      </v-col>

      <v-col cols="12" md="3">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Horas semanales</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalHorasFiltradas }}</p>
        </v-card>
      </v-col>

      <v-col cols="12" md="3">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Obligatorios</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalObligatorios }}</p>
        </v-card>
      </v-col>

      <v-col cols="12" md="3">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Electivos</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalElectivos }}</p>
        </v-card>
      </v-col>
    </v-row>

    <v-card class="main-card">
      <v-data-table
        :headers="headers"
        :items="detallesFiltrados"
        :loading="loading"
        item-value="id"
        no-data-text="No hay cursos inscritos"
        loading-text="Cargando detalle de matrícula..."
      >
        <template #item.matriculaId="{ item }">
          <div>
            <p class="font-weight-bold mb-0">
              {{ nombreEstudiante(item.matriculaId) }}
            </p>
            <p class="text-caption text-grey-darken-1 mb-0">
              {{ resumenMatricula(item.matriculaId) }}
            </p>
          </div>
        </template>

        <template #item.nombreCurso="{ item }">
          <div>
            <p class="font-weight-bold mb-0">
              {{ item.nombreCurso || nombreCurso(item.cursoId) }}
            </p>
            <p class="text-caption text-grey-darken-1 mb-0">
              {{ item.codigoCurso || codigoCurso(item.cursoId) }}
            </p>
          </div>
        </template>

        <template #item.horasSemanales="{ item }">
          {{ item.horasSemanales ?? 0 }} h
        </template>

        <template #item.tipoInscripcion="{ item }">
          <v-chip
            size="small"
            variant="tonal"
            :color="item.tipoInscripcion === 'OBLIGATORIO' ? 'primary' : 'secondary'"
          >
            {{ item.tipoInscripcion || 'SIN TIPO' }}
          </v-chip>
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
              @click="eliminar(item)"
            />
          </div>
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-model="dialog" max-width="820" persistent>
      <v-card class="pa-5">
        <DialogHeader
          :title="form.id ? 'Editar curso inscrito' : 'Agregar curso a matrícula'"
          subtitle="Seleccione la matrícula y el curso que formará parte de su detalle."
          @close="cerrarDialog"
        />

        <v-form ref="formRef">
          <v-row>
            <v-col cols="12">
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

            <v-col cols="12">
              <v-select
                v-model="form.cursoId"
                :items="cursosDisponiblesParaFormulario"
                item-title="title"
                item-value="value"
                label="Curso"
                variant="outlined"
                density="compact"
                :loading="loadingCatalogos"
                :rules="[rules.required]"
                @update:model-value="sincronizarCurso"
              />
            </v-col>

            <v-col cols="12" md="6">
              <v-text-field
                v-model="form.nombreCurso"
                label="Nombre del curso"
                variant="outlined"
                density="compact"
                readonly
              />
            </v-col>

            <v-col cols="12" md="3">
              <v-text-field
                v-model="form.codigoCurso"
                label="Código"
                variant="outlined"
                density="compact"
                readonly
              />
            </v-col>

            <v-col cols="12" md="3">
              <v-text-field
                v-model.number="form.horasSemanales"
                label="Horas semanales"
                type="number"
                min="1"
                max="40"
                variant="outlined"
                density="compact"
                :rules="[rules.requiredNumber, rules.horas]"
              />
            </v-col>

            <v-col cols="12" md="6">
              <v-select
                v-model="form.tipoInscripcion"
                :items="tiposInscripcion"
                label="Tipo de inscripción"
                variant="outlined"
                density="compact"
                :rules="[rules.required]"
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
          <v-btn variant="text" @click="cerrarDialog">
            Cancelar
          </v-btn>

          <v-btn color="primary" :loading="saving" @click="guardar">
            Guardar
          </v-btn>
        </div>
      </v-card>
    </v-dialog>

    <v-dialog v-model="dialogDetalle" max-width="720">
      <v-card class="pa-5">
        <DialogHeader
          title="Detalle del curso inscrito"
          subtitle="Información del curso asociado a la matrícula."
          @close="dialogDetalle = false"
        />

        <v-row v-if="detalleSeleccionado">
          <v-col cols="12">
            <p class="text-caption text-grey-darken-1 mb-1">Estudiante</p>
            <p class="font-weight-bold mb-0">
              {{ nombreEstudiante(detalleSeleccionado.matriculaId) }}
            </p>
            <p class="text-caption text-grey-darken-1">
              {{ resumenMatricula(detalleSeleccionado.matriculaId) }}
            </p>
          </v-col>

          <v-col cols="12" md="8">
            <p class="text-caption text-grey-darken-1 mb-1">Curso</p>
            <p class="font-weight-bold">
              {{ detalleSeleccionado.nombreCurso || nombreCurso(detalleSeleccionado.cursoId) }}
            </p>
          </v-col>

          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Código</p>
            <v-chip size="small" color="primary" variant="tonal">
              {{ detalleSeleccionado.codigoCurso || codigoCurso(detalleSeleccionado.cursoId) }}
            </v-chip>
          </v-col>

          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Horas</p>
            <p class="font-weight-bold">
              {{ detalleSeleccionado.horasSemanales ?? 0 }} h
            </p>
          </v-col>

          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Tipo</p>
            <v-chip
              size="small"
              variant="tonal"
              :color="detalleSeleccionado.tipoInscripcion === 'OBLIGATORIO' ? 'primary' : 'secondary'"
            >
              {{ detalleSeleccionado.tipoInscripcion || 'SIN TIPO' }}
            </v-chip>
          </v-col>

          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Estado</p>
            <v-chip
              size="small"
              :color="colorEstado(detalleSeleccionado.estado)"
              variant="tonal"
            >
              {{ detalleSeleccionado.estado || 'SIN ESTADO' }}
            </v-chip>
          </v-col>
        </v-row>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
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

interface Curso {
  id: string
  codigo: string
  nombre: string
  horasSemanales?: number
  tipo?: string
  estado?: string
}

interface DetalleMatricula {
  id: string | null
  matriculaId: string
  cursoId: string
  nombreCurso: string
  codigoCurso: string
  horasSemanales: number | null
  tipoInscripcion: string
  estado: string
  usuarioRegistro?: string
  fechaRegistro?: string
  fechaActualizacion?: string
}

const { matricula } = useMatriculaHttp()
const { academico } = useAcademicoHttp()

const loading = ref(false)
const loadingCatalogos = ref(false)
const saving = ref(false)
const dialog = ref(false)
const dialogDetalle = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const busqueda = ref('')
const filtroMatricula = ref('TODOS')
const filtroTipo = ref('TODOS')
const filtroEstado = ref('TODOS')
const formRef = ref()

const detalles = ref<DetalleMatricula[]>([])
const matriculas = ref<Matricula[]>([])
const cursos = ref<Curso[]>([])
const detalleSeleccionado = ref<DetalleMatricula | null>(null)

const tiposInscripcion = ['OBLIGATORIO', 'ELECTIVO']
const estados = ['INSCRITO', 'RETIRADO', 'APROBADO', 'DESAPROBADO']

const headers = [
  { title: 'Matrícula', key: 'matriculaId' },
  { title: 'Curso', key: 'nombreCurso' },
  { title: 'Horas', key: 'horasSemanales' },
  { title: 'Tipo', key: 'tipoInscripcion' },
  { title: 'Estado', key: 'estado' },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'end' as const },
]

const form = reactive<DetalleMatricula>({
  id: null,
  matriculaId: '',
  cursoId: '',
  nombreCurso: '',
  codigoCurso: '',
  horasSemanales: 1,
  tipoInscripcion: 'OBLIGATORIO',
  estado: 'INSCRITO',
})

const rules = {
  required: (value: string) =>
    !!String(value || '').trim() || 'Campo requerido',
  requiredNumber: (value: number | null) =>
    value !== null && value !== undefined || 'Campo requerido',
  horas: (value: number | null) =>
    (value !== null && Number(value) >= 1 && Number(value) <= 40) ||
    'Debe estar entre 1 y 40 horas',
}

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

const cursosDisponiblesParaFormulario = computed(() => {
  const usados = detalles.value
    .filter(
      (detalle) =>
        detalle.matriculaId === form.matriculaId &&
        detalle.id !== form.id
    )
    .map((detalle) => detalle.cursoId)

  return cursos.value
    .filter(
      (curso) =>
        curso.estado !== 'INACTIVO' &&
        (curso.id === form.cursoId || !usados.includes(curso.id))
    )
    .map((curso) => ({
      title: `${curso.nombre} (${curso.codigo})`,
      value: curso.id,
    }))
})

const detallesFiltrados = computed(() => {
  const texto = busqueda.value?.trim().toLowerCase() || ''

  return detalles.value.filter((detalle) => {
    const coincideTexto =
      !texto ||
      [
        detalle.nombreCurso,
        detalle.codigoCurso,
        nombreCurso(detalle.cursoId),
        codigoCurso(detalle.cursoId),
        nombreEstudiante(detalle.matriculaId),
        resumenMatricula(detalle.matriculaId),
      ].some((valor) =>
        String(valor || '').toLowerCase().includes(texto)
      )

    const coincideMatricula =
      filtroMatricula.value === 'TODOS' ||
      detalle.matriculaId === filtroMatricula.value

    const coincideTipo =
      filtroTipo.value === 'TODOS' ||
      detalle.tipoInscripcion === filtroTipo.value

    const coincideEstado =
      filtroEstado.value === 'TODOS' ||
      detalle.estado === filtroEstado.value

    return coincideTexto && coincideMatricula && coincideTipo && coincideEstado
  })
})

const totalHorasFiltradas = computed(() =>
  detallesFiltrados.value.reduce(
    (total, detalle) => total + Number(detalle.horasSemanales || 0),
    0
  )
)

const totalObligatorios = computed(() =>
  detallesFiltrados.value.filter(
    (detalle) => detalle.tipoInscripcion === 'OBLIGATORIO'
  ).length
)

const totalElectivos = computed(() =>
  detallesFiltrados.value.filter(
    (detalle) => detalle.tipoInscripcion === 'ELECTIVO'
  ).length
)

async function cargarCatalogos() {
  loadingCatalogos.value = true

  try {
    const [matriculasData, cursosData] = await Promise.all([
      matricula('/api/matriculas'),
      matricula('/api/cursos'),
    ])

    matriculas.value = Array.isArray(matriculasData?.matriculas)
      ? matriculasData.matriculas
      : Array.isArray(matriculasData)
        ? matriculasData
        : []

    cursos.value = Array.isArray(cursosData) ? cursosData : []
  } finally {
    loadingCatalogos.value = false
  }
}

async function cargarDetalles() {
  const data = await matricula('/api/detalles-matricula')

  detalles.value = Array.isArray(data) ? data : []
}

async function cargarTodo() {
  loading.value = true
  errorMsg.value = ''

  try {
    await Promise.all([cargarCatalogos(), cargarDetalles()])
  } catch (error: any) {
    errorMsg.value = error.message || 'Error al cargar el detalle de matrícula'
    detalles.value = []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    id: null,
    matriculaId: '',
    cursoId: '',
    nombreCurso: '',
    codigoCurso: '',
    horasSemanales: 1,
    tipoInscripcion: 'OBLIGATORIO',
    estado: 'INSCRITO',
  })

  formRef.value?.resetValidation?.()
}

function abrirNuevo() {
  errorMsg.value = ''
  successMsg.value = ''
  resetForm()
  dialog.value = true
}

function editar(item: DetalleMatricula) {
  errorMsg.value = ''
  successMsg.value = ''

  Object.assign(form, {
    id: item.id ?? null,
    matriculaId: item.matriculaId ?? '',
    cursoId: item.cursoId ?? '',
    nombreCurso: item.nombreCurso ?? nombreCurso(item.cursoId),
    codigoCurso: item.codigoCurso ?? codigoCurso(item.cursoId),
    horasSemanales: item.horasSemanales ?? 1,
    tipoInscripcion: item.tipoInscripcion ?? 'OBLIGATORIO',
    estado: item.estado ?? 'INSCRITO',
  })

  dialog.value = true
}

function verDetalle(item: DetalleMatricula) {
  detalleSeleccionado.value = { ...item }
  dialogDetalle.value = true
}

function cerrarDialog() {
  dialog.value = false
  resetForm()
}

function sincronizarCurso() {
  const curso = cursos.value.find((item) => item.id === form.cursoId)

  if (!curso) {
    form.nombreCurso = ''
    form.codigoCurso = ''
    return
  }

  form.nombreCurso = curso.nombre
  form.codigoCurso = curso.codigo
  form.horasSemanales = Number(curso.horasSemanales || 1)
  form.tipoInscripcion =
    curso.tipo === 'ELECTIVO' ? 'ELECTIVO' : 'OBLIGATORIO'
}

async function guardar() {
  errorMsg.value = ''
  successMsg.value = ''

  const validation = await formRef.value?.validate?.()

  if (validation && !validation.valid) {
    errorMsg.value = 'Revise los campos obligatorios del formulario.'
    return
  }

  saving.value = true

  try {
    const esEdicion = Boolean(form.id)
    const url = esEdicion
      ? `/api/detalles-matricula/${form.id}`
      : `/api/detalles-matricula`

    const payload = {
      matriculaId: form.matriculaId,
      cursoId: form.cursoId,
      nombreCurso: form.nombreCurso,
      codigoCurso: form.codigoCurso,
      horasSemanales: Number(form.horasSemanales),
      tipoInscripcion: form.tipoInscripcion,
      estado: form.estado,
    }

    await matricula(url, {
      method: esEdicion ? 'PUT' : 'POST',
      body: payload,
    })

    cerrarDialog()
    await cargarDetalles()

    successMsg.value = esEdicion
      ? 'Curso inscrito actualizado correctamente.'
      : 'Curso agregado a la matrícula correctamente.'
  } catch (error: any) {
    errorMsg.value =
      error.message || 'Error al guardar el detalle de matrícula'
  } finally {
    saving.value = false
  }
}

async function eliminar(item: DetalleMatricula) {
  if (!item.id) return

  const confirmado = await confirmAction(
    `¿Está seguro de eliminar el curso "${item.nombreCurso || nombreCurso(item.cursoId)}" de esta matrícula?`
  )

  if (!confirmado) return

  errorMsg.value = ''
  successMsg.value = ''

  try {
    await matricula(`/api/detalles-matricula/${item.id}`, {
      method: 'DELETE',
    })

    await cargarDetalles()
    successMsg.value = 'Curso eliminado de la matrícula correctamente.'
  } catch (error: any) {
    errorMsg.value =
      error.message || 'Error al eliminar el detalle de matrícula'
  }
}

function nombreEstudiante(matriculaId: string) {
  return (
    matriculas.value.find((item) => item.id === matriculaId)
      ?.nombreEstudiante || 'Estudiante no encontrado'
  )
}

function resumenMatricula(matriculaId: string) {
  const matricula = matriculas.value.find((item) => item.id === matriculaId)

  if (!matricula) return matriculaId || '-'

  return `${matricula.grado || '-'} ${matricula.seccion || ''} · ${matricula.periodo || '-'}`
}

function nombreCurso(cursoId: string) {
  return (
    cursos.value.find((item) => item.id === cursoId)?.nombre ||
    'Curso no encontrado'
  )
}

function codigoCurso(cursoId: string) {
  return cursos.value.find((item) => item.id === cursoId)?.codigo || cursoId || '-'
}

function colorEstado(estado?: string) {
  if (estado === 'INSCRITO') return 'info'
  if (estado === 'APROBADO') return 'success'
  if (estado === 'DESAPROBADO') return 'error'
  if (estado === 'RETIRADO') return 'warning'
  return 'default'
}

onMounted(async () => {
  await cargarTodo()
})
</script>