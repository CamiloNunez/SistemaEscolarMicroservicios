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
          Nuevo plan
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
            label="Buscar por nombre, programa, grado o periodo"
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            clearable
            hide-details
          />
        </v-col>
        <v-col cols="12" md="3">
          <v-select
            v-model="filtroPrograma"
            :items="programasFiltro"
            item-title="title"
            item-value="value"
            label="Programa"
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
          <p class="text-caption text-grey-darken-1 mb-1">Total de planes</p>
          <p class="text-h5 font-weight-bold mb-0">{{ planes.length }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Planes activos</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalActivos }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Horas semanales acumuladas</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalHorasGlobal }}</p>
        </v-card>
      </v-col>
    </v-row>

    <v-card class="main-card">
      <v-data-table
        :headers="headers"
        :items="planesFiltrados"
        :loading="loading"
        item-value="id"
        no-data-text="No hay planes de estudio registrados"
        loading-text="Cargando planes de estudio..."
      >
        <template #item.programaAcademicoId="{ item }">
          <div>
            <p class="font-weight-bold mb-0">
              {{ nombrePrograma(item.programaAcademicoId) }}
            </p>
            <p class="text-caption text-grey-darken-1 mb-0">
              {{ codigoPrograma(item.programaAcademicoId) }}
            </p>
          </div>
        </template>
        <template #item.gradoId="{ item }">
          {{ nombreGrado(item.gradoId) }}
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
        <template #item.cursos="{ item }">
          <v-chip size="small" variant="tonal" color="primary">
            {{ item.cursos?.length || 0 }} curso{{ (item.cursos?.length || 0) === 1 ? '' : 's' }}
          </v-chip>
        </template>
        <template #item.totalHorasSemanales="{ item }">
          {{ item.totalHorasSemanales ?? calcularTotalHoras(item.cursos) }} h
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

    <v-dialog v-model="dialog" max-width="1200" persistent>
      <v-card class="pa-5">
        <DialogHeader
          :title="form.id ? 'Editar plan de estudios' : 'Nuevo plan de estudios'"
          subtitle="Seleccione los catálogos académicos y configure los cursos del plan."
          @close="cerrarDialog"
        />
        <v-form ref="formRef">
          <v-card variant="outlined" class="pa-4 mb-4">
            <h3 class="text-subtitle-1 font-weight-bold mb-4">
              Datos generales
            </h3>
            <v-row>
              <v-col cols="12" md="4">
                <v-select
                  v-model="form.programaAcademicoId"
                  :items="programasSelect"
                  item-title="title"
                  item-value="value"
                  label="Programa académico"
                  variant="outlined"
                  density="compact"
                  :loading="loadingCatalogos"
                  :rules="[rules.required]"
                  @update:model-value="alCambiarPrograma"
                />
              </v-col>
              <v-col cols="12" md="4">
                <v-select
                  v-model="form.gradoId"
                  :items="gradosDisponiblesSelect"
                  item-title="title"
                  item-value="value"
                  label="Grado"
                  variant="outlined"
                  density="compact"
                  :loading="loadingCatalogos"
                  :disabled="!form.programaAcademicoId"
                  :rules="[rules.required]"
                />
              </v-col>
              <v-col cols="12" md="4">
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
              <v-col cols="12" md="8">
                <v-text-field
                  v-model.trim="form.nombre"
                  label="Nombre del plan"
                  variant="outlined"
                  density="compact"
                  maxlength="150"
                  counter="150"
                  :rules="[rules.required]"
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
            </v-row>
          </v-card>

          <v-card variant="outlined" class="pa-4 mb-4">
            <div class="d-flex justify-space-between align-center flex-wrap ga-3 mb-4">
              <div>
                <h3 class="text-subtitle-1 font-weight-bold mb-1">
                  Cursos del plan
                </h3>
                <p class="text-body-2 text-grey-darken-1 mb-0">
                  Agregue cursos y configure horas, obligatoriedad y orden.
                </p>
              </div>
              <v-btn
                color="primary"
                variant="tonal"
                :disabled="cursosDisponibles.length === 0"
                @click="agregarCurso"
              >
                <v-icon start>mdi-plus</v-icon>
                Agregar curso
              </v-btn>
            </div>

            <v-alert
              v-if="form.cursos.length === 0"
              type="info"
              variant="tonal"
              class="mb-0"
            >
              El plan todavía no tiene cursos agregados.
            </v-alert>

            <div
              v-for="(cursoPlan, index) in form.cursos"
              :key="`${cursoPlan.cursoId}-${index}`"
              class="curso-row pa-3 mb-3"
            >
              <v-row align="center">
                <v-col cols="12" md="4">
                  <v-select
                    v-model="cursoPlan.cursoId"
                    :items="cursosSelectParaFila(index)"
                    item-title="title"
                    item-value="value"
                    label="Curso"
                    variant="outlined"
                    density="compact"
                    hide-details
                    :rules="[rules.required]"
                    @update:model-value="sincronizarCurso(index)"
                  />
                </v-col>
                <v-col cols="12" md="2">
                  <v-text-field
                    v-model.number="cursoPlan.horasSemanales"
                    label="Horas"
                    type="number"
                    min="1"
                    max="40"
                    variant="outlined"
                    density="compact"
                    hide-details
                  />
                </v-col>
                <v-col cols="12" md="2">
                  <v-text-field
                    v-model.number="cursoPlan.orden"
                    label="Orden"
                    type="number"
                    min="1"
                    variant="outlined"
                    density="compact"
                    hide-details
                  />
                </v-col>
                <v-col cols="12" md="3">
                  <v-switch
                    v-model="cursoPlan.esObligatorio"
                    label="Obligatorio"
                    color="primary"
                    hide-details
                  />
                </v-col>
                <v-col cols="12" md="1" class="text-right">
                  <v-btn
                    icon="mdi-delete-outline"
                    variant="text"
                    color="error"
                    @click="quitarCurso(index)"
                  />
                </v-col>
              </v-row>
            </div>

            <v-divider class="my-4" />
            <div class="d-flex justify-space-between align-center flex-wrap ga-3">
              <p class="font-weight-bold mb-0">
                Total de cursos: {{ form.cursos.length }}
              </p>
              <p class="font-weight-bold mb-0">
                Total de horas semanales: {{ totalHorasFormulario }}
              </p>
            </div>
          </v-card>
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

    <v-dialog v-model="dialogDetalle" max-width="900">
      <v-card class="pa-5">
        <DialogHeader
          title="Detalle del plan de estudios"
          subtitle="Información general y cursos incluidos en el plan."
          @close="dialogDetalle = false"
        />
        <v-row v-if="planDetalle">
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Nombre</p>
            <p class="font-weight-bold">{{ planDetalle.nombre }}</p>
          </v-col>
          <v-col cols="12" md="3">
            <p class="text-caption text-grey-darken-1 mb-1">Estado</p>
            <v-chip
              size="small"
              :color="colorEstado(planDetalle.estado)"
              variant="tonal"
            >
              {{ planDetalle.estado || 'SIN ESTADO' }}
            </v-chip>
          </v-col>
          <v-col cols="12" md="3">
            <p class="text-caption text-grey-darken-1 mb-1">Horas totales</p>
            <p class="font-weight-bold">
              {{ planDetalle.totalHorasSemanales ?? calcularTotalHoras(planDetalle.cursos) }} h
            </p>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Programa</p>
            <p class="font-weight-bold">
              {{ nombrePrograma(planDetalle.programaAcademicoId) }}
            </p>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Grado</p>
            <p class="font-weight-bold">
              {{ nombreGrado(planDetalle.gradoId) }}
            </p>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Periodo</p>
            <p class="font-weight-bold">
              {{ nombrePeriodo(planDetalle.periodoAcademicoId) }}
            </p>
          </v-col>
          <v-col cols="12">
            <p class="text-caption text-grey-darken-1 mb-2">Cursos</p>
            <v-table density="compact">
              <thead>
                <tr>
                  <th>Orden</th>
                  <th>Curso</th>
                  <th>Horas</th>
                  <th>Tipo</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="curso in cursosDetalleOrdenados"
                  :key="curso.cursoId"
                >
                  <td>{{ curso.orden }}</td>
                  <td>{{ curso.nombreCurso || nombreCurso(curso.cursoId) }}</td>
                  <td>{{ curso.horasSemanales }} h</td>
                  <td>
                    <v-chip
                      size="small"
                      variant="tonal"
                      :color="curso.esObligatorio ? 'primary' : 'secondary'"
                    >
                      {{ curso.esObligatorio ? 'OBLIGATORIO' : 'ELECTIVO' }}
                    </v-chip>
                  </td>
                </tr>
              </tbody>
            </v-table>
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
}

