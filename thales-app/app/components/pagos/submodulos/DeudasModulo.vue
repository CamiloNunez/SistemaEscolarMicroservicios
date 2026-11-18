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

        <v-btn color="primary" @click="abrirGenerar">
          <v-icon start>mdi-cash-plus</v-icon>
          Generar deuda
        </v-btn>
      </div>
    </div>

    <v-alert
      v-if="errorMsg"
      type="error"
      variant="tonal"
      closable
      class="mb-4"
      @click:close="errorMsg = ''"
    >
      {{ errorMsg }}
    </v-alert>

    <v-alert
      v-if="successMsg"
      type="success"
      variant="tonal"
      closable
      class="mb-4"
      @click:close="successMsg = ''"
    >
      {{ successMsg }}
    </v-alert>

    <v-card class="main-card pa-4 mb-4">
      <v-row>
        <v-col cols="12" md="6">
          <v-text-field
            v-model="buscar"
            label="Buscar estudiante, matrícula o concepto"
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            clearable
            hide-details
          />
        </v-col>

        <v-col cols="12" md="3">
          <v-select
            v-model="filtroConcepto"
            :items="['TODOS', 'MATRICULA', 'PENSION']"
            label="Concepto"
            variant="outlined"
            hide-details
          />
        </v-col>

        <v-col cols="12" md="3">
          <v-select
            v-model="filtroEstado"
            :items="['TODOS', 'PENDIENTE', 'PARCIAL', 'PAGADA', 'VENCIDA', 'ANULADA']"
            label="Estado"
            variant="outlined"
            hide-details
          />
        </v-col>
      </v-row>
    </v-card>

    <v-row class="mb-4">
      <v-col cols="12" sm="6" md="3">
        <v-card class="main-card pa-4">
          <p class="text-caption mb-1">Total</p>
          <p class="text-h5 font-weight-bold mb-0">{{ filtradas.length }}</p>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="3">
        <v-card class="main-card pa-4">
          <p class="text-caption mb-1">Pendientes</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalPendientes }}</p>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="3">
        <v-card class="main-card pa-4">
          <p class="text-caption mb-1">Pagadas</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalPagadas }}</p>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="3">
        <v-card class="main-card pa-4">
          <p class="text-caption mb-1">Saldo pendiente</p>
          <p class="text-h5 font-weight-bold mb-0">{{ moneda(totalSaldo) }}</p>
        </v-card>
      </v-col>
    </v-row>

    <v-card class="main-card">
      <v-data-table
        :headers="headers"
        :items="filtradas"
        :loading="loading"
        item-value="id"
        no-data-text="No existen deudas registradas"
      >
        <template #item.estudianteId="{ item }">
          <div>
            <strong>{{ nombreEstudiante(item) }}</strong>
            <p class="text-caption mb-0">{{ item.estudianteId }}</p>
          </div>
        </template>

        <template #item.concepto="{ item }">
          <v-chip size="small" variant="tonal" color="primary">
            {{ item.concepto }}
          </v-chip>
        </template>

        <template #item.monto="{ item }">
          {{ moneda(item.monto, item.moneda) }}
        </template>

        <template #item.saldoPendiente="{ item }">
          <strong>{{ moneda(item.saldoPendiente, item.moneda) }}</strong>
        </template>

        <template #item.fechaVencimiento="{ item }">
          {{ fecha(item.fechaVencimiento) }}
        </template>

        <template #item.estado="{ item }">
          <v-chip
            size="small"
            variant="tonal"
            :color="colorEstado(item.estado)"
          >
            {{ item.estado }}
          </v-chip>
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-model="dialog" max-width="720">
      <v-card class="pa-5">
        <v-card-title>Generar deuda</v-card-title>

        <v-card-subtitle class="mb-4">
          Genera una obligación de matrícula o pensión.
        </v-card-subtitle>

        <v-card-text>
          <v-row>
            <v-col cols="12">
              <v-select
                v-model="form.matriculaId"
                :items="matriculaItems"
                item-title="title"
                item-value="value"
                label="Matrícula *"
                variant="outlined"
                @update:model-value="seleccionarMatricula"
              />
            </v-col>

            <v-col cols="12" md="6">
              <v-select
                v-model="form.concepto"
                :items="conceptosGenerables"
                item-title="title"
                item-value="value"
                label="Concepto *"
                variant="outlined"
              />
            </v-col>

            <v-col cols="12" md="6">
              <v-text-field
                v-model.number="form.monto"
                type="number"
                min="0"
                step="0.01"
                label="Monto *"
                prefix="S/"
                variant="outlined"
              />
            </v-col>

            <v-col cols="12" md="6">
              <v-text-field
                v-model="form.fechaVencimiento"
                type="datetime-local"
                label="Fecha de vencimiento *"
                variant="outlined"
              />
            </v-col>

            <v-col cols="12" md="6">
              <v-text-field
                model-value="PEN"
                label="Moneda"
                variant="outlined"
                readonly
              />
            </v-col>

            <v-col cols="12">
              <v-alert type="info" variant="tonal">
                Esta operación solamente genera la deuda. No registra ningún pago.
              </v-alert>
            </v-col>
          </v-row>
        </v-card-text>

        <v-card-actions class="justify-end">
          <v-btn variant="text" @click="dialog = false">
            Cancelar
          </v-btn>

          <v-btn color="primary" :loading="saving" @click="generarDeuda">
            Generar deuda
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
const { pagos, matricula } = usePagosHttp()

const loading = ref(false)
const saving = ref(false)
const dialog = ref(false)

const errorMsg = ref('')
const successMsg = ref('')

const buscar = ref('')
const filtroConcepto = ref('TODOS')
const filtroEstado = ref('TODOS')

