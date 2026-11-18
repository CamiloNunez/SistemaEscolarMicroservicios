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
          Nuevo periodo
        </v-btn>
      </div>
    </div>

    <v-alert v-if="errorMsg" type="error" variant="tonal" class="mb-4" closable @click:close="errorMsg = ''">
      {{ errorMsg }}
    </v-alert>
    <v-alert v-if="successMsg" type="success" variant="tonal" class="mb-4" closable @click:close="successMsg = ''">
      {{ successMsg }}
    </v-alert>

    <v-card class="main-card pa-4 mb-4">
      <v-row align="center">
        <v-col cols="12" md="6">
          <v-text-field
            v-model="busqueda"
            label="Buscar por código o nombre"
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
          <p class="text-caption text-grey-darken-1 mb-1">Total de periodos</p>
          <p class="text-h5 font-weight-bold mb-0">{{ periodosFiltrados.length }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">En matrícula</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalEnMatricula }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">En curso</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalEnCurso }}</p>
        </v-card>
      </v-col>
    </v-row>

    <v-card class="main-card">
      <v-data-table
        :headers="headers"
        :items="periodosFiltrados"
        :loading="loading"
        item-value="id"
        no-data-text="No hay periodos académicos registrados"
        loading-text="Cargando periodos académicos..."
      >
        <template #item.codigo="{ item }">
          <v-chip size="small" color="primary" variant="tonal">{{ item.codigo }}</v-chip>
        </template>
        <template #item.tipo="{ item }">
          <v-chip size="small" color="secondary" variant="tonal">{{ item.tipo || 'SIN TIPO' }}</v-chip>
        </template>
        <template #item.rangoAcademico="{ item }">
          {{ formatearRango(item.fechaInicio, item.fechaFin) }}
        </template>
        <template #item.rangoMatricula="{ item }">
          {{ formatearRango(item.inicioMatricula, item.finMatricula) }}
        </template>
        <template #item.estado="{ item }">
          <v-chip size="small" :color="colorEstado(item.estado)" variant="tonal">
            {{ item.estado || 'SIN ESTADO' }}
          </v-chip>
        </template>
        <template #item.actions="{ item }">
          <div class="d-flex ga-1 justify-end">
            <v-btn icon="mdi-eye-outline" variant="text" size="small" @click="verDetalle(item)" />
            <v-btn icon="mdi-pencil" variant="text" size="small" @click="editar(item)" />
            <v-btn icon="mdi-delete" variant="text" color="error" size="small" @click="eliminar(item)" />
          </div>
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-model="dialog" max-width="980" persistent>
      <v-card class="pa-5">
        <DialogHeader
          :title="form.id ? 'Editar periodo académico' : 'Nuevo periodo académico'"
          subtitle="Configure el periodo académico y sus ventanas de matrícula."
          @close="cerrarDialog"
        />
        <v-form ref="formRef">
          <v-card variant="outlined" class="pa-4 mb-4">
            <h3 class="text-subtitle-1 font-weight-bold mb-4">Datos generales</h3>
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
            <h3 class="text-subtitle-1 font-weight-bold mb-4">Periodo académico</h3>
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="form.fechaInicio"
                  label="Fecha de inicio"
                  type="date"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.required]"
                />
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="form.fechaFin"
                  label="Fecha de fin"
                  type="date"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.required, rules.finAcademico]"
                />
              </v-col>
            </v-row>
          </v-card>
          <v-card variant="outlined" class="pa-4 mb-4">
            <h3 class="text-subtitle-1 font-weight-bold mb-4">Matrícula regular</h3>
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="form.inicioMatricula"
                  label="Inicio de matrícula"
                  type="date"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.required]"
                />
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="form.finMatricula"
                  label="Fin de matrícula"
                  type="date"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.required, rules.finMatricula]"
                />
              </v-col>
            </v-row>
          </v-card>
          <v-card variant="outlined" class="pa-4 mb-4">
            <h3 class="text-subtitle-1 font-weight-bold mb-4">Matrícula extemporánea</h3>
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="form.inicioMatriculaExtemporanea"
                  label="Inicio de matrícula extemporánea"
                  type="date"
                  variant="outlined"
                  density="compact"
                  clearable
                />
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="form.finMatriculaExtemporanea"
                  label="Fin de matrícula extemporánea"
                  type="date"
                  variant="outlined"
                  density="compact"
                  clearable
                  :rules="[rules.finExtemporanea]"
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

    <v-dialog v-model="dialogDetalle" max-width="760">
      <v-card class="pa-5">
        <DialogHeader
          title="Detalle del periodo académico"
          subtitle="Fechas académicas y ventanas de matrícula."
          @close="dialogDetalle = false"
        />
        <v-row v-if="periodoDetalle">
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Código</p>
            <v-chip size="small" color="primary" variant="tonal">{{ periodoDetalle.codigo }}</v-chip>
          </v-col>
          <v-col cols="12" md="8">
            <p class="text-caption text-grey-darken-1 mb-1">Nombre</p>
            <p class="font-weight-bold">{{ periodoDetalle.nombre }}</p>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Tipo</p>
            <v-chip size="small" color="secondary" variant="tonal">{{ periodoDetalle.tipo || 'SIN TIPO' }}</v-chip>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Estado</p>
            <v-chip size="small" :color="colorEstado(periodoDetalle.estado)" variant="tonal">
              {{ periodoDetalle.estado || 'SIN ESTADO' }}
            </v-chip>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Duración</p>
            <p class="font-weight-bold">{{ diasEntre(periodoDetalle.fechaInicio, periodoDetalle.fechaFin) }} días</p>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Periodo académico</p>
            <p>{{ formatearRango(periodoDetalle.fechaInicio, periodoDetalle.fechaFin) }}</p>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Matrícula regular</p>
            <p>{{ formatearRango(periodoDetalle.inicioMatricula, periodoDetalle.finMatricula) }}</p>
          </v-col>
          <v-col cols="12">
            <p class="text-caption text-grey-darken-1 mb-1">Matrícula extemporánea</p>
            <p>{{ formatearRango(periodoDetalle.inicioMatriculaExtemporanea, periodoDetalle.finMatriculaExtemporanea) }}</p>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Usuario registro</p>
            <p>{{ periodoDetalle.usuarioRegistro || '-' }}</p>
          </v-col>
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Fecha de registro</p>
            <p>{{ formatearFechaHora(periodoDetalle.fechaRegistro) }}</p>
          </v-col>
        </v-row>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { useMatriculaHttp } from '~/composables/useMatriculaHttp'
