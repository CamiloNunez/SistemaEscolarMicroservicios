<template>
  <div>
    <OfflineMatriculaStatus @synced="cargar" />
    <!-- Encabezado -->
    <div class="d-flex justify-space-between align-center mb-6">


      <v-btn color="primary" @click="abrirNuevo">
        <v-icon start>mdi-plus</v-icon>
        Nuevo estudiante
      </v-btn>
    </div>

    <!-- Alertas -->
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

    <!-- Tabla -->
    <v-card class="main-card">
      <v-data-table
        :headers="headers"
        :items="estudiantes"
        :loading="loading"
        item-value="id"
      >
        <template #item.nombreCompleto="{ item }">
          {{ item.nombres }} {{ item.apellidoPaterno }} {{ item.apellidoMaterno }}
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
        </template>
      </v-data-table>
    </v-card>

    <!-- Dialog Crear / Editar -->
    <v-dialog v-model="dialog" max-width="1000" persistent>
      <v-card class="pa-5">
        <DialogHeader
          :title="form.id ? 'Editar estudiante' : 'Nuevo estudiante'"
          subtitle="Complete los datos del estudiante."
          @close="cerrarDialog"
        />

        <v-form ref="formRef">
          <!-- Datos personales -->
          <v-card variant="outlined" class="pa-4 mb-4">
            <h3 class="text-subtitle-1 font-weight-bold mb-4">
              Datos personales
            </h3>

            <v-row>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model="form.nombres"
                  label="Nombres"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.required]"
                />
              </v-col>

              <v-col cols="12" md="4">
                <v-text-field
                  v-model="form.apellidoPaterno"
                  label="Apellido paterno"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.required]"
                />
              </v-col>

              <v-col cols="12" md="4">
                <v-text-field
                  v-model="form.apellidoMaterno"
                  label="Apellido materno"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.required]"
                />
              </v-col>

              <v-col cols="12" md="4">
                <v-text-field
                  v-model="form.dni"
                  label="DNI"
                  variant="outlined"
                  density="compact"
                  maxlength="8"
                />
              </v-col>

              <v-col cols="12" md="4">
                <v-text-field
                  v-model="form.codigoEstudiante"
                  label="Código estudiante"
                  variant="outlined"
                  density="compact"
                />
              </v-col>

              <v-col cols="12" md="4">
                <v-text-field
                  v-model="form.fechaNacimiento"
                  label="Fecha de nacimiento"
                  type="date"
                  variant="outlined"
                  density="compact"
                />
              </v-col>

              <v-col cols="12" md="4">
                <v-select
                  v-model="form.genero"
                  :items="generos"
                  label="Género"
                  variant="outlined"
                  density="compact"
                  clearable
                />
              </v-col>

              <v-col cols="12" md="4">
                <v-text-field
                  v-model="form.lugarNacimiento"
                  label="Lugar de nacimiento"
                  variant="outlined"
                  density="compact"
                />
              </v-col>

              <v-col cols="12" md="4">
                <v-text-field
                  v-model="form.nacionalidad"
                  label="Nacionalidad"
                  variant="outlined"
                  density="compact"
                />
              </v-col>
            </v-row>
          </v-card>

          <!-- Contacto -->
          <v-card variant="outlined" class="pa-4 mb-4">
            <h3 class="text-subtitle-1 font-weight-bold mb-4">
              Contacto
            </h3>

            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="form.telefono"
                  label="Teléfono"
                  variant="outlined"
                  density="compact"
                />
              </v-col>

              <v-col cols="12" md="6">
                <v-text-field
                  v-model="form.correo"
                  label="Correo"
                  variant="outlined"
                  density="compact"
                  type="email"
                />
              </v-col>
            </v-row>
          </v-card>

          <!-- Dirección -->
          <v-card variant="outlined" class="pa-4 mb-4">
            <h3 class="text-subtitle-1 font-weight-bold mb-4">
              Dirección
            </h3>

            <v-row>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model="form.direccion.calle"
                  label="Calle"
                  variant="outlined"
                  density="compact"
                />
              </v-col>

              <v-col cols="12" md="2">
                <v-text-field
                  v-model="form.direccion.numero"
                  label="Número"
                  variant="outlined"
                  density="compact"
                />
              </v-col>

              <v-col cols="12" md="3">
                <v-text-field
                  v-model="form.direccion.distrito"
                  label="Distrito"
                  variant="outlined"
                  density="compact"
                />
              </v-col>

              <v-col cols="12" md="3">
                <v-text-field
                  v-model="form.direccion.provincia"
                  label="Provincia"
                  variant="outlined"
                  density="compact"
                />
              </v-col>

              <v-col cols="12" md="4">
                <v-text-field
                  v-model="form.direccion.departamento"
                  label="Departamento"
                  variant="outlined"
                  density="compact"
                />
              </v-col>

              <v-col cols="12" md="4">
                <v-text-field
                  v-model="form.direccion.codigoPostal"
                  label="Código postal"
                  variant="outlined"
                  density="compact"
                />
              </v-col>

              <v-col cols="12" md="4">
                <v-text-field
                  v-model="form.direccion.referencia"
                  label="Referencia"
                  variant="outlined"
                  density="compact"
                />
              </v-col>
            </v-row>
          </v-card>

          <!-- Datos médicos -->
          <v-card variant="outlined" class="pa-4 mb-4">
            <h3 class="text-subtitle-1 font-weight-bold mb-4">
              Datos médicos
            </h3>

            <v-row>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model="form.grupoSanguineo"
                  label="Grupo sanguíneo"
                  variant="outlined"
                  density="compact"
                />
              </v-col>

              <v-col cols="12" md="8">
                <v-combobox
                  v-model="form.condicionesEspeciales"
                  label="Condiciones especiales"
                  variant="outlined"
                  density="compact"
                  multiple
                  chips
                  clearable
                />
              </v-col>

              <v-col cols="12">
                <v-switch
                  v-model="form.requiereAtencionEspecial"
                  label="Requiere atención especial"
                  color="primary"
                  hide-details
                />
              </v-col>
            </v-row>
          </v-card>

          <!-- Apoderados y estado -->
          <v-card variant="outlined" class="pa-4 mb-4">
            <h3 class="text-subtitle-1 font-weight-bold mb-4">
              Apoderados y estado
            </h3>

            <v-row>
              <v-col cols="12" md="8">
                <v-autocomplete
                  v-model="form.apoderadoIds"
                  v-model:search="busquedaApoderado"
                  :items="opcionesApoderados"
                  item-title="titulo"
                  item-value="id"
                  :loading="buscandoApoderados"
                  :error-messages="errorApoderados"
                  :no-data-text="buscandoApoderados ? 'Buscando apoderados...' : busquedaApoderado.trim().length < 2 ? 'Escriba al menos 2 caracteres del nombre o apellido' : 'No se encontraron apoderados'"
                  label="Buscar y asignar apoderados"
                  hint="Busque por nombre o apellido y seleccione un apoderado."
                  persistent-hint
                  no-filter
                  variant="outlined"
                  density="compact"
                  multiple
                  chips
                  closable-chips
                  clearable
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
        </v-form>

        <!-- Acciones -->
        <div class="d-flex justify-end ga-2 mt-5">
          <v-btn variant="text" @click="cerrarDialog">
            Cancelar
          </v-btn>

          <v-btn
            color="primary"
            :loading="saving"
            @click="guardar"
          >
            Guardar
          </v-btn>
        </div>
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

