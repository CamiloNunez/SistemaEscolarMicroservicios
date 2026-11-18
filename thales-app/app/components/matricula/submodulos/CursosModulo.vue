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
          Nuevo curso
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
        <v-col cols="12" md="6">
          <v-text-field
            v-model="busqueda"
            label="Buscar por código, nombre, área o descripción"
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            clearable
            hide-details
          />
        </v-col>
        <v-col cols="12" md="3">
          <v-select
            v-model="filtroTipo"
            :items="['TODOS', ...tipos]"
            label="Tipo"
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
          <p class="text-caption text-grey-darken-1 mb-1">Total de cursos</p>
          <p class="text-h5 font-weight-bold mb-0">{{ cursos.length }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Cursos activos</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalActivos }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Horas semanales</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalHoras }}</p>
        </v-card>
      </v-col>
    </v-row>

    <v-card class="main-card">
      <v-data-table
        :headers="headers"
        :items="cursosFiltrados"
        :loading="loading"
        item-value="id"
        no-data-text="No hay cursos registrados"
        loading-text="Cargando cursos..."
      >
        <template #item.codigo="{ item }">
          <v-chip size="small" variant="tonal" color="primary">
            {{ item.codigo }}
          </v-chip>
        </template>
        <template #item.nombre="{ item }">
          <div class="py-2">
            <p class="font-weight-bold mb-0">{{ item.nombre }}</p>
            <p class="text-caption text-grey-darken-1 mb-0">
              {{ item.descripcion || 'Sin descripción' }}
            </p>
          </div>
        </template>
        <template #item.tipo="{ item }">
          <v-chip
            size="small"
            variant="tonal"
            :color="item.tipo === 'OBLIGATORIO' ? 'primary' : 'secondary'"
          >
            {{ item.tipo || 'SIN TIPO' }}
          </v-chip>
        </template>
        <template #item.horasSemanales="{ item }">
          {{ item.horasSemanales ?? 0 }} h
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
              @click="eliminar(item)"
            />
          </div>
        </template>
      </v-data-table>
    </v-card>

    <!-- Diálogo de edición/creación -->
    <v-dialog v-model="dialog" max-width="760" persistent>
      <v-card class="pa-5">
        <DialogHeader
          :title="form.id ? 'Editar curso' : 'Nuevo curso'"
          subtitle="Complete los datos del curso académico."
          @close="cerrarDialog"
        />

        <v-form ref="formRef">
          <v-row>
            <v-col cols="12" md="4">
              <v-text-field
                v-model.trim="form.codigo"
                label="Código"
                variant="outlined"
                density="compact"
                maxlength="15"
                counter="15"
                :rules="[rules.required, rules.codigo]"
                @update:model-value="form.codigo = normalizarCodigo(form.codigo)"
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
            <v-col cols="12" md="6">
              <v-combobox
                v-model="form.area"
                :items="areasSugeridas"
                label="Área curricular"
                variant="outlined"
                density="compact"
                clearable
                :rules="[rules.required]"
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-select
                v-model="form.tipo"
                :items="tipos"
                label="Tipo"
                variant="outlined"
                density="compact"
                :rules="[rules.required]"
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                v-model.number="form.horasSemanales"
                label="Horas semanales"
                type="number"
                variant="outlined"
                density="compact"
                min="1"
                max="60"
                :rules="[rules.requiredNumber, rules.horas]"
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

    <!-- Diálogo de detalle -->
    <v-dialog v-model="dialogDetalle" max-width="650">
      <v-card class="pa-5">
        <DialogHeader
          title="Detalle del curso"
          subtitle="Información registrada en el catálogo académico."
          @close="dialogDetalle = false"
        />
        <v-row v-if="cursoDetalle">
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Código</p>
            <v-chip size="small" color="primary" variant="tonal">
              {{ cursoDetalle.codigo }}
            </v-chip>
          </v-col>
          <v-col cols="12" md="8">
            <p class="text-caption text-grey-darken-1 mb-1">Nombre</p>
            <p class="font-weight-bold">{{ cursoDetalle.nombre }}</p>
          </v-col>
          <v-col cols="12">
            <p class="text-caption text-grey-darken-1 mb-1">Descripción</p>
            <p>{{ cursoDetalle.descripcion || '-' }}</p>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Área</p>
            <p class="font-weight-bold">{{ cursoDetalle.area || '-' }}</p>
          </v-col>
          <v-col cols="12" md="3">
            <p class="text-caption text-grey-darken-1 mb-1">Tipo</p>
            <p class="font-weight-bold">{{ cursoDetalle.tipo || '-' }}</p>
          </v-col>
          <v-col cols="12" md="3">
            <p class="text-caption text-grey-darken-1 mb-1">Horas</p>
            <p class="font-weight-bold">{{ cursoDetalle.horasSemanales ?? 0 }} h</p>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Estado</p>
            <v-chip size="small" :color="colorEstado(cursoDetalle.estado)" variant="tonal">
              {{ cursoDetalle.estado || 'SIN ESTADO' }}
            </v-chip>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Usuario registro</p>
            <p>{{ cursoDetalle.usuarioRegistro || '-' }}</p>
          </v-col>
        </v-row>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { useMatriculaHttp } from '~/composables/useMatriculaHttp'
