<template>
  <div>
    <div class="d-flex justify-space-between align-center mb-4 flex-wrap ga-3">
      <div class="d-flex ga-2 flex-wrap">
        <v-btn variant="tonal" color="primary" :loading="loading" @click="cargarTodo">
          <v-icon start>mdi-refresh</v-icon>
          Actualizar
        </v-btn>
        <v-btn color="primary" @click="abrirNuevo">
          <v-icon start>mdi-plus</v-icon>
          Nuevo grado
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
        <v-col cols="12" md="5">
          <v-text-field
            v-model="busqueda"
            label="Buscar por nombre, número o descripción"
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            clearable
            hide-details
          />
        </v-col>
        <v-col cols="12" md="4">
          <v-select
            v-model="filtroPrograma"
            :items="programasFiltro"
            item-title="title"
            item-value="value"
            label="Programa académico"
            variant="outlined"
            density="compact"
            hide-details
          />
        </v-col>
        <v-col cols="12" md="3">
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
          <p class="text-caption text-grey-darken-1 mb-1">Total de grados</p>
          <p class="text-h5 font-weight-bold mb-0">{{ grados.length }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Grados activos</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalActivos }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Programas asociados</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalProgramas }}</p>
        </v-card>
      </v-col>
    </v-row>

    <v-card class="main-card">
      <v-data-table
        :headers="headers"
        :items="gradosFiltrados"
        :loading="loading"
        item-value="id"
        no-data-text="No hay grados registrados"
        loading-text="Cargando grados..."
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
        <template #item.numero="{ item }">
          <v-chip size="small" color="primary" variant="tonal">
            {{ item.numero }}
          </v-chip>
        </template>
        <template #item.edades="{ item }">
          {{ textoEdades(item) }}
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

    <!-- Diálogo crear / editar -->
    <v-dialog v-model="dialog" max-width="760" persistent>
      <v-card class="pa-5">
        <DialogHeader
          :title="form.id ? 'Editar grado' : 'Nuevo grado'"
          subtitle="Configure el grado y su programa académico."
          @close="cerrarDialog"
        />

        <v-form ref="formRef">
          <v-row>
            <v-col cols="12">
              <v-select
                v-model="form.programaAcademicoId"
                :items="programasSelect"
                item-title="title"
                item-value="value"
                label="Programa académico"
                variant="outlined"
                density="compact"
                :loading="loadingProgramas"
                :rules="[rules.required]"
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model.number="form.numero"
                label="Número de grado"
                type="number"
                min="1"
                max="20"
                variant="outlined"
                density="compact"
                :rules="[rules.requiredNumber, rules.numero]"
              />
            </v-col>
            <v-col cols="12" md="8">
              <v-text-field
                v-model.trim="form.nombre"
                label="Nombre"
                variant="outlined"
                density="compact"
                maxlength="100"
                counter="100"
                :rules="[rules.required]"
                hint="Ejemplo: Primer Grado o 1° de Primaria"
              />
            </v-col>
            <v-col cols="12">
              <v-textarea
                v-model.trim="form.descripcion"
                label="Descripción"
                variant="outlined"
                density="compact"
                rows="3"
                maxlength="300"
                counter="300"
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model.number="form.edadMinima"
                label="Edad mínima"
                type="number"
                min="0"
                max="30"
                variant="outlined"
                density="compact"
                :rules="[rules.edadOpcional]"
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model.number="form.edadMaxima"
                label="Edad máxima"
                type="number"
                min="0"
                max="30"
                variant="outlined"
                density="compact"
                :rules="[rules.edadOpcional, rules.rangoEdades]"
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
        </v-form>

        <div class="d-flex justify-end ga-2 mt-3">
          <v-btn variant="text" @click="cerrarDialog">Cancelar</v-btn>
          <v-btn color="primary" :loading="saving" @click="guardar">Guardar</v-btn>
        </div>
      </v-card>
    </v-dialog>

    <!-- Diálogo detalle -->
    <v-dialog v-model="dialogDetalle" max-width="650">
      <v-card class="pa-5">
        <DialogHeader
          title="Detalle del grado"
          subtitle="Información académica registrada."
          @close="dialogDetalle = false"
        />

        <v-row v-if="gradoDetalle">
          <v-col cols="12">
            <p class="text-caption text-grey-darken-1 mb-1">Programa académico</p>
            <p class="font-weight-bold mb-0">
              {{ nombrePrograma(gradoDetalle.programaAcademicoId) }}
            </p>
            <p class="text-caption text-grey-darken-1">
              {{ codigoPrograma(gradoDetalle.programaAcademicoId) }}
            </p>
          </v-col>
          <v-col cols="12" md="3">
            <p class="text-caption text-grey-darken-1 mb-1">Número</p>
            <v-chip size="small" color="primary" variant="tonal">
              {{ gradoDetalle.numero }}
            </v-chip>
          </v-col>
          <v-col cols="12" md="9">
            <p class="text-caption text-grey-darken-1 mb-1">Nombre</p>
            <p class="font-weight-bold">{{ gradoDetalle.nombre }}</p>
          </v-col>
          <v-col cols="12">
            <p class="text-caption text-grey-darken-1 mb-1">Descripción</p>
            <p>{{ gradoDetalle.descripcion || '-' }}</p>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Rango recomendado</p>
            <p class="font-weight-bold">{{ textoEdades(gradoDetalle) }}</p>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Estado</p>
            <v-chip
              size="small"
              :color="colorEstado(gradoDetalle.estado)"
              variant="tonal"
            >
              {{ gradoDetalle.estado || 'SIN ESTADO' }}
            </v-chip>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Usuario registro</p>
            <p>{{ gradoDetalle.usuarioRegistro || '-' }}</p>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Fecha de registro</p>
            <p>{{ formatearFecha(gradoDetalle.fechaRegistro) }}</p>
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
  nivelEducativo?: string
  estado?: string
}