const { confirm: confirmAction } = useUiFeedback()

interface PeriodoAcademico {
  id: string | null
  codigo: string
  nombre: string
  tipo: string
  fechaInicio: string
  fechaFin: string
  inicioMatricula: string
  finMatricula: string
  inicioMatriculaExtemporanea: string
  finMatriculaExtemporanea: string
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
const filtroTipo = ref('TODOS')
const filtroEstado = ref('TODOS')
const formRef = ref()

const periodos = ref<PeriodoAcademico[]>([])
const periodoDetalle = ref<PeriodoAcademico | null>(null)

const tipos = ['ANUAL', 'SEMESTRAL', 'TRIMESTRAL']
const estados = ['PLANIFICADO', 'MATRICULA', 'EN_CURSO', 'CERRADO']

const headers = [
  { title: 'Código', key: 'codigo' },
  { title: 'Nombre', key: 'nombre' },
  { title: 'Tipo', key: 'tipo' },
  { title: 'Periodo académico', key: 'rangoAcademico', sortable: false },
  { title: 'Matrícula', key: 'rangoMatricula', sortable: false },
  { title: 'Estado', key: 'estado' },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'end' as const },
]

const form = reactive<PeriodoAcademico>(crearFormVacio())

const rules = {
  required: (value: unknown) => !!String(value || '').trim() || 'Campo requerido',
  codigo: (value: string) =>
    /^[A-Z0-9_-]{2,20}$/.test(value || '') ||
    'Use entre 2 y 20 caracteres: letras, números, guion o guion bajo',
  finAcademico: (value: string) =>
    !value || !form.fechaInicio || value >= form.fechaInicio || 'La fecha de fin no puede ser anterior al inicio',
  finMatricula: (value: string) =>
    !value || !form.inicioMatricula || value >= form.inicioMatricula || 'El fin de matrícula no puede ser anterior al inicio',
  finExtemporanea: (value: string) =>
    !value ||
    !form.inicioMatriculaExtemporanea ||
    value >= form.inicioMatriculaExtemporanea ||
    'El fin extemporáneo no puede ser anterior al inicio',
}