const { confirm: confirmAction } = useUiFeedback()

// ============================================================
// Tipos
// ============================================================
interface Curso {
  id: string | null
  codigo: string
  nombre: string
  descripcion: string
  area: string
  tipo: string
  horasSemanales: number | null
  estado: string
  usuarioRegistro?: string
  fechaRegistro?: string
  fechaActualizacion?: string
}

// ============================================================
// Composable y refs de estado
// ============================================================
const { matricula } = useMatriculaHttp()

const loading = ref(false)
const saving = ref(false)
const dialog = ref(false)
const dialogDetalle = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const busqueda = ref('')
const filtroTipo = ref('TODOS')
const filtroEstado = ref('TODOS')
const formRef = ref()

const cursos = ref<Curso[]>([])
const cursoDetalle = ref<Curso | null>(null)

// ============================================================
// Constantes de dominio
// ============================================================
const tipos = ['OBLIGATORIO', 'ELECTIVO']
const estados = ['ACTIVO', 'INACTIVO']
const areasSugeridas = [
  'MATEMÁTICA',
  'COMUNICACIÓN',
  'CIENCIAS',
  'CIENCIAS SOCIALES',
  'INGLÉS',
  'ARTE',
  'EDUCACIÓN FÍSICA',
  'TECNOLOGÍA',
  'FORMACIÓN RELIGIOSA',
  'TUTORÍA',
]

const headers = [
  { title: 'Código', key: 'codigo' },
  { title: 'Curso', key: 'nombre' },
  { title: 'Área', key: 'area' },
  { title: 'Tipo', key: 'tipo' },
  { title: 'Horas', key: 'horasSemanales' },
  { title: 'Estado', key: 'estado' },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'end' as const },
]

// ============================================================
// Reglas de validación
// ============================================================
const rules = {
  required: (value: string) => !!String(value || '').trim() || 'Campo requerido',
  requiredNumber: (value: number | null) =>
    (value !== null && value !== undefined && value !== 0) || 'Campo requerido',
  codigo: (value: string) =>
    /^[A-Z0-9_-]{2,15}$/.test(value || '') ||
    'Use entre 2 y 15 caracteres: letras, números, guion o guion bajo',
  horas: (value: number | null) =>
    (value !== null && Number(value) >= 1 && Number(value) <= 60) ||
    'Debe estar entre 1 y 60 horas',
}

// ============================================================
// Formulario reactivo
// ============================================================
const form = reactive<Curso>(crearFormVacio())

// ============================================================
// Funciones auxiliares puras
// ============================================================
function crearFormVacio(): Curso {
  return {
    id: null,
    codigo: '',
    nombre: '',
    descripcion: '',
    area: '',
    tipo: 'OBLIGATORIO',
    horasSemanales: 1,
    estado: 'ACTIVO',
  }
}

function limpiarTexto(value: string | null | undefined): string | null {
  const limpio = String(value ?? '').trim()
  return limpio || null
}