interface PeriodoAcademico {
  id: string
  codigo: string
  nombre: string
}

interface Curso {
  id: string
  codigo: string
  nombre: string
  horasSemanales?: number
  estado?: string
}

interface CursoEnPlan {
  cursoId: string
  nombreCurso: string
  horasSemanales: number
  esObligatorio: boolean
  orden: number
}

interface PlanEstudios {
  id: string | null
  programaAcademicoId: string
  gradoId: string
  periodoAcademicoId: string
  nombre: string
  totalHorasSemanales: number
  cursos: CursoEnPlan[]
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
const filtroPrograma = ref('TODOS')
const filtroPeriodo = ref('TODOS')
const filtroEstado = ref('TODOS')
const formRef = ref()

const planes = ref<PlanEstudios[]>([])
const programas = ref<ProgramaAcademico[]>([])
const grados = ref<Grado[]>([])
const periodos = ref<PeriodoAcademico[]>([])
const cursos = ref<Curso[]>([])
const planDetalle = ref<PlanEstudios | null>(null)

const estados = ['ACTIVO', 'INACTIVO', 'BORRADOR']

const headers = [
  { title: 'Nombre', key: 'nombre' },
  { title: 'Programa', key: 'programaAcademicoId' },
  { title: 'Grado', key: 'gradoId' },
  { title: 'Periodo', key: 'periodoAcademicoId' },
  { title: 'Cursos', key: 'cursos', sortable: false },
  { title: 'Horas', key: 'totalHorasSemanales' },
  { title: 'Estado', key: 'estado' },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'end' as const },
]

