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
          Nuevo apoderado
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
            label="Buscar por nombres, DNI, relación o teléfono"
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            clearable
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
        <v-col cols="12" md="3">
          <v-select
            v-model="filtroRelacion"
            :items="['TODOS', ...tiposRelacion]"
            label="Relación"
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
          <p class="text-caption text-grey-darken-1 mb-1">Total registrados</p>
          <p class="text-h5 font-weight-bold mb-0">{{ apoderados.length }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Activos</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalActivos }}</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card class="main-card pa-4">
          <p class="text-caption text-grey-darken-1 mb-1">Principales</p>
          <p class="text-h5 font-weight-bold mb-0">{{ totalPrincipales }}</p>
        </v-card>
      </v-col>
    </v-row>

    <v-card class="main-card">
      <v-data-table
        :headers="headers"
        :items="apoderadosFiltrados"
        :loading="loading"
        item-value="id"
      >
        <template #item.nombreCompleto="{ item }">
          <div>
            <p class="font-weight-bold mb-0">{{ nombreCompleto(item) }}</p>
            <p class="text-caption text-grey-darken-1 mb-0">
              DNI: {{ item.dni || 'Sin DNI' }}
            </p>
          </div>
        </template>
        <template #item.tipoRelacion="{ item }">
          <v-chip size="small" variant="tonal" color="primary">
            {{ textoRelacion(item.tipoRelacion) }}
          </v-chip>
        </template>
        <template #item.esApoderadoPrincipal="{ item }">
          <v-chip
            size="small"
            :color="item.esApoderadoPrincipal ? 'success' : 'default'"
            variant="tonal"
          >
            {{ item.esApoderadoPrincipal ? 'Sí' : 'No' }}
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

    <v-dialog v-model="dialog" max-width="1050" persistent>
      <v-card class="pa-5">
        <DialogHeader
          :title="form.id ? 'Editar apoderado' : 'Nuevo apoderado'"
          subtitle="Complete solo los campos que existen en el backend de apoderados."
          @close="cerrarDialog"
        />

        <v-form ref="formRef">
          <v-card variant="outlined" class="pa-4 mb-4">
            <h3 class="text-subtitle-1 font-weight-bold mb-4">Datos personales</h3>
            <v-row>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model.trim="form.nombres"
                  label="Nombres"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.required]"
                />
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model.trim="form.apellidoPaterno"
                  label="Apellido paterno"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.required]"
                />
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model.trim="form.apellidoMaterno"
                  label="Apellido materno"
                  variant="outlined"
                  density="compact"
                />
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model="form.dni"
                  label="DNI"
                  variant="outlined"
                  density="compact"
                  maxlength="8"
                  counter="8"
                  :rules="[rules.required, rules.dni]"
                  @update:model-value="form.dni = normalizarDni(form.dni)"
                />
              </v-col>
              <v-col cols="12" md="4">
                <v-select
                  v-model="form.tipoRelacion"
                  :items="tiposRelacion"
                  label="Tipo de relación"
                  variant="outlined"
                  density="compact"
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
              <v-col cols="12">
                <v-switch
                  v-model="form.esApoderadoPrincipal"
                  label="Es apoderado principal"
                  color="primary"
                  hide-details
                />
              </v-col>
            </v-row>
          </v-card>

          <v-card variant="outlined" class="pa-4 mb-4">
            <h3 class="text-subtitle-1 font-weight-bold mb-4">Contacto</h3>
            <v-row>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model.trim="form.telefonoCelular"
                  label="Celular"
                  variant="outlined"
                  density="compact"
                />
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model.trim="form.telefonoFijo"
                  label="Teléfono fijo"
                  variant="outlined"
                  density="compact"
                />
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model.trim="form.correo"
                  label="Correo"
                  type="email"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.email]"
                />
              </v-col>
            </v-row>
          </v-card>

          <v-card variant="outlined" class="pa-4 mb-4">
            <h3 class="text-subtitle-1 font-weight-bold mb-4">Dirección</h3>
            <v-row>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model.trim="form.direccion.calle"
                  label="Calle / avenida"
                  variant="outlined"
                  density="compact"
                />
              </v-col>
              <v-col cols="12" md="2">
                <v-text-field
                  v-model.trim="form.direccion.numero"
                  label="Número"
                  variant="outlined"
                  density="compact"
                />
              </v-col>
              <v-col cols="12" md="3">
                <v-text-field
                  v-model.trim="form.direccion.distrito"
                  label="Distrito"
                  variant="outlined"
                  density="compact"
                />
              </v-col>
              <v-col cols="12" md="3">
                <v-text-field
                  v-model.trim="form.direccion.provincia"
                  label="Provincia"
                  variant="outlined"
                  density="compact"
                />
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model.trim="form.direccion.departamento"
                  label="Departamento"
                  variant="outlined"
                  density="compact"
                />
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model.trim="form.direccion.codigoPostal"
                  label="Código postal"
                  variant="outlined"
                  density="compact"
                />
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model.trim="form.direccion.referencia"
                  label="Referencia"
                  variant="outlined"
                  density="compact"
                />
              </v-col>
            </v-row>
          </v-card>

          <v-card variant="outlined" class="pa-4 mb-4">
            <h3 class="text-subtitle-1 font-weight-bold mb-4">Datos laborales</h3>
            <v-row>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model.trim="form.ocupacion"
                  label="Ocupación"
                  variant="outlined"
                  density="compact"
                />
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model.trim="form.centroTrabajo"
                  label="Centro de trabajo"
                  variant="outlined"
                  density="compact"
                />
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model.trim="form.telefonoTrabajo"
                  label="Teléfono de trabajo"
                  variant="outlined"
                  density="compact"
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
          title="Detalle del apoderado"
          subtitle="Información registrada para consulta rápida."
          @close="dialogDetalle = false"
        />
        <v-row v-if="apoderadoDetalle">
          <v-col cols="12" md="6">
            <p class="text-caption text-grey-darken-1 mb-1">Nombre completo</p>
            <p class="font-weight-bold">{{ nombreCompleto(apoderadoDetalle) }}</p>
          </v-col>
          <v-col cols="12" md="3">
            <p class="text-caption text-grey-darken-1 mb-1">DNI</p>
            <p class="font-weight-bold">{{ apoderadoDetalle.dni || '-' }}</p>
          </v-col>
          <v-col cols="12" md="3">
            <p class="text-caption text-grey-darken-1 mb-1">Relación</p>
            <p class="font-weight-bold">
              {{ textoRelacion(apoderadoDetalle.tipoRelacion) }}
            </p>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Celular</p>
            <p>{{ apoderadoDetalle.telefonoCelular || '-' }}</p>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Correo</p>
            <p>{{ apoderadoDetalle.correo || '-' }}</p>
          </v-col>
          <v-col cols="12" md="4">
            <p class="text-caption text-grey-darken-1 mb-1">Estado</p>
            <v-chip
              size="small"
              :color="colorEstado(apoderadoDetalle.estado)"
              variant="tonal"
            >
              {{ apoderadoDetalle.estado || 'SIN ESTADO' }}
            </v-chip>
          </v-col>
          <v-col cols="12">
            <p class="text-caption text-grey-darken-1 mb-1">Dirección</p>
            <p>{{ direccionTexto(apoderadoDetalle.direccion) }}</p>
          </v-col>
        </v-row>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { useMatriculaHttp } from '~/composables/useMatriculaHttp'