interface Estudiante {
  id: string | null
  nombres: string
  apellidoPaterno: string
  apellidoMaterno: string
  dni: string | null
  codigoEstudiante: string
  fechaNacimiento: string | null
  genero: string
  lugarNacimiento: string
  nacionalidad: string
  direccion: DireccionEmbedded
  telefono: string
  correo: string
  grupoSanguineo: string
  condicionesEspeciales: string[]
  requiereAtencionEspecial: boolean
  apoderadoIds: string[]
  estado: string
}

const { matricula } = useMatriculaHttp()
const offlineMatricula = useOfflineAttendance('matricula')


const loading = ref(false)
const saving = ref(false)
const dialog = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const formRef = ref()

const estudiantes = ref<Estudiante[]>([])

interface OpcionApoderado {
  id: string
  titulo: string
}

const busquedaApoderado = ref('')
const opcionesApoderados = ref<OpcionApoderado[]>([])
const buscandoApoderados = ref(false)
const errorApoderados = ref('')
let temporizadorApoderados: ReturnType<typeof setTimeout> | undefined
let versionBusquedaApoderados = 0
let sesionApoderados = 0

function opcionApoderado(item: any): OpcionApoderado {
  const nombre = [item.nombres, item.apellidoPaterno, item.apellidoMaterno].filter(Boolean).join(' ')
  return { id: item.id, titulo: `${nombre || 'Apoderado'}${item.dni ? ` · DNI: ${item.dni}` : ''}` }
}