const rules = {
  required: (value: string) =>
    !!String(value || '').trim() || 'Campo requerido',
}

const form = reactive<PlanEstudios>({
  id: null,
  programaAcademicoId: '',
  gradoId: '',
  periodoAcademicoId: '',
  nombre: '',
  totalHorasSemanales: 0,
  cursos: [],
  estado: 'BORRADOR',
})

const programasSelect = computed(() =>
  programas.value.map((item) => ({
    title: `${item.nombre} (${item.codigo})`,
    value: item.id,
  }))
)

const programasFiltro = computed(() => [
  { title: 'TODOS', value: 'TODOS' },
  ...programasSelect.value,
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

const gradosDisponibles = computed(() =>
  grados.value.filter(
    (grado) => grado.programaAcademicoId === form.programaAcademicoId
  )
)

const gradosDisponiblesSelect = computed(() =>
  gradosDisponibles.value.map((item) => ({
    title: item.nombre,
    value: item.id,
  }))
)

const cursosDisponibles = computed(() =>
  cursos.value.filter((curso) => curso.estado !== 'INACTIVO')
)

const totalHorasFormulario = computed(() =>
  calcularTotalHoras(form.cursos)
)

const totalActivos = computed(() =>
  planes.value.filter((plan) => plan.estado === 'ACTIVO').length
)

const totalHorasGlobal = computed(() =>
  planes.value.reduce(
    (total, plan) =>
      total +
      Number(
        plan.totalHorasSemanales ?? calcularTotalHoras(plan.cursos)
      ),
    0
  )
)

const planesFiltrados = computed(() => {
  const texto = busqueda.value?.trim().toLowerCase() || ''

  return planes.value.filter((plan) => {
    const coincideTexto =
      !texto ||
      [
        plan.nombre,
        nombrePrograma(plan.programaAcademicoId),
        nombreGrado(plan.gradoId),
        nombrePeriodo(plan.periodoAcademicoId),
      ].some((valor) =>
        String(valor || '').toLowerCase().includes(texto)
      )

    const coincidePrograma =
      filtroPrograma.value === 'TODOS' ||
      plan.programaAcademicoId === filtroPrograma.value

    const coincidePeriodo =
      filtroPeriodo.value === 'TODOS' ||
      plan.periodoAcademicoId === filtroPeriodo.value

    const coincideEstado =
      filtroEstado.value === 'TODOS' ||
      plan.estado === filtroEstado.value

    return (
      coincideTexto &&
      coincidePrograma &&
      coincidePeriodo &&
      coincideEstado
    )
  })
})

const cursosDetalleOrdenados = computed(() =>
  [...(planDetalle.value?.cursos || [])].sort(
    (a, b) => Number(a.orden || 0) - Number(b.orden || 0)
  )
)

async function cargarCatalogos() {
  loadingCatalogos.value = true

  try {
    const [programasData, gradosData, periodosData, cursosData] =
      await Promise.all([
        matricula('/api/programas-academicos'),
        matricula('/api/grados'),
        matricula('/api/periodos-academicos'),
        matricula('/api/cursos'),
      ])

    programas.value = Array.isArray(programasData) ? programasData : []
    grados.value = Array.isArray(gradosData) ? gradosData : []
    periodos.value = Array.isArray(periodosData) ? periodosData : []
    cursos.value = Array.isArray(cursosData) ? cursosData : []
  } catch (error: any) {
    throw new Error(error.message || 'Error al cargar los catálogos académicos')
  } finally {
    loadingCatalogos.value = false
  }
}

async function cargarPlanes() {
  const data = await matricula('/api/planes-estudio')
  planes.value = Array.isArray(data) ? data : []
}

async function cargarTodo() {
  loading.value = true
  errorMsg.value = ''

  try {
    await Promise.all([cargarCatalogos(), cargarPlanes()])
  } catch (error: any) {
    errorMsg.value = error.message || 'Error al cargar los planes de estudio'
    planes.value = []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    id: null,
    programaAcademicoId: '',
    gradoId: '',
    periodoAcademicoId: '',
    nombre: '',
    totalHorasSemanales: 0,
    cursos: [],
    estado: 'BORRADOR',
  })

  formRef.value?.resetValidation?.()
}

function abrirNuevo() {
  errorMsg.value = ''
  successMsg.value = ''
  resetForm()
  dialog.value = true
}

function editar(item: PlanEstudios) {
  errorMsg.value = ''
  successMsg.value = ''

  Object.assign(form, {
    id: item.id ?? null,
    programaAcademicoId: item.programaAcademicoId ?? '',
    gradoId: item.gradoId ?? '',
    periodoAcademicoId: item.periodoAcademicoId ?? '',
    nombre: item.nombre ?? '',
    totalHorasSemanales:
      item.totalHorasSemanales ?? calcularTotalHoras(item.cursos),
    cursos: (item.cursos || []).map((curso) => ({
      cursoId: curso.cursoId,
      nombreCurso: curso.nombreCurso || nombreCurso(curso.cursoId),
      horasSemanales: Number(curso.horasSemanales || 1),
      esObligatorio: curso.esObligatorio ?? true,
      orden: Number(curso.orden || 1),
    })),
    estado: item.estado ?? 'BORRADOR',
  })

  dialog.value = true
}

function verDetalle(item: PlanEstudios) {
  planDetalle.value = {
    ...item,
    cursos: (item.cursos || []).map((curso) => ({ ...curso })),
  }
  dialogDetalle.value = true
}

function cerrarDialog() {
  dialog.value = false
  resetForm()
}

function alCambiarPrograma() {
  const gradoValido = gradosDisponibles.value.some(
    (grado) => grado.id === form.gradoId
  )

  if (!gradoValido) {
    form.gradoId = ''
  }
}

function agregarCurso() {
  const cursoDisponible = cursosDisponibles.value.find(
    (curso) =>
      !form.cursos.some((cursoPlan) => cursoPlan.cursoId === curso.id)
  )

  if (!cursoDisponible) {
    errorMsg.value = 'No hay más cursos disponibles para agregar.'
    return
  }

  form.cursos.push({
    cursoId: cursoDisponible.id,
    nombreCurso: cursoDisponible.nombre,
    horasSemanales: Number(cursoDisponible.horasSemanales || 1),
    esObligatorio: true,
    orden: form.cursos.length + 1,
  })
}

function quitarCurso(index: number) {
  form.cursos.splice(index, 1)
  form.cursos.forEach((curso, posicion) => {
    curso.orden = posicion + 1
  })
}

function sincronizarCurso(index: number) {
  const fila = form.cursos[index]

  if (!fila) return

  const curso = cursos.value.find(
    (item) => item.id === fila.cursoId
  )

  if (!curso) return

  fila.nombreCurso = curso.nombre

  fila.horasSemanales = Number(
    curso.horasSemanales || fila.horasSemanales || 1
  )
}

function cursosSelectParaFila(index: number) {
  const actualId = form.cursos[index]?.cursoId

  return cursosDisponibles.value
    .filter(
      (curso) =>
        curso.id === actualId ||
        !form.cursos.some(
          (cursoPlan, posicion) =>
            posicion !== index && cursoPlan.cursoId === curso.id
        )
    )
    .map((curso) => ({
      title: `${curso.nombre} (${curso.codigo})`,
      value: curso.id,
    }))
}

function validarCursos() {
  if (form.cursos.length === 0) {
    throw new Error('Debe agregar al menos un curso al plan de estudios.')
  }

  const ids = form.cursos.map((curso) => curso.cursoId)

  if (new Set(ids).size !== ids.length) {
    throw new Error('No puede agregar el mismo curso más de una vez.')
  }

  const ordenes = form.cursos.map((curso) => Number(curso.orden))

  if (ordenes.some((orden) => !orden || orden < 1)) {
    throw new Error('Todos los cursos deben tener un orden válido.')
  }

  if (new Set(ordenes).size !== ordenes.length) {
    throw new Error('El orden de los cursos no puede repetirse.')
  }

  if (
    form.cursos.some(
      (curso) =>
        !curso.horasSemanales ||
        Number(curso.horasSemanales) < 1 ||
        Number(curso.horasSemanales) > 40
    )
  ) {
    throw new Error('Las horas de cada curso deben estar entre 1 y 40.')
  }
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
    validarCursos()

    const esEdicion = Boolean(form.id)

    const cursosOrdenados = [...form.cursos]
      .sort((a, b) => Number(a.orden) - Number(b.orden))
      .map((curso) => ({
        cursoId: curso.cursoId,
        nombreCurso: nombreCurso(curso.cursoId),
        horasSemanales: Number(curso.horasSemanales),
        esObligatorio: Boolean(curso.esObligatorio),
        orden: Number(curso.orden),
      }))

    const payload = {
      programaAcademicoId: form.programaAcademicoId,
      gradoId: form.gradoId,
      periodoAcademicoId: form.periodoAcademicoId,
      nombre: form.nombre.trim(),
      totalHorasSemanales: calcularTotalHoras(cursosOrdenados),
      cursos: cursosOrdenados,
      estado: form.estado,
    }

    if (esEdicion) {
      await matricula(`/api/planes-estudio/${form.id}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      })
    } else {
      await matricula('/api/planes-estudio', {
        method: 'POST',
        body: JSON.stringify(payload),
      })
    }

    cerrarDialog()
    await cargarPlanes()

    successMsg.value = esEdicion
      ? 'Plan de estudios actualizado correctamente.'
      : 'Plan de estudios registrado correctamente.'
  } catch (error: any) {
    errorMsg.value = error.message || 'Error al guardar el plan de estudios'
  } finally {
    saving.value = false
  }
}

async function eliminar(item: PlanEstudios) {
  if (!item.id) return

  const confirmado = await confirmAction(
    `¿Está seguro de eliminar el plan "${item.nombre}"?`
  )

  if (!confirmado) return

  errorMsg.value = ''
  successMsg.value = ''

  try {
    await matricula(`/api/planes-estudio/${item.id}`, {
      method: 'DELETE',
    })

    await cargarPlanes()
    successMsg.value = 'Plan de estudios eliminado correctamente.'
  } catch (error: any) {
    errorMsg.value = error.message || 'Error al eliminar el plan de estudios'
  }
}

function calcularTotalHoras(lista: CursoEnPlan[] = []) {
  return lista.reduce(
    (total, curso) => total + Number(curso.horasSemanales || 0),
    0
  )
}

function nombrePrograma(id: string) {
  return programas.value.find((item) => item.id === id)?.nombre || 'No encontrado'
}

function codigoPrograma(id: string) {
  return programas.value.find((item) => item.id === id)?.codigo || id || '-'
}

function nombreGrado(id: string) {
  return grados.value.find((item) => item.id === id)?.nombre || 'No encontrado'
}

function nombrePeriodo(id: string) {
  return periodos.value.find((item) => item.id === id)?.nombre || 'No encontrado'
}

function codigoPeriodo(id: string) {
  return periodos.value.find((item) => item.id === id)?.codigo || id || '-'
}

function nombreCurso(id: string) {
  return cursos.value.find((item) => item.id === id)?.nombre || 'Curso no encontrado'
}

function colorEstado(estado?: string) {
  if (estado === 'ACTIVO') return 'success'
  if (estado === 'BORRADOR') return 'warning'
  return 'default'
}

onMounted(() => {
  cargarTodo()
})
</script>

<style scoped>
.curso-row {
  border: 1px solid var(--color-border, #e5e7eb);
  border-radius: var(--radius-md, 10px);
  background: var(--color-surface-soft, #f8fafc);
}
</style>