const periodosFiltrados = computed(() => {
  const texto = (busqueda.value || '').toLowerCase().trim()
  return periodos.value.filter((periodo) => {
    const coincideTexto =
      !texto ||
      [periodo.codigo, periodo.nombre].some((valor) =>
        String(valor || '').toLowerCase().includes(texto)
      )
    const coincideTipo = filtroTipo.value === 'TODOS' || periodo.tipo === filtroTipo.value
    const coincideEstado = filtroEstado.value === 'TODOS' || periodo.estado === filtroEstado.value
    return coincideTexto && coincideTipo && coincideEstado
  })
})

const totalEnMatricula = computed(() =>
  periodosFiltrados.value.filter((periodo) => periodo.estado === 'MATRICULA').length
)
const totalEnCurso = computed(() =>
  periodosFiltrados.value.filter((periodo) => periodo.estado === 'EN_CURSO').length
)

function crearFormVacio(): PeriodoAcademico {
  return {
    id: null,
    codigo: '',
    nombre: '',
    tipo: 'ANUAL',
    fechaInicio: '',
    fechaFin: '',
    inicioMatricula: '',
    finMatricula: '',
    inicioMatriculaExtemporanea: '',
    finMatriculaExtemporanea: '',
    estado: 'PLANIFICADO',
  }
}

function normalizarPeriodo(item: any): PeriodoAcademico {
  return {
    id: item.id ?? null,
    codigo: item.codigo ?? '',
    nombre: item.nombre ?? '',
    tipo: item.tipo ?? 'ANUAL',
    fechaInicio: item.fechaInicio ?? '',
    fechaFin: item.fechaFin ?? '',
    inicioMatricula: item.inicioMatricula ?? '',
    finMatricula: item.finMatricula ?? '',
    inicioMatriculaExtemporanea: item.inicioMatriculaExtemporanea ?? '',
    finMatriculaExtemporanea: item.finMatriculaExtemporanea ?? '',
    estado: item.estado ?? 'PLANIFICADO',
    usuarioRegistro: item.usuarioRegistro,
    fechaRegistro: item.fechaRegistro,
    fechaActualizacion: item.fechaActualizacion,
  }
}

function construirPayload() {
  return {
    codigo: normalizarCodigo(form.codigo),
    nombre: form.nombre.trim(),
    tipo: form.tipo,
    fechaInicio: form.fechaInicio || null,
    fechaFin: form.fechaFin || null,
    inicioMatricula: form.inicioMatricula || null,
    finMatricula: form.finMatricula || null,
    inicioMatriculaExtemporanea: form.inicioMatriculaExtemporanea || null,
    finMatriculaExtemporanea: form.finMatriculaExtemporanea || null,
    estado: form.estado,
  }
}

function normalizarCodigo(value: string) {
  return String(value || '')
    .toUpperCase()
    .replace(/\s+/g, '_')
    .replace(/[^A-Z0-9_-]/g, '')
    .slice(0, 20)
}