function reiniciarBusquedaApoderados() {
  sesionApoderados++
  clearTimeout(temporizadorApoderados)
  versionBusquedaApoderados++
  busquedaApoderado.value = ''
  opcionesApoderados.value = []
  buscandoApoderados.value = false
  errorApoderados.value = ''
}

watch(busquedaApoderado, (texto) => {
  clearTimeout(temporizadorApoderados)
  const version = ++versionBusquedaApoderados
  const nombre = texto.trim()
  buscandoApoderados.value = false
  errorApoderados.value = ''
  opcionesApoderados.value = opcionesApoderados.value.filter(item => form.apoderadoIds.includes(item.id))
  if (!dialog.value || nombre.length < 2) return

  buscandoApoderados.value = true
  temporizadorApoderados = setTimeout(async () => {
    try {
      const data = await matricula(`/api/apoderados/buscar?nombre=${encodeURIComponent(nombre)}`)
      if (version !== versionBusquedaApoderados) return
      const seleccionados = opcionesApoderados.value.filter(item => form.apoderadoIds.includes(item.id))
      const resultados = Array.isArray(data) ? data.filter(item => item.id).map(opcionApoderado) : []
      opcionesApoderados.value = [...new Map([...seleccionados, ...resultados].map(item => [item.id, item])).values()]
    } catch (error: any) {
      if (version === versionBusquedaApoderados) {
        errorApoderados.value = error.data?.mensaje || error.message || 'Error al buscar apoderados'
      }
    } finally {
      if (version === versionBusquedaApoderados) buscandoApoderados.value = false
    }
  }, 350)
})

async function cargarApoderadosAsignados() {
  const sesion = sesionApoderados
  const ids = [...form.apoderadoIds]
  opcionesApoderados.value = ids.map(id => ({ id, titulo: 'Cargando apoderado...' }))
  const resultados = await Promise.allSettled(ids.map(id => matricula(`/api/apoderados/${encodeURIComponent(id)}`)))
  if (sesion !== sesionApoderados || !dialog.value) return
  const asignados = resultados.map((resultado, index) => resultado.status === 'fulfilled'
    ? opcionApoderado(resultado.value)
    : { id: ids[index]!, titulo: 'Apoderado no disponible' })
  opcionesApoderados.value = [...new Map([...opcionesApoderados.value, ...asignados].map(item => [item.id, item])).values()]
  if (resultados.some(resultado => resultado.status === 'rejected')) {
    errorApoderados.value = 'No se pudieron cargar algunos apoderados asignados. Intente abrir el formulario nuevamente.'
  }
}