function normalizarCurso(item: any): Curso {
  return {
    id: item.id ?? null,
    codigo: item.codigo ?? '',
    nombre: item.nombre ?? '',
    descripcion: item.descripcion ?? '',
    area: item.area ?? '',
    tipo: item.tipo ?? 'OBLIGATORIO',
    horasSemanales: item.horasSemanales ?? 0,
    estado: item.estado ?? 'ACTIVO',
    usuarioRegistro: item.usuarioRegistro,
    fechaRegistro: item.fechaRegistro,
    fechaActualizacion: item.fechaActualizacion,
  }
}

function normalizarCodigo(value: string): string {
  return String(value || '')
    .toUpperCase()
    .replace(/\s+/g, '_')
    .replace(/[^A-Z0-9_-]/g, '')
    .slice(0, 15)
}

function construirPayload(): Omit<Curso, 'id' | 'usuarioRegistro' | 'fechaRegistro' | 'fechaActualizacion'> {
  return {
    codigo: normalizarCodigo(form.codigo),
    nombre: form.nombre.trim(),
    descripcion: form.descripcion.trim(),
    area: form.area.trim().toUpperCase(),
    tipo: form.tipo,
    horasSemanales: Number(form.horasSemanales),
    estado: form.estado,
  }
}

function colorEstado(estado?: string): string {
  return estado === 'ACTIVO' ? 'success' : 'default'
}

// ============================================================
// Computed: filtros y totales
// ============================================================
const cursosFiltrados = computed(() => {
  const texto = busqueda.value?.trim().toLowerCase() || ''

  return cursos.value.filter((curso) => {
    const coincideTexto =
      !texto ||
      [curso.codigo, curso.nombre, curso.descripcion, curso.area].some((valor) =>
        String(valor || '').toLowerCase().includes(texto)
      )

    const coincideTipo = filtroTipo.value === 'TODOS' || curso.tipo === filtroTipo.value
    const coincideEstado = filtroEstado.value === 'TODOS' || curso.estado === filtroEstado.value

    return coincideTexto && coincideTipo && coincideEstado
  })
})

const totalActivos = computed(() =>
  cursos.value.filter((curso) => curso.estado === 'ACTIVO').length
)

const totalHoras = computed(() =>
  cursos.value.reduce((total, curso) => total + Number(curso.horasSemanales || 0), 0)
)

// ============================================================
// Carga de datos
// ============================================================
async function cargar() {
  loading.value = true
  errorMsg.value = ''

  try {
    const data: any = await matricula('/api/cursos')

    const lista = Array.isArray(data)
      ? data
      : Array.isArray(data?.cursos)
        ? data.cursos
        : []

    cursos.value = lista.map(normalizarCurso)
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al cargar los cursos'
    cursos.value = []
  } finally {
    loading.value = false
  }
}

// ============================================================
// Operaciones del formulario
// ============================================================
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

function editar(item: Curso) {
  errorMsg.value = ''
  successMsg.value = ''
  Object.assign(form, normalizarCurso(item))
  dialog.value = true
}

function verDetalle(item: Curso) {
  cursoDetalle.value = normalizarCurso(item)
  dialogDetalle.value = true
}

function cerrarDialog() {
  dialog.value = false
  resetForm()
}

// ============================================================
// Guardar (crear / actualizar)
// ============================================================
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
    const esEdicion = Boolean(form.id)
    const url = esEdicion ? `/api/cursos/${form.id}` : '/api/cursos'
    const method = esEdicion ? 'PUT' : 'POST'

    await matricula(url, {
      method,
      body: JSON.stringify(payload),
    })

    cerrarDialog()
    await cargar()

    successMsg.value = esEdicion
      ? 'Curso actualizado correctamente.'
      : 'Curso registrado correctamente.'
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al guardar el curso'
  } finally {
    saving.value = false
  }
}

// ============================================================
// Eliminar
// ============================================================
async function eliminar(item: Curso) {
  if (!item.id) return

  const confirmado = await confirmAction(`¿Está seguro de eliminar el curso "${item.nombre}"?`)
  if (!confirmado) return

  errorMsg.value = ''
  successMsg.value = ''

  try {
    const data: any = await matricula(`/api/cursos/${item.id}`, {
      method: 'DELETE',
    })

    await cargar()
    successMsg.value = data?.mensaje || 'Curso eliminado correctamente.'
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al eliminar el curso'
  }
}

// ============================================================
// Ciclo de vida
// ============================================================
onMounted(() => {
  cargar()
})
</script>