<template>
  <div>
    <div class="d-flex justify-space-between align-center mb-4 flex-wrap ga-3">
      <div>
        <h2 class="text-h6 font-weight-bold mb-1">Consulta de pagos</h2>
        <p class="text-body-2 text-grey-darken-1 mb-0">
          Lectura de pagos y estados financieros asociados a las matrículas.
        </p>
      </div>

      <v-btn
        variant="tonal"
        color="primary"
        :loading="loading"
        @click="cargarTodo"
      >
        <v-icon start>mdi-refresh</v-icon>
        Actualizar
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

    <v-card class="main-card pa-4 mb-4">
      <v-row align="center">
        <v-col cols="12" md="4">
          <v-text-field
            v-model="busqueda"
            label="Buscar por estudiante, comprobante o concepto"
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            clearable
            hide-details
          />
        </v-col>

        <v-col cols="12" md="3">
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

        <v-col cols="12" md="3">
          <v-select
            v-model="filtroConcepto"
            :items="['TODOS', ...conceptos]"
            label="Concepto"
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
          <p class="text-h5 font-weight-bold mb-0">{{ pagosFiltrados.length }}</p>
        </v-card>
      </v-col>

      <v-col cols="12" md="3">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Pagados</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalPagados }}</p>
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
          <p class="text-caption text-grey-darken-1 mb-1">Monto pagado</p>
          <p class="text-h5 font-weight-bold mb-0">
            {{ formatearMoneda(totalMontoPagado, monedaResumen) }}
          </p>
        </v-card>
      </v-col>
    </v-row>

    <v-card class="main-card">
      <v-data-table
        :headers="headers"
        :items="pagosFiltrados"
        :loading="loading"
        item-value="id"
        no-data-text="No hay pagos registrados"
        loading-text="Cargando pagos..."
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

        <template #item.concepto="{ item }">
          <v-chip size="small" color="primary" variant="tonal">
            {{ textoConcepto(item.concepto) }}
          </v-chip>
        </template>

        <template #item.monto="{ item }">
          <span class="font-weight-bold">
            {{ formatearMoneda(item.monto, item.moneda) }}
          </span>
        </template>

        <template #item.metodoPago="{ item }">
          {{ textoMetodoPago(item.metodoPago) }}
        </template>

        <template #item.fechaPago="{ item }">
          {{ formatearFecha(item.fechaPago) }}
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
          <div class="d-flex justify-end">
            <v-btn
              icon="mdi-eye-outline"
              variant="text"
              size="small"
              @click="verDetalle(item)"
            />
          </div>
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-model="dialogDetalle" max-width="820">
      <v-card class="pa-5">
        <DialogHeader
          title="Detalle del pago"
          subtitle="Información financiera registrada en modo de solo lectura."
          @close="dialogDetalle = false"
        />

        <v-row v-if="pagoSeleccionado">
          <v-col cols="12">
            <p class="text-caption text-grey-darken-1 mb-1">Estudiante</p>
            <p class="font-weight-bold mb-0">
              {{ nombreEstudiante(pagoSeleccionado.matriculaId) }}
            </p>
            <p class="text-caption text-grey-darken-1">
              {{ resumenMatricula(pagoSeleccionado.matriculaId) }}
            </p>
          </v-col>

          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Concepto</p>
            <v-chip size="small" color="primary" variant="tonal">
              {{ textoConcepto(pagoSeleccionado.concepto) }}
            </v-chip>
          </v-col>

          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Monto</p>
            <p class="font-weight-bold">
              {{ formatearMoneda(pagoSeleccionado.monto, pagoSeleccionado.moneda) }}
            </p>
          </v-col>

          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Estado</p>
            <v-chip
              size="small"
              :color="colorEstado(pagoSeleccionado.estado)"
              variant="tonal"
            >
              {{ pagoSeleccionado.estado || 'SIN ESTADO' }}
            </v-chip>
          </v-col>

          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Comprobante</p>
            <p>{{ pagoSeleccionado.numeroComprobante || '-' }}</p>
          </v-col>

          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Método de pago</p>
            <p>{{ textoMetodoPago(pagoSeleccionado.metodoPago) }}</p>
          </v-col>

          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Fecha de vencimiento</p>
            <p>{{ formatearFecha(pagoSeleccionado.fechaVencimiento) }}</p>
          </v-col>

          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Fecha de pago</p>
            <p>{{ formatearFecha(pagoSeleccionado.fechaPago) }}</p>
          </v-col>

          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Entidad bancaria</p>
            <p>{{ pagoSeleccionado.entidadBancaria || '-' }}</p>
          </v-col>

          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Referencia bancaria</p>
            <p>{{ pagoSeleccionado.referenciaBancaria || '-' }}</p>
          </v-col>

          <v-col cols="12">
            <p class="text-caption text-grey-darken-1 mb-1">Observaciones</p>
            <p>{{ pagoSeleccionado.observaciones || '-' }}</p>
          </v-col>

          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Usuario de registro</p>
            <p>{{ pagoSeleccionado.usuarioRegistro || '-' }}</p>
          </v-col>

          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Fecha de registro</p>
            <p>{{ formatearFechaHora(pagoSeleccionado.fechaRegistro) }}</p>
          </v-col>
        </v-row>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'


