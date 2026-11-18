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
          Nuevo programa
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
            label="Buscar por código, nombre o descripción"
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            clearable
            hide-details
          />
        </v-col>
        <v-col cols="12" md="3">
          <v-select
            v-model="filtroNivel"
            :items="['TODOS', ...nivelesEducativos]"
            label="Nivel educativo"
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
          <p class="text-caption text-grey-darken-1 mb-1">Total de programas</p>
          <p class="text-h5 font-weight-bold mb-0">{{ programas.length }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Programas activos</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalActivos }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Años de duración acumulados</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalDuracion }}</p>
        </v-card>
      </v-col>
    </v-row>

    <v-card class="main-card">
      <v-data-table
        :headers="headers"
        :items="programasFiltrados"
        :loading="loading"
        item-value="id"
        no-data-text="No hay programas académicos registrados"
        loading-text="Cargando programas académicos..."
      >
        <template #item.codigo="{ item }">
          <v-chip size="small" color="primary" variant="tonal">
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
        <template #item.nivelEducativo="{ item }">
          <v-chip size="small" variant="tonal" color="secondary">
            {{ item.nivelEducativo || 'SIN NIVEL' }}
          </v-chip>
        </template>
        <template #item.duracionAnios="{ item }">
          {{ item.duracionAnios ?? 0 }} año{{ item.duracionAnios === 1 ? '' : 's' }}
        </template>
        <template #item.estado="{ item }">
          <v-chip size="small" :color="colorEstado(item.estado)" variant="tonal">
            {{ textoEstado(item.estado) }}
          </v-chip>
        </template>
        <template #item.actions="{ item }">
          <div class="d-flex ga-1 justify-end">
            <v-btn icon="mdi-eye-outline" variant="text" size="small" @click="verDetalle(item)" />
            <v-btn icon="mdi-pencil" variant="text" size="small" @click="editar(item)" />
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

    <v-dialog v-model="dialog" max-width="760" persistent>
      <v-card class="pa-5">
        <DialogHeader
          :title="form.id ? 'Editar programa académico' : 'Nuevo programa académico'"
          subtitle="Configure el nivel educativo y la duración del programa."
          @close="cerrarDialog"
        />

        <v-form ref="formRef">
          <v-card variant="outlined" class="pa-4 mb-4">
            <h3 class="text-subtitle-1 font-weight-bold mb-4">Identificación</h3>
            <v-row>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model.trim="form.codigo"
                  label="Código"
                  variant="outlined"
                  density="compact"
                  maxlength="20"
                  counter="20"
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
                  maxlength="120"
                  counter="120"
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
            </v-row>
          </v-card>

          <v-card variant="outlined" class="pa-4 mb-4">
            <h3 class="text-subtitle-1 font-weight-bold mb-4">Configuración académica</h3>
            <v-row>
              <v-col cols="12" md="6">
                <v-select
                  v-model="form.nivelEducativo"
                  :items="nivelesEducativos"
                  label="Nivel educativo"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.required]"
                />
              </v-col>
              <v-col cols="12" md="3">
                <v-text-field
                  v-model.number="form.duracionAnios"
                  label="Duración en años"
                  type="number"
                  min="1"
                  max="20"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.requiredNumber, rules.duracion]"
                />
              </v-col>
              <v-col cols="12" md="3">
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
        </v-form>

        <div class="d-flex justify-end ga-2 mt-5">
          <v-btn variant="text" @click="cerrarDialog">Cancelar</v-btn>
          <v-btn color="primary" :loading="saving" @click="guardar">Guardar</v-btn>
        </div>
      </v-card>
    </v-dialog>

    <v-dialog v-model="dialogDetalle" max-width="760">
      <v-card class="pa-5">
        <DialogHeader
          title="Detalle del programa académico"
          subtitle="Información registrada en el catálogo académico."
          @close="dialogDetalle = false"
        />
        <v-row v-if="programaDetalle">
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Código</p>
            <v-chip size="small" color="primary" variant="tonal">
              {{ programaDetalle.codigo }}
            </v-chip>
          </v-col>
          <v-col cols="12" md="8">
            <p class="text-caption text-grey-darken-1 mb-1">Nombre</p>
            <p class="font-weight-bold">{{ programaDetalle.nombre }}</p>
          </v-col>
          <v-col cols="12">
            <p class="text-caption text-grey-darken-1 mb-1">Descripción</p>
            <p>{{ programaDetalle.descripcion || '-' }}</p>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Nivel educativo</p>
            <v-chip size="small" variant="tonal" color="secondary">
              {{ programaDetalle.nivelEducativo || 'SIN NIVEL' }}
            </v-chip>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Duración</p>
            <p class="font-weight-bold">
              {{ programaDetalle.duracionAnios ?? 0 }}
              año{{ programaDetalle.duracionAnios === 1 ? '' : 's' }}
            </p>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Estado</p>
            <v-chip size="small" :color="colorEstado(programaDetalle.estado)" variant="tonal">
              {{ textoEstado(programaDetalle.estado) }}
            </v-chip>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Usuario registro</p>
            <p>{{ programaDetalle.usuarioRegistro || '-' }}</p>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Fecha de registro</p>
            <p>{{ formatearFecha(programaDetalle.fechaRegistro) }}</p>
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
  id: string | null
  codigo: string
  nombre: string
  descripcion: string
  duracionAnios: number | null
  nivelEducativo: string
  estado: string
  usuarioRegistro?: string
  fechaRegistro?: string
  fechaActualizacion?: string
}