watch(dialog, (abierto) => {
  if (!abierto) reiniciarBusquedaApoderados()
})

onBeforeUnmount(() => {
  clearTimeout(temporizadorApoderados)
  versionBusquedaApoderados++
})

const generos = ['M', 'F', 'OTRO']
const estados = ['ACTIVO', 'INACTIVO', 'RETIRADO', 'EGRESADO']

const headers = [
  { title: 'Estudiante', key: 'nombreCompleto' },
  { title: 'DNI', key: 'dni' },
  { title: 'Código', key: 'codigoEstudiante' },
  { title: 'Género', key: 'genero' },
  { title: 'Teléfono', key: 'telefono' },
  { title: 'Estado', key: 'estado' },
  { title: 'Acciones', key: 'actions', sortable: false, align: 'end' },
] as const

const rules = {
  required: (value: string) => !!value || 'Campo requerido',
}

const form = reactive<Estudiante>({
  id: null,
  nombres: '',
  apellidoPaterno: '',
  apellidoMaterno: '',
  dni: null,
  codigoEstudiante: '',
  fechaNacimiento: null,
  genero: '',
  lugarNacimiento: '',
  nacionalidad: '',
  direccion: {
    calle: '',
    numero: '',
    distrito: '',
    provincia: '',
    departamento: '',
    codigoPostal: '',
    referencia: '',
  },
  telefono: '',
  correo: '',
  grupoSanguineo: '',
  condicionesEspeciales: [],
  requiereAtencionEspecial: false,
  apoderadoIds: [],
  estado: 'ACTIVO',
})

function limpiarForm() {
  reiniciarBusquedaApoderados()
  Object.assign(form, {
    id: null,
    nombres: '',
    apellidoPaterno: '',
    apellidoMaterno: '',
    dni: null,
    codigoEstudiante: '',
    fechaNacimiento: null,
    genero: '',
    lugarNacimiento: '',
    nacionalidad: '',
    direccion: {
      calle: '',
      numero: '',
      distrito: '',
      provincia: '',
      departamento: '',
      codigoPostal: '',
      referencia: '',
    },
    telefono: '',
    correo: '',
    grupoSanguineo: '',
    condicionesEspeciales: [],
    requiereAtencionEspecial: false,
    apoderadoIds: [],
    estado: 'ACTIVO',
  })
}

function normalizarEstudiante(item: any): Estudiante {
  return {
    id: item.id ?? null,
    nombres: item.nombres ?? '',
    apellidoPaterno: item.apellidoPaterno ?? '',
    apellidoMaterno: item.apellidoMaterno ?? '',
    dni: item.dni ?? null,
    codigoEstudiante: item.codigoEstudiante ?? '',
    fechaNacimiento: item.fechaNacimiento ?? null,
    genero: item.genero ?? '',
    lugarNacimiento: item.lugarNacimiento ?? '',
    nacionalidad: item.nacionalidad ?? '',
    direccion: {
      calle: item.direccion?.calle ?? '',
      numero: item.direccion?.numero ?? '',
      distrito: item.direccion?.distrito ?? '',
      provincia: item.direccion?.provincia ?? '',
      departamento: item.direccion?.departamento ?? '',
      codigoPostal: item.direccion?.codigoPostal ?? '',
      referencia: item.direccion?.referencia ?? '',
    },
    telefono: item.telefono ?? '',
    correo: item.correo ?? '',
    grupoSanguineo: item.grupoSanguineo ?? '',
    condicionesEspeciales: Array.isArray(item.condicionesEspeciales)
      ? item.condicionesEspeciales
      : [],
    requiereAtencionEspecial: Boolean(item.requiereAtencionEspecial),
    apoderadoIds: Array.isArray(item.apoderadoIds)
      ? item.apoderadoIds
      : [],
    estado: item.estado ?? 'ACTIVO',
  }
}

