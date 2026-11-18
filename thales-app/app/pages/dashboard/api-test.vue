<template>
  <v-container class="py-8">

    <div class="d-flex align-center ga-3 mb-6">
      <v-icon size="32" color="primary">mdi-api</v-icon>
      <div>
        <h1 class="text-h5 font-weight-bold" style="color:#5b0014">Test API</h1>
        <p class="text-body-2 text-grey-darken-1 mb-0">
          Panel para probar endpoints del microservicio
        </p>
      </div>
    </div>

    <v-row>
      <!-- ── Panel izquierdo: configuración ─────────── -->
      <v-col cols="12" md="5">
        <v-card class="main-card pa-5" elevation="3">
          <h2 class="text-h6 mb-4" style="color:#5b0014">Configuración</h2>

          <v-text-field
            v-model="loginApi"
            label="Base URL"
            prepend-inner-icon="mdi-server-outline"
            variant="outlined"
            density="compact"
          />

          <v-select
            v-model="selectedEndpoint"
            :items="endpoints"
            item-title="name"
            item-value="value"
            label="Endpoint"
            variant="outlined"
            density="compact"
            return-object
          />

          <v-chip class="endpoint-chip mb-4">
            {{ selectedEndpoint?.method }} {{ selectedEndpoint?.path }}
          </v-chip>

          <!-- Token bearer (opcional) -->
          <v-text-field
            v-model="bearerToken"
            label="Bearer Token (opcional)"
            prepend-inner-icon="mdi-key-outline"
            variant="outlined"
            density="compact"
            class="mb-2"
          />

          <v-textarea
            v-if="selectedEndpoint?.body"
            v-model="requestBody"
            label="Body JSON"
            variant="outlined"
            rows="8"
            auto-grow
            class="mb-3"
          />

          <v-alert
            v-else
            type="info"
            variant="tonal"
            class="mb-4"
            density="compact"
          >
            Este endpoint no necesita body.
          </v-alert>

          <v-btn
            block
            size="large"
            :loading="loading"
            color="primary"
            class="text-none font-weight-bold"
            @click="sendRequest"
          >
            <v-icon start>mdi-send</v-icon>
            Enviar solicitud
          </v-btn>
        </v-card>
      </v-col>

      <!-- ── Panel derecho: respuesta ───────────────── -->
      <v-col cols="12" md="7">
        <v-card class="main-card pa-5" elevation="3">
          <div class="d-flex justify-space-between align-center mb-4">
            <h2 class="text-h6" style="color:#5b0014">Respuesta</h2>

            <div class="d-flex ga-2 align-center">
              <v-chip
                v-if="statusCode"
                :color="statusCode >= 200 && statusCode < 300 ? 'green' : 'red'"
                variant="flat"
                size="small"
              >
                Status: {{ statusCode }}
              </v-chip>

              <v-btn
                v-if="responseText && responseText !== placeholderText"
                icon="mdi-content-copy"
                size="small"
                variant="text"
                @click="copyResponse"
              />

              <v-btn
                v-if="responseText && responseText !== placeholderText"
                icon="mdi-delete-outline"
                size="small"
                variant="text"
                @click="clearResponse"
              />
            </div>
          </div>

          <div class="response-box">{{ responseText }}</div>

          <!-- Snackbar de copiado -->
          <v-snackbar v-model="snackbar" :timeout="2000" color="success">
            ✓ Respuesta copiada al portapapeles
          </v-snackbar>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'


definePageMeta({ middleware: 'auth' })

const authStore = useAuthStore()
const config = useRuntimeConfig()

/* ── Estado ───────────────────────────────────────────── */
const placeholderText = 'Aquí aparecerá la respuesta del servidor.'

const loginApi     = ref(config.public.loginApiBase)
const loading      = ref(false)
const statusCode   = ref<number | null>(null)
const responseText = ref(placeholderText)
const bearerToken  = ref(authStore.token ?? '')
const requestBody  = ref('')
const snackbar     = ref(false)

/* ── Endpoints ────────────────────────────────────────── */
interface Endpoint {
  name:        string
  method:      string
  path:        string
  value:       string
  body:        boolean
  defaultBody?: Record<string, unknown>
}

const endpoints: Endpoint[] = [
  {
    name:        'Login',
    method:      'POST',
    path:        '/api/auth/login',
    value:       'login',
    body:        true,
    defaultBody: { username: 'admin', password: '123456' },
  },
  {
    name:   'Test simple',
    method: 'GET',
    path:   '/test',
    value:  'test',
    body:   false,
  },
  {
    name:   'Usuarios',
    method: 'GET',
    path:   '/api/usuarios',
    value:  'usuarios',
    body:   false,
  },
  {
    name:   'Roles',
    method: 'GET',
    path:   '/api/roles',
    value:  'roles',
    body:   false,
  },
]

const selectedEndpoint = ref<Endpoint>(endpoints[0])

/* ── Inicializar body ─────────────────────────────────── */
onMounted(() => {
  requestBody.value = JSON.stringify(selectedEndpoint.value.defaultBody, null, 2)
})

/* ── Watcher de endpoint ──────────────────────────────── */
watch(selectedEndpoint, (endpoint) => {
  statusCode.value   = null
  responseText.value = `Listo para probar: ${endpoint.method} ${endpoint.path}`

  requestBody.value = endpoint.body
    ? JSON.stringify(endpoint.defaultBody ?? {}, null, 2)
    : ''
})

/* ── Enviar solicitud ─────────────────────────────────── */
async function sendRequest() {
  loading.value      = true
  statusCode.value   = null
  responseText.value = 'Enviando solicitud...'

  try {
    const endpoint = selectedEndpoint.value
    const url      = loginApi.value + endpoint.path

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    }

    if (bearerToken.value) {
      headers['Authorization'] = `Bearer ${bearerToken.value}`
    }

    const options: RequestInit = { method: endpoint.method, headers }

    if (endpoint.body) {
      try {
        options.body = JSON.stringify(JSON.parse(requestBody.value))
      } catch {
        responseText.value = '⚠ El body no es un JSON válido.'
        loading.value      = false
        return
      }
    }

    const response  = await fetch(url, options)
    statusCode.value = response.status

    const contentType = response.headers.get('content-type')

    if (contentType?.includes('application/json')) {
      const data     = await response.json()
      responseText.value = JSON.stringify(data, null, 2)
    } else {
      const text     = await response.text()
      responseText.value = text || 'Respuesta vacía.'
    }

  } catch (error) {
    responseText.value =
      '❌ Error al conectar con el backend.\n\n' +
      'Revisa:\n' +
      '  • Que el microservicio Login esté disponible en ' + loginApi.value + '\n' +
      '  • Que el endpoint exista\n' +
      '  • Que CORS permita tu frontend\n\n' +
      String(error)
  } finally {
    loading.value = false
  }
}

/* ── Copiar respuesta ─────────────────────────────────── */
async function copyResponse() {
  await navigator.clipboard.writeText(responseText.value)
  snackbar.value = true
}

/* ── Limpiar respuesta ────────────────────────────────── */
function clearResponse() {
  responseText.value = placeholderText
  statusCode.value   = null
}
</script>