interface Grado {
  id: string | null
  programaAcademicoId: string
  numero: number | null
  nombre: string
  descripcion: string
  edadMinima: number | null
  edadMaxima: number | null
  estado: string
  usuarioRegistro?: string
  fechaRegistro?: string
  fechaActualizacion?: string
}

const { matricula } = useMatriculaHttp()

const loading = ref(false)
const loadingProgramas = ref(false)
const saving = ref(false)
const dialog = ref(false)
const dialogDetalle = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const busqueda = ref('')
const filtroPrograma = ref('TODOS')
const filtroEstado = ref('TODOS')
const formRef = ref()

const grados = ref<Grado[]>([])
const programas = ref<ProgramaAcademico[]>([])
const gradoDetalle = ref<Grado | null>(null)

const estados = ['ACTIVO', 'INACTIVO']

const headers = [
  { title: 'Programa', key: 'programaAcademicoId' },
  { title: 'N.°', key: 'numero' },
  { title: 'Grado', key: 'nombre' },
  { title: 'Edades', key: 'edades', sortable: false },
  { title: 'Estado', key: 'estado' },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'end' as const },
]

const form = reactive<Grado>(crearFormVacio())

const rules = {
  required: (value: string) =>
    !!String(value || '').trim() || 'Campo requerido',
  requiredNumber: (value: number | null) =>
    value !== null && value !== undefined || 'Campo requerido',
  numero: (value: number | null) =>
    (value !== null && Number(value) >= 1 && Number(value) <= 20) ||
    'Debe estar entre 1 y 20',
  edadOpcional: (value: number | null) =>
    value === null ||
    value === undefined ||
    value === ('' as any) ||
    (Number(value) >= 0 && Number(value) <= 30) ||
    'Debe estar entre 0 y 30',
  rangoEdades: (value: number | null) =>
    value === null ||
    value === undefined ||
    form.edadMinima === null ||
    Number(value) >= Number(form.edadMinima) ||
    'La edad máxima no puede ser menor que la edad mínima',
}

// ---------- Computed ----------
const programasSelect = computed(() =>
  programas.value.map((programa) => ({
    title: `${programa.nombre} (${programa.codigo})`,
    value: programa.id,
  }))
)

const programasFiltro = computed(() => [
  { title: 'TODOS', value: 'TODOS' },
  ...programasSelect.value,
])

const gradosFiltrados = computed(() => {
  const texto = busqueda.value?.trim().toLowerCase() || ''

  return grados.value.filter((grado) => {
    const programaNombre = nombrePrograma(grado.programaAcademicoId)
    const programaCodigo = codigoPrograma(grado.programaAcademicoId)

    const coincideTexto =
      !texto ||
      [
        grado.numero,
        grado.nombre,
        grado.descripcion,
        programaNombre,
        programaCodigo,
      ].some((valor) =>
        String(valor ?? '').toLowerCase().includes(texto)
      )

    const coincidePrograma =
      filtroPrograma.value === 'TODOS' ||
      grado.programaAcademicoId === filtroPrograma.value

    const coincideEstado =
      filtroEstado.value === 'TODOS' ||
      grado.estado === filtroEstado.value

    return coincideTexto && coincidePrograma && coincideEstado
  })
})

const totalActivos = computed(() =>
  grados.value.filter((grado) => grado.estado === 'ACTIVO').length
)

const totalProgramas = computed(() =>
  new Set(
    grados.value
      .map((grado) => grado.programaAcademicoId)
      .filter(Boolean)
  ).size
)

// ---------- Helpers ----------
function crearFormVacio(): Grado {
  return {
    id: null,
    programaAcademicoId: '',
    numero: 1,
    nombre: '',
    descripcion: '',
    edadMinima: null,
    edadMaxima: null,
    estado: 'ACTIVO',
  }
}

function limpiarTexto(value: string | null | undefined): string | null {
  const limpio = String(value ?? '').trim()
  return limpio || null
}

function limpiarNumero(value: number | null | undefined | ''): number | null {
  if (value === null || value === undefined || value === '') return null
  const n = Number(value)
  return Number.isNaN(n) ? null : n
}