const { matricula } = useMatriculaHttp()

const loading = ref(false)
const saving = ref(false)
const dialog = ref(false)
const dialogDetalle = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const busqueda = ref('')
const filtroNivel = ref('TODOS')
const filtroEstado = ref('TODOS')
const formRef = ref()

const programas = ref<ProgramaAcademico[]>([])
const programaDetalle = ref<ProgramaAcademico | null>(null)

const nivelesEducativos = ['INICIAL', 'PRIMARIA', 'SECUNDARIA']
const estados = ['ACTIVO', 'INACTIVO']

const headers = [
  { title: 'Código', key: 'codigo' },
  { title: 'Programa', key: 'nombre' },
  { title: 'Nivel', key: 'nivelEducativo' },
  { title: 'Duración', key: 'duracionAnios' },
  { title: 'Estado', key: 'estado' },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'end' as const },
]

const rules = {
  required: (value: unknown) => !!String(value ?? '').trim() || 'Campo requerido',
  requiredNumber: (value: number | null) =>
    value !== null && value !== undefined || 'Campo requerido',
  codigo: (value: string) =>
    /^[A-Z0-9_-]{2,20}$/.test(value || '') ||
    'Use entre 2 y 20 caracteres: letras, números, guion o guion bajo',
  duracion: (value: number | null) =>
    (value !== null && Number(value) >= 1 && Number(value) <= 20) ||
    'Debe estar entre 1 y 20 años',
}

const form = reactive<ProgramaAcademico>(crearFormVacio())

const programasFiltrados = computed(() => {
  const texto = (busqueda.value || '').toLowerCase().trim()

  return programas.value.filter((programa) => {
    const coincideTexto =
      !texto ||
      [programa.codigo, programa.nombre, programa.descripcion].some((valor) =>
        String(valor || '').toLowerCase().includes(texto)
      )

    const coincideNivel =
      filtroNivel.value === 'TODOS' || programa.nivelEducativo === filtroNivel.value

    const coincideEstado =
      filtroEstado.value === 'TODOS' || programa.estado === filtroEstado.value

    return coincideTexto && coincideNivel && coincideEstado
  })
})

const totalActivos = computed(() =>
  programas.value.filter((programa) => programa.estado === 'ACTIVO').length
)

const totalDuracion = computed(() =>
  programas.value.reduce(
    (total, programa) => total + Number(programa.duracionAnios || 0),
    0
  )
)

function crearFormVacio(): ProgramaAcademico {
  return {
    id: null,
    codigo: '',
    nombre: '',
    descripcion: '',
    duracionAnios: 1,
    nivelEducativo: 'INICIAL',
    estado: 'ACTIVO',
  }
}