const { confirm: confirmAction } = useUiFeedback()

interface DireccionEmbedded {
  calle: string
  numero: string
  distrito: string
  provincia: string
  departamento: string
  codigoPostal: string
  referencia: string
}

interface Apoderado {
  id: string | null
  nombres: string
  apellidoPaterno: string
  apellidoMaterno: string
  dni: string | null
  tipoRelacion: string
  esApoderadoPrincipal: boolean
  telefonoCelular: string
  telefonoFijo: string
  correo: string
  direccion: DireccionEmbedded
  ocupacion: string
  centroTrabajo: string
  telefonoTrabajo: string
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
const formRef = ref()
const busqueda = ref('')
const filtroEstado = ref('TODOS')
const filtroRelacion = ref('TODOS')
const apoderadoDetalle = ref<Apoderado | null>(null)

const apoderados = ref<Apoderado[]>([])

const tiposRelacion = ['PADRE', 'MADRE', 'TUTOR_LEGAL', 'ABUELO', 'OTRO']
const estados = ['ACTIVO', 'INACTIVO']

const headers = [
  { title: 'Apoderado', key: 'nombreCompleto' },
  { title: 'Relación', key: 'tipoRelacion' },
  { title: 'Celular', key: 'telefonoCelular' },
  { title: 'Correo', key: 'correo' },
  { title: 'Principal', key: 'esApoderadoPrincipal' },
  { title: 'Estado', key: 'estado' },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'end' as const },
]