function normalizarGrado(item: any): Grado {
  return {
    id: item.id ?? null,
    programaAcademicoId: item.programaAcademicoId ?? '',
    numero: limpiarNumero(item.numero),
    nombre: item.nombre ?? '',
    descripcion: item.descripcion ?? '',
    edadMinima: limpiarNumero(item.edadMinima),
    edadMaxima: limpiarNumero(item.edadMaxima),
    estado: item.estado ?? 'ACTIVO',
    usuarioRegistro: item.usuarioRegistro,
    fechaRegistro: item.fechaRegistro,
    fechaActualizacion: item.fechaActualizacion,
  }
}

function normalizarPrograma(item: any): ProgramaAcademico {
  return {
    id: item.id ?? '',
    codigo: item.codigo ?? '',
    nombre: item.nombre ?? '',
    nivelEducativo: item.nivelEducativo,
    estado: item.estado,
  }
}

function construirPayload() {
  return {
    programaAcademicoId: form.programaAcademicoId,
    numero: Number(form.numero),
    nombre: limpiarTexto(form.nombre),
    descripcion: limpiarTexto(form.descripcion),
    edadMinima: limpiarNumero(form.edadMinima),
    edadMaxima: limpiarNumero(form.edadMaxima),
    estado: limpiarTexto(form.estado) || 'ACTIVO',
  }
}

function nombrePrograma(programaId: string) {
  return (
    programas.value.find((programa) => programa.id === programaId)?.nombre ||
    'Programa no encontrado'
  )
}

function codigoPrograma(programaId: string) {
  return (
    programas.value.find((programa) => programa.id === programaId)?.codigo ||
    programaId ||
    '-'
  )
}

function textoEdades(grado: Grado) {
  const minima = grado.edadMinima
  const maxima = grado.edadMaxima

  if (minima === null && maxima === null) return 'Sin rango'
  if (minima !== null && maxima !== null) return `${minima} a ${maxima} años`
  if (minima !== null) return `Desde ${minima} años`
  return `Hasta ${maxima} años`
}

function colorEstado(estado?: string) {
  return estado === 'ACTIVO' ? 'success' : 'default'
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

// ---------- Carga de datos ----------
async function cargarProgramas() {
  loadingProgramas.value = true
  try {
    const data: any = await matricula('/api/programas-academicos')
    const lista = Array.isArray(data)
      ? data
      : Array.isArray(data?.programas)
        ? data.programas
        : []
    programas.value = lista.map(normalizarPrograma)
  } catch (error: any) {
    throw new Error(
      error.data?.mensaje || error.message || 'No se pudo listar los programas académicos'
    )
  } finally {
    loadingProgramas.value = false
  }
}

async function cargarGrados() {
  const data: any = await matricula('/api/grados')
  const lista = Array.isArray(data)
    ? data
    : Array.isArray(data?.grados)
      ? data.grados
      : []
  grados.value = lista.map(normalizarGrado)
}

async function cargarTodo() {
  loading.value = true
  errorMsg.value = ''

  try {
    await Promise.all([cargarProgramas(), cargarGrados()])
  } catch (error: any) {
    errorMsg.value = error.message || 'Error al cargar los grados'
    grados.value = []
  } finally {
    loading.value = false
  }
}

// ---------- UI ----------
function resetForm() {
  Object.assign(form, crearFormVacio())
  formRef.value?.resetValidation?.()
}

function abrirNuevo() {
  errorMsg.value = ''
  successMsg.value = ''
  resetForm()
  dialog.value = true
}

function editar(item: Grado) {
  errorMsg.value = ''
  successMsg.value = ''
  Object.assign(form, normalizarGrado(item))
  dialog.value = true
}

function verDetalle(item: Grado) {
  gradoDetalle.value = normalizarGrado(item)
  dialogDetalle.value = true
}

function cerrarDialog() {
  dialog.value = false
  resetForm()
}

// ---------- API ----------
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
    const payload = construirPayload()

    if (form.id) {
      await matricula(`/api/grados/${form.id}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      })
      successMsg.value = 'Grado actualizado correctamente.'
    } else {
      await matricula('/api/grados', {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      successMsg.value = 'Grado registrado correctamente.'
    }

    cerrarDialog()
    await cargarGrados()
  } catch (error: any) {
    errorMsg.value =
      error.data?.mensaje || error.message || 'Error al guardar el grado'
  } finally {
    saving.value = false
  }
}

async function eliminar(id: string | null) {
  if (!id) return
  if (!await confirmAction('¿Está seguro de eliminar este grado? Esta acción no se puede deshacer.')) return

  errorMsg.value = ''
  successMsg.value = ''

  try {
    const data: any = await matricula(`/api/grados/${id}`, {
      method: 'DELETE',
    })
    successMsg.value = data?.mensaje || 'Grado eliminado correctamente.'
    await cargarGrados()
  } catch (error: any) {
    errorMsg.value =
      error.data?.mensaje || error.message || 'Error al eliminar el grado'
  }
}

onMounted(() => {
  cargarTodo()
})
</script>