function normalizarPrograma(item: any): ProgramaAcademico {
  return {
    id: item.id ?? null,
    codigo: item.codigo ?? '',
    nombre: item.nombre ?? '',
    descripcion: item.descripcion ?? '',
    duracionAnios: item.duracionAnios ?? null,
    nivelEducativo: item.nivelEducativo ?? '',
    estado: item.estado ?? 'ACTIVO',
    usuarioRegistro: item.usuarioRegistro,
    fechaRegistro: item.fechaRegistro,
    fechaActualizacion: item.fechaActualizacion,
  }
}

function limpiarTexto(value: string | null | undefined): string | null {
  const limpio = String(value ?? '').trim()
  return limpio || null
}

function normalizarCodigo(value: string): string {
  return String(value || '')
    .toUpperCase()
    .replace(/\s+/g, '_')
    .replace(/[^A-Z0-9_-]/g, '')
    .slice(0, 20)
}

function construirPayload() {
  return {
    codigo: normalizarCodigo(form.codigo),
    nombre: limpiarTexto(form.nombre),
    descripcion: limpiarTexto(form.descripcion),
    duracionAnios: Number(form.duracionAnios ?? 0),
    nivelEducativo: limpiarTexto(form.nivelEducativo),
    estado: limpiarTexto(form.estado) || 'ACTIVO',
  }
}

function textoEstado(estado?: string): string {
  const mapa: Record<string, string> = {
    ACTIVO: 'Activo',
    INACTIVO: 'Inactivo',
  }
  return mapa[estado || ''] || estado || 'SIN ESTADO'
}

function colorEstado(estado?: string): string {
  const mapa: Record<string, string> = {
    ACTIVO: 'success',
    INACTIVO: 'default',
  }
  return mapa[estado || ''] || 'default'
}

function formatearFecha(fecha?: string): string {
  if (!fecha) return '-'

  const date = new Date(fecha)
  if (Number.isNaN(date.getTime())) return fecha

  return new Intl.DateTimeFormat('es-PE', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

async function cargar() {
  loading.value = true
  errorMsg.value = ''

  try {
    const data: any = await matricula('/api/programas-academicos')

    const lista = Array.isArray(data)
      ? data
      : Array.isArray(data?.programas)
        ? data.programas
        : []

    programas.value = lista.map(normalizarPrograma)
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al cargar los programas académicos'
    programas.value = []
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

function editar(item: ProgramaAcademico) {
  Object.assign(form, normalizarPrograma(item))
  errorMsg.value = ''
  successMsg.value = ''
  dialog.value = true
}

function verDetalle(item: ProgramaAcademico) {
  programaDetalle.value = normalizarPrograma(item)
  dialogDetalle.value = true
}

function cerrarDialog() {
  dialog.value = false
  resetForm()
}

async function guardar() {
  const validation = await formRef.value?.validate?.()
  if (validation && validation.valid === false) {
    errorMsg.value = 'Revise los campos obligatorios del formulario.'
    return
  }

  saving.value = true
  errorMsg.value = ''
  successMsg.value = ''

  try {
    const payload = construirPayload()
    const esEdicion = Boolean(form.id)

    if (esEdicion) {
      await matricula(`/api/programas-academicos/${form.id}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      })
      successMsg.value = 'Programa académico actualizado correctamente.'
    } else {
      await matricula('/api/programas-academicos', {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      successMsg.value = 'Programa académico registrado correctamente.'
    }

    cerrarDialog()
    await cargar()
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al guardar el programa académico'
  } finally {
    saving.value = false
  }
}

async function eliminar(id: string | null) {
  if (!id) return
  if (!await confirmAction('¿Eliminar este programa académico? Esta acción no se puede deshacer.')) return

  errorMsg.value = ''
  successMsg.value = ''

  try {
    const data: any = await matricula(`/api/programas-academicos/${id}`, {
      method: 'DELETE',
    })

    successMsg.value = data?.mensaje || 'Programa académico eliminado correctamente.'
    await cargar()
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al eliminar el programa académico'
  }
}

onMounted(() => {
  cargar()
})
</script>