const rules = {
  required: (value: unknown) => !!value || 'Campo requerido',
  dni: (value: string | null) =>
    !value || value.length === 8 || 'El DNI debe tener 8 dígitos',
  email: (value: string) =>
    !value || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value) || 'Correo no válido',
}

const form = reactive<Apoderado>(crearFormVacio())

const totalActivos = computed(() =>
  apoderados.value.filter((item) => item.estado === 'ACTIVO').length
)
const totalPrincipales = computed(() =>
  apoderados.value.filter((item) => item.esApoderadoPrincipal).length
)

const apoderadosFiltrados = computed(() => {
  const texto = (busqueda.value || '').toLowerCase().trim()

  return apoderados.value.filter((item) => {
    const cumpleEstado =
      filtroEstado.value === 'TODOS' || item.estado === filtroEstado.value
    const cumpleRelacion =
      filtroRelacion.value === 'TODOS' ||
      item.tipoRelacion === filtroRelacion.value
    const contenido = [
      item.nombres,
      item.apellidoPaterno,
      item.apellidoMaterno,
      item.dni,
      item.tipoRelacion,
      item.telefonoCelular,
      item.telefonoFijo,
      item.correo,
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()

    return cumpleEstado && cumpleRelacion && (!texto || contenido.includes(texto))
  })
})

function crearDireccionVacia(): DireccionEmbedded {
  return {
    calle: '',
    numero: '',
    distrito: '',
    provincia: '',
    departamento: '',
    codigoPostal: '',
    referencia: '',
  }
}

function crearFormVacio(): Apoderado {
  return {
    id: null,
    nombres: '',
    apellidoPaterno: '',
    apellidoMaterno: '',
    dni: null,
    tipoRelacion: '',
    esApoderadoPrincipal: false,
    telefonoCelular: '',
    telefonoFijo: '',
    correo: '',
    direccion: crearDireccionVacia(),
    ocupacion: '',
    centroTrabajo: '',
    telefonoTrabajo: '',
    estado: 'ACTIVO',
  }
}

function normalizarDni(value: string | null) {
  return String(value || '').replace(/\D/g, '').slice(0, 8)
}

function limpiarTexto(value: string | null | undefined) {
  const limpio = String(value ?? '').trim()
  return limpio || null
}

function construirPayload() {
  return {
    nombres: limpiarTexto(form.nombres),
    apellidoPaterno: limpiarTexto(form.apellidoPaterno),
    apellidoMaterno: limpiarTexto(form.apellidoMaterno),
    dni: limpiarTexto(form.dni),
    tipoRelacion: limpiarTexto(form.tipoRelacion),
    esApoderadoPrincipal: !!form.esApoderadoPrincipal,
    telefonoCelular: limpiarTexto(form.telefonoCelular),
    telefonoFijo: limpiarTexto(form.telefonoFijo),
    correo: limpiarTexto(form.correo),
    direccion: {
      calle: limpiarTexto(form.direccion.calle),
      numero: limpiarTexto(form.direccion.numero),
      distrito: limpiarTexto(form.direccion.distrito),
      provincia: limpiarTexto(form.direccion.provincia),
      departamento: limpiarTexto(form.direccion.departamento),
      codigoPostal: limpiarTexto(form.direccion.codigoPostal),
      referencia: limpiarTexto(form.direccion.referencia),
    },
    ocupacion: limpiarTexto(form.ocupacion),
    centroTrabajo: limpiarTexto(form.centroTrabajo),
    telefonoTrabajo: limpiarTexto(form.telefonoTrabajo),
    estado: limpiarTexto(form.estado) || 'ACTIVO',
  }
}

function normalizarApoderado(item: any): Apoderado {
  return {
    id: item.id ?? null,
    nombres: item.nombres ?? '',
    apellidoPaterno: item.apellidoPaterno ?? '',
    apellidoMaterno: item.apellidoMaterno ?? '',
    dni: item.dni ?? null,
    tipoRelacion: item.tipoRelacion ?? '',
    esApoderadoPrincipal: !!item.esApoderadoPrincipal,
    telefonoCelular: item.telefonoCelular ?? '',
    telefonoFijo: item.telefonoFijo ?? '',
    correo: item.correo ?? '',
    direccion: { ...crearDireccionVacia(), ...(item.direccion || {}) },
    ocupacion: item.ocupacion ?? '',
    centroTrabajo: item.centroTrabajo ?? '',
    telefonoTrabajo: item.telefonoTrabajo ?? '',
    estado: item.estado ?? 'ACTIVO',
    usuarioRegistro: item.usuarioRegistro,
    fechaRegistro: item.fechaRegistro,
    fechaActualizacion: item.fechaActualizacion,
  }
}

async function cargar() {
  loading.value = true
  errorMsg.value = ''

  try {
    const data: any = await matricula('/api/apoderados')

    const lista = Array.isArray(data)
      ? data
      : Array.isArray(data?.apoderados)
        ? data.apoderados
        : []

    apoderados.value = lista.map(normalizarApoderado)
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al cargar apoderados'
    apoderados.value = []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, crearFormVacio())
}

function abrirNuevo() {
  resetForm()
  errorMsg.value = ''
  successMsg.value = ''
  dialog.value = true
}

function editar(item: Apoderado) {
  Object.assign(form, normalizarApoderado(item))
  dialog.value = true
}

function verDetalle(item: Apoderado) {
  apoderadoDetalle.value = normalizarApoderado(item)
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

    if (form.id) {
      await matricula(`/api/apoderados/${form.id}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      })
      successMsg.value = 'Apoderado actualizado correctamente.'
    } else {
      await matricula('/api/apoderados', {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      successMsg.value = 'Apoderado registrado correctamente.'
    }

    cerrarDialog()
    await cargar()
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al guardar apoderado'
  } finally {
    saving.value = false
  }
}

async function eliminar(id: string | null) {
  if (!id) return
  if (!await confirmAction('¿Eliminar este apoderado? Esta acción no se puede deshacer.')) return

  errorMsg.value = ''
  successMsg.value = ''

  try {
    const data: any = await matricula(`/api/apoderados/${id}`, {
      method: 'DELETE',
    })

    successMsg.value = data?.mensaje || 'Apoderado eliminado correctamente.'
    await cargar()
  } catch (error: any) {
    errorMsg.value = error.data?.mensaje || error.message || 'Error al eliminar apoderado'
  }
}

function nombreCompleto(item: Apoderado) {
  return (
    [item.nombres, item.apellidoPaterno, item.apellidoMaterno]
      .filter(Boolean)
      .join(' ') || 'Sin nombre'
  )
}

function textoRelacion(value: string) {
  const mapa: Record<string, string> = {
    PADRE: 'Padre',
    MADRE: 'Madre',
    TUTOR_LEGAL: 'Tutor legal',
    ABUELO: 'Abuelo(a)',
    OTRO: 'Otro',
  }
  return mapa[value] || value || 'Sin relación'
}

function colorEstado(value: string) {
  const mapa: Record<string, string> = {
    ACTIVO: 'success',
    INACTIVO: 'grey',
  }
  return mapa[value] || 'default'
}

function direccionTexto(direccion?: DireccionEmbedded) {
  if (!direccion) return '-'

  const partes = [
    direccion.calle,
    direccion.numero,
    direccion.distrito,
    direccion.provincia,
    direccion.departamento,
    direccion.referencia ? `Ref: ${direccion.referencia}` : '',
  ].filter(Boolean)

  return partes.length ? partes.join(', ') : '-'
}

onMounted(() => {
  cargar()
})
</script>