const deudas = ref<any[]>([])
const matriculas = ref<any[]>([])

const conceptosGenerables = [
  { title: 'Matrícula', value: 'MATRICULA' },
  { title: 'Pensión', value: 'PENSION' },
]

const form = reactive({
  estudianteId: '',
  matriculaId: '',
  concepto: 'MATRICULA',
  monto: null as number | null,
  moneda: 'PEN',
  fechaVencimiento: '',
})

const headers = [
  { title: 'Estudiante', key: 'estudianteId' },
  { title: 'Matrícula', key: 'matriculaId' },
  { title: 'Concepto', key: 'concepto' },
  { title: 'Monto', key: 'monto' },
  { title: 'Saldo', key: 'saldoPendiente' },
  { title: 'Vencimiento', key: 'fechaVencimiento' },
  { title: 'Estado', key: 'estado' },
]

const matriculaItems = computed(() =>
  matriculas.value.map((m: any) => ({
    title:
      `${m.nombreEstudiante || m.estudianteId || 'Estudiante'} - ` +
      `${m.grado || ''} ${m.seccion || ''} ` +
      `${m.periodo ? '(' + m.periodo + ')' : ''}`,
    value: m.id,
  }))
)

const filtradas = computed(() => {
  const q = buscar.value.trim().toLowerCase()

  return deudas.value.filter((d: any) => {
    const coincideTexto =
      !q ||
      [
        d.estudianteId,
        d.matriculaId,
        d.concepto,
        nombreEstudiante(d),
      ].some(value =>
        String(value || '').toLowerCase().includes(q)
      )

    const coincideConcepto =
      filtroConcepto.value === 'TODOS' ||
      d.concepto === filtroConcepto.value

    const coincideEstado =
      filtroEstado.value === 'TODOS' ||
      d.estado === filtroEstado.value

    return coincideTexto && coincideConcepto && coincideEstado
  })
})

const totalPendientes = computed(() =>
  filtradas.value.filter(
    d => d.estado === 'PENDIENTE' || d.estado === 'PARCIAL'
  ).length
)

const totalPagadas = computed(() =>
  filtradas.value.filter(d => d.estado === 'PAGADA').length
)

const totalSaldo = computed(() =>
  filtradas.value.reduce(
    (total, d) => total + Number(d.saldoPendiente || 0),
    0
  )
)

async function cargarDeudas() {
  const data = await pagos('/api/deudas')
  deudas.value = Array.isArray(data) ? data : []
}

async function cargarMatriculas() {
  const data = await matricula('/api/matriculas')

  matriculas.value =
    Array.isArray(data?.matriculas)
      ? data.matriculas
      : Array.isArray(data)
        ? data
        : []
}

async function cargarTodo() {
  loading.value = true
  errorMsg.value = ''

  try {
    await Promise.all([
      cargarDeudas(),
      cargarMatriculas(),
    ])
  } catch (e: any) {
    errorMsg.value =
      e.message || 'No se pudo cargar la información.'
  } finally {
    loading.value = false
  }
}

function seleccionarMatricula(id: string) {
  const seleccionada = matriculas.value.find(m => m.id === id)
  form.estudianteId = seleccionada?.estudianteId || ''
}

function abrirGenerar() {
  Object.assign(form, {
    estudianteId: '',
    matriculaId: '',
    concepto: 'MATRICULA',
    monto: null,
    moneda: 'PEN',
    fechaVencimiento: '',
  })

  dialog.value = true
}

async function generarDeuda() {
  errorMsg.value = ''
  successMsg.value = ''

  if (!form.matriculaId || !form.estudianteId) {
    errorMsg.value = 'Debe seleccionar una matrícula.'
    return
  }

  if (form.monto == null || Number(form.monto) <= 0) {
    errorMsg.value = 'Ingrese un monto válido.'
    return
  }

  if (!form.fechaVencimiento) {
    errorMsg.value = 'Ingrese la fecha de vencimiento.'
    return
  }

  saving.value = true

  try {
    await pagos('/api/deudas', {
      method: 'POST',
      body: JSON.stringify({
        estudianteId: form.estudianteId,
        matriculaId: form.matriculaId,
        concepto: form.concepto,
        monto: Number(form.monto),
        saldoPendiente: Number(form.monto),
        moneda: 'PEN',
        fechaVencimiento: form.fechaVencimiento,
        estado: 'PENDIENTE',
      }),
    })

    dialog.value = false

    successMsg.value =
      form.concepto === 'MATRICULA'
        ? 'Deuda de matrícula generada correctamente.'
        : 'Deuda de pensión generada correctamente.'

    await cargarDeudas()
  } catch (e: any) {
    errorMsg.value =
      e.message || 'No se pudo generar la deuda.'
  } finally {
    saving.value = false
  }
}

function nombreEstudiante(deuda: any) {
  const m = matriculas.value.find(x => x.id === deuda.matriculaId)

  return (
    m?.nombreEstudiante ||
    deuda.estudianteId ||
    '-'
  )
}

function moneda(monto: number, codigo = 'PEN') {
  return new Intl.NumberFormat('es-PE', {
    style: 'currency',
    currency: codigo || 'PEN',
  }).format(Number(monto || 0))
}

function fecha(value?: string) {
  if (!value) return '-'

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('es-PE', {
    dateStyle: 'medium',
  }).format(date)
}

function colorEstado(estado?: string) {
  if (estado === 'PAGADA') return 'success'
  if (estado === 'PENDIENTE') return 'warning'
  if (estado === 'PARCIAL') return 'info'
  if (estado === 'VENCIDA' || estado === 'ANULADA') return 'error'

  return 'default'
}

onMounted(cargarTodo)
</script>