function construirPayload() {
  return {
    nombres: form.nombres,
    apellidoPaterno: form.apellidoPaterno,
    apellidoMaterno: form.apellidoMaterno,
    dni: form.dni || null,
    codigoEstudiante: form.codigoEstudiante,
    fechaNacimiento: form.fechaNacimiento || null,
    genero: form.genero,
    lugarNacimiento: form.lugarNacimiento,
    nacionalidad: form.nacionalidad,
    direccion: {
      calle: form.direccion.calle,
      numero: form.direccion.numero,
      distrito: form.direccion.distrito,
      provincia: form.direccion.provincia,
      departamento: form.direccion.departamento,
      codigoPostal: form.direccion.codigoPostal,
      referencia: form.direccion.referencia,
    },
    telefono: form.telefono,
    correo: form.correo,
    grupoSanguineo: form.grupoSanguineo,
    condicionesEspeciales: form.condicionesEspeciales,
    requiereAtencionEspecial: form.requiereAtencionEspecial,
    apoderadoIds: form.apoderadoIds,
    estado: form.estado,
  }
}

function colorEstado(estado: string) {
  if (estado === 'ACTIVO') return 'success'
  if (estado === 'INACTIVO') return 'grey'
  if (estado === 'RETIRADO') return 'warning'
  if (estado === 'EGRESADO') return 'info'
  return 'grey'
}

async function cargar() {
  loading.value = true
  errorMsg.value = ''
  successMsg.value = ''

  try {
    const data = await matricula('/api/estudiantes')

    estudiantes.value = Array.isArray(data)
      ? data.map((item: any) => normalizarEstudiante(item))
      : []
  } catch (error: any) {
    errorMsg.value = error.message || 'Error al listar estudiantes'
  } finally {
    loading.value = false
  }
}

function abrirNuevo() {
  limpiarForm()
  errorMsg.value = ''
  successMsg.value = ''
  dialog.value = true
}

function editar(item: any) {
  reiniciarBusquedaApoderados()
  const estudiante = normalizarEstudiante(item)

  Object.assign(form, estudiante)
  form.direccion = { ...estudiante.direccion }
  form.condicionesEspeciales = [...estudiante.condicionesEspeciales]
  form.apoderadoIds = [...estudiante.apoderadoIds]

  errorMsg.value = ''
  successMsg.value = ''
  dialog.value = true
  cargarApoderadosAsignados()
}

function cerrarDialog() {
  dialog.value = false
  limpiarForm()
}

async function guardar() {
  const validation = await formRef.value?.validate()

  if (validation && !validation.valid) {
    return
  }

  saving.value = true
  errorMsg.value = ''
  successMsg.value = ''

  try {
    const payload = construirPayload()

    if (form.id) {
      await offlineMatricula.enqueue(`/api/estudiantes/${form.id}`, 'PUT', JSON.stringify(payload))
    } else {
      await offlineMatricula.enqueue('/api/estudiantes', 'POST', JSON.stringify(payload))
    }

    successMsg.value = form.id
      ? 'Cambio guardado en este dispositivo; pendiente de confirmación del servidor.'
      : 'Estudiante guardado en este dispositivo; pendiente de confirmación del servidor.'

    dialog.value = false
    limpiarForm()

  } catch (error: any) {
    errorMsg.value = error.message || 'Error al guardar estudiante'
  } finally {
    saving.value = false
  }
}

async function eliminar(id: string | null) {
  if (!id) return

  const confirmado = await confirmAction('¿Eliminar este estudiante?')
  if (!confirmado) return

  loading.value = true
  errorMsg.value = ''
  successMsg.value = ''

  try {
    await offlineMatricula.enqueue(`/api/estudiantes/${id}`, 'DELETE')

    successMsg.value = 'Eliminación guardada en este dispositivo; pendiente de confirmación del servidor.'

  } catch (error: any) {
    errorMsg.value = error.message || 'Error al eliminar estudiante'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  cargar()
})
</script>