interface Matricula {
  id: string
  estudianteId: string
  nombreEstudiante: string
  grado: string
  seccion: string
  periodo: string
  estado: string
}

interface PagoMatricula {
  id: string
  matriculaId: string
  numeroComprobante: string
  concepto: string
  monto: number
  moneda: string
  metodoPago: string
  fechaVencimiento: string
  fechaPago: string
  referenciaBancaria: string
  entidadBancaria: string
  estado: string
  observaciones: string
  usuarioRegistro?: string
  fechaRegistro?: string
  fechaActualizacion?: string
}

const authStore = useAuthStore()
const config = useRuntimeConfig()
const matriculaApi = config.public.matriculaApiBase
const pagosApi = config.public.pagosApiBase

const loading = ref(false)
const dialogDetalle = ref(false)
const errorMsg = ref('')
const busqueda = ref('')
const filtroMatricula = ref('TODOS')
const filtroConcepto = ref('TODOS')
const filtroEstado = ref('TODOS')

const pagos = ref<PagoMatricula[]>([])
const matriculas = ref<Matricula[]>([])
const pagoSeleccionado = ref<PagoMatricula | null>(null)

const conceptos = [
  'DERECHO_MATRICULA',
  'PENSION',
  'SEGURO',
  'MATERIALES',
  'OTRO',
]

const estados = ['PENDIENTE', 'PAGADO', 'ANULADO', 'DEVUELTO']

const headers = [
  { title: 'Matrícula', key: 'matriculaId' },
  { title: 'Concepto', key: 'concepto' },
  { title: 'Comprobante', key: 'numeroComprobante' },
  { title: 'Monto', key: 'monto' },
  { title: 'Método', key: 'metodoPago' },
  { title: 'Fecha de pago', key: 'fechaPago' },
  { title: 'Estado', key: 'estado' },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'end' as const },
]

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

const pagosFiltrados = computed(() => {
  const texto = busqueda.value?.trim().toLowerCase() || ''

  return pagos.value.filter((pago) => {
    const coincideTexto =
      !texto ||
      [
        pago.numeroComprobante,
        pago.concepto,
        pago.referenciaBancaria,
        pago.entidadBancaria,
        nombreEstudiante(pago.matriculaId),
        resumenMatricula(pago.matriculaId),
      ].some((valor) =>
        String(valor || '').toLowerCase().includes(texto)
      )

    const coincideMatricula =
      filtroMatricula.value === 'TODOS' ||
      pago.matriculaId === filtroMatricula.value

    const coincideConcepto =
      filtroConcepto.value === 'TODOS' ||
      pago.concepto === filtroConcepto.value

    const coincideEstado =
      filtroEstado.value === 'TODOS' ||
      pago.estado === filtroEstado.value

    return coincideTexto && coincideMatricula && coincideConcepto && coincideEstado
  })
})

const totalPagados = computed(() =>
  pagosFiltrados.value.filter((pago) => pago.estado === 'PAGADO').length
)

const totalPendientes = computed(() =>
  pagosFiltrados.value.filter((pago) => pago.estado === 'PENDIENTE').length
)

const totalMontoPagado = computed(() =>
  pagosFiltrados.value
    .filter((pago) => pago.estado === 'PAGADO')
    .reduce((total, pago) => total + Number(pago.monto || 0), 0)
)

const monedaResumen = computed(() => {
  const monedas = new Set(
    pagosFiltrados.value
      .filter((pago) => pago.estado === 'PAGADO')
      .map((pago) => pago.moneda)
      .filter(Boolean)
  )

  return monedas.size === 1 ? [...monedas][0] : 'PEN'
})

function authHeaders() {
  return {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${authStore.token}`,
  }
}

async function leerRespuesta(response: Response) {
  const contentType = response.headers.get('content-type') || ''
  const text = await response.text()

  if (!text) return null

  if (contentType.includes('application/json')) {
    try {
      return JSON.parse(text)
    } catch {
      return { mensaje: text }
    }
  }

  try {
    return JSON.parse(text)
  } catch {
    return { mensaje: text }
  }
}

function validarSesionReal() {
  authStore.restoreSession()

  if (!authStore.token) {
    throw new Error('No hay token de sesión. Inicia sesión nuevamente.')
  }

  if (authStore.token.startsWith('demo-token-')) {
    throw new Error(
      'Estás usando modo demo. Para consultar pagos, inicia sesión con el backend real.'
    )
  }
}

function obtenerMensajeError(data: any, mensajeDefecto: string) {
  return data?.mensaje || data?.message || data?.error || mensajeDefecto
}

async function cargarMatriculas() {
  const response = await fetch(`${matriculaApi}/api/matriculas`, {
    headers: authHeaders(),
  })

  const data = await leerRespuesta(response)

  if (!response.ok) {
    throw new Error(
      obtenerMensajeError(data, 'No se pudo cargar las matrículas')
    )
  }

  matriculas.value = Array.isArray(data?.matriculas)
    ? data.matriculas
    : Array.isArray(data)
      ? data
      : []
}

async function cargarPagos() {
  const response = await fetch(`${pagosApi}/api/pagos-matricula`, {
    headers: authHeaders(),
  })

  const data = await leerRespuesta(response)

  if (!response.ok) {
    throw new Error(
      obtenerMensajeError(data, 'No se pudo listar los pagos')
    )
  }

  pagos.value = Array.isArray(data) ? data : []
}

async function cargarTodo() {
  loading.value = true
  errorMsg.value = ''

  try {
    validarSesionReal()
    await Promise.all([cargarMatriculas(), cargarPagos()])
  } catch (error: any) {
    errorMsg.value = error.message || 'Error al cargar los pagos'
    pagos.value = []
  } finally {
    loading.value = false
  }
}

function verDetalle(item: PagoMatricula) {
  pagoSeleccionado.value = { ...item }
  dialogDetalle.value = true
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

function textoConcepto(concepto?: string) {
  const textos: Record<string, string> = {
    DERECHO_MATRICULA: 'Derecho de matrícula',
    PENSION: 'Pensión',
    SEGURO: 'Seguro',
    MATERIALES: 'Materiales',
    OTRO: 'Otro',
  }

  return textos[concepto || ''] || concepto || 'SIN CONCEPTO'
}

function textoMetodoPago(metodo?: string) {
  const textos: Record<string, string> = {
    EFECTIVO: 'Efectivo',
    TRANSFERENCIA: 'Transferencia',
    TARJETA: 'Tarjeta',
    DEPOSITO: 'Depósito',
    YAPE: 'Yape',
    PLIN: 'Plin',
  }

  return textos[metodo || ''] || metodo || '-'
}

function colorEstado(estado?: string) {
  if (estado === 'PAGADO') return 'success'
  if (estado === 'PENDIENTE') return 'warning'
  if (estado === 'DEVUELTO') return 'info'
  if (estado === 'ANULADO') return 'error'
  return 'default'
}

function formatearMoneda(monto?: number, moneda?: string) {
  const codigo = moneda || 'PEN'

  return new Intl.NumberFormat('es-PE', {
    style: 'currency',
    currency: codigo,
    minimumFractionDigits: 2,
  }).format(Number(monto || 0))
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

onMounted(() => {
  cargarTodo()
})
</script>