async function cargar() {
  loading.value = true
  errorMsg.value = ''

  try {
    const data: any = await matricula('/api/periodos-academicos')
    const lista = Array.isArray(data)
      ? data
      : Array.isArray(data?.periodos)
        ? data.periodos
        : []
    periodos.value = lista.map(normalizarPeriodo)
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al cargar los periodos académicos'
    periodos.value = []
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

function editar(item: PeriodoAcademico) {
  errorMsg.value = ''
  successMsg.value = ''
  Object.assign(form, normalizarPeriodo(item))
  dialog.value = true
}

function verDetalle(item: PeriodoAcademico) {
  periodoDetalle.value = normalizarPeriodo(item)
  dialogDetalle.value = true
}

function cerrarDialog() {
  dialog.value = false
  resetForm()
}

function validarFechas() {
  if (form.fechaFin && form.fechaInicio && form.fechaFin < form.fechaInicio) {
    throw new Error('La fecha de fin no puede ser anterior a la fecha de inicio.')
  }
  if (form.finMatricula && form.inicioMatricula && form.finMatricula < form.inicioMatricula) {
    throw new Error('El fin de matrícula no puede ser anterior al inicio de matrícula.')
  }
  if (
    Boolean(form.inicioMatriculaExtemporanea) !== Boolean(form.finMatriculaExtemporanea)
  ) {
    throw new Error('Debe completar ambas fechas de matrícula extemporánea o dejar ambas vacías.')
  }
  if (
    form.inicioMatriculaExtemporanea &&
    form.finMatriculaExtemporanea &&
    form.finMatriculaExtemporanea < form.inicioMatriculaExtemporanea
  ) {
    throw new Error('El fin de matrícula extemporánea no puede ser anterior al inicio.')
  }
  if (
    form.inicioMatriculaExtemporanea &&
    form.finMatricula &&
    form.inicioMatriculaExtemporanea < form.finMatricula
  ) {
    throw new Error('La matrícula extemporánea debe iniciar después de finalizar la matrícula regular.')
  }
}

async function guardar() {
  const validation = await formRef.value?.validate?.()
  if (validation && validation.valid === false) return

  saving.value = true
  errorMsg.value = ''
  successMsg.value = ''

  try {
    validarFechas()
    const payload = construirPayload()
    const esEdicion = Boolean(form.id)

    if (esEdicion) {
      await matricula(`/api/periodos-academicos/${form.id}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      })
      successMsg.value = 'Periodo académico actualizado correctamente.'
    } else {
      await matricula('/api/periodos-academicos', {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      successMsg.value = 'Periodo académico registrado correctamente.'
    }

    cerrarDialog()
    await cargar()
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al guardar el periodo académico'
  } finally {
    saving.value = false
  }
}

async function eliminar(item: PeriodoAcademico) {
  if (!item.id) return
  if (!await confirmAction(`¿Está seguro de eliminar el periodo "${item.nombre}"?`)) return

  errorMsg.value = ''
  successMsg.value = ''

  try {
    const data: any = await matricula(`/api/periodos-academicos/${item.id}`, {
      method: 'DELETE',
    })
    successMsg.value = data?.mensaje || 'Periodo académico eliminado correctamente.'
    await cargar()
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al eliminar el periodo académico'
  }
}

function colorEstado(estado?: string) {
  const mapa: Record<string, string> = {
    MATRICULA: 'warning',
    EN_CURSO: 'success',
    CERRADO: 'default',
    PLANIFICADO: 'info',
  }
  return mapa[estado || ''] || 'default'
}

function formatearFecha(fecha?: string) {
  if (!fecha) return '-'
  const date = new Date(`${fecha}T00:00:00`)
  if (Number.isNaN(date.getTime())) return fecha
  return new Intl.DateTimeFormat('es-PE', { dateStyle: 'medium' }).format(date)
}

function formatearFechaHora(fecha?: string) {
  if (!fecha) return '-'
  const date = new Date(fecha)
  if (Number.isNaN(date.getTime())) return fecha
  return new Intl.DateTimeFormat('es-PE', { dateStyle: 'medium', timeStyle: 'short' }).format(date)
}

function formatearRango(inicio?: string, fin?: string) {
  if (!inicio && !fin) return 'Sin definir'
  if (inicio && !fin) return `Desde ${formatearFecha(inicio)}`
  if (!inicio && fin) return `Hasta ${formatearFecha(fin)}`
  return `${formatearFecha(inicio)} - ${formatearFecha(fin)}`
}

function diasEntre(inicio?: string, fin?: string) {
  if (!inicio || !fin) return 0
  const fechaInicio = new Date(`${inicio}T00:00:00`)
  const fechaFin = new Date(`${fin}T00:00:00`)
  if (Number.isNaN(fechaInicio.getTime()) || Number.isNaN(fechaFin.getTime())) return 0
  return Math.floor((fechaFin.getTime() - fechaInicio.getTime()) / 86400000) + 1
}

onMounted(() => {
  cargar()
})
</script>