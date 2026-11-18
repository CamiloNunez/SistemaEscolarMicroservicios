<template>
  <v-app>
    <div class="login-bg"></div>
    <div class="login-mask"></div>

    <v-main class="login-page">
      <v-container fluid class="fill-height login-content">
        <v-row justify="center" align="center" class="fill-height">
          <v-col cols="12" sm="8" md="5" lg="4">
            <v-card class="login-card pa-8 mx-auto" elevation="8">
              <div class="text-center mb-6">
                <img src="~/assets/logo.png" class="logo-box mx-auto d-block mb-4">

                <h1 class="text-h5 font-weight-bold" style="color:#5b0014">
                  Thales de Mileto
                </h1>

                <p class="text-caption text-grey-darken-1 mt-1">
                  Sistema de Gestión Educativa
                </p>
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

              <v-form ref="formRef" @submit.prevent="handleLogin">
                <v-text-field
                  v-model="form.username"
                  label="Usuario"
                  prepend-inner-icon="mdi-account-outline"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.required]"
                  class="mb-3"
                />

                <v-text-field
                  v-model="form.password"
                  label="Contraseña"
                  prepend-inner-icon="mdi-lock-outline"
                  :append-inner-icon="showPass ? 'mdi-eye-off' : 'mdi-eye'"
                  :type="showPass ? 'text' : 'password'"
                  variant="outlined"
                  density="compact"
                  :rules="[rules.required]"
                  class="mb-1"
                  @click:append-inner="showPass = !showPass"
                />

                <v-expand-transition>
                  <v-text-field
                    v-if="showAdvanced"
                    v-model="loginApi"
                    label="Backend URL"
                    prepend-inner-icon="mdi-server-outline"
                    variant="outlined"
                    density="compact"
                    class="mb-3"
                  />
                </v-expand-transition>

                <div class="d-flex justify-space-between align-center mb-4">
                  <v-switch
                    v-model="demoMode"
                    label="Modo demo"
                    color="primary"
                    density="compact"
                    hide-details
                  />

                  <v-btn
                    variant="text"
                    size="small"
                    color="primary"
                    @click="showAdvanced = !showAdvanced"
                  >
                    {{ showAdvanced ? 'Ocultar' : 'Avanzado' }}
                  </v-btn>
                </div>

                <v-btn
                  type="submit"
                  block
                  size="large"
                  color="primary"
                  :loading="loading"
                  class="text-none font-weight-bold"
                >
                  <v-icon start>mdi-login</v-icon>
                  Iniciar sesión
                </v-btn>
              </v-form>

              <p class="text-center text-caption text-grey mt-6 mb-0">
                © {{ new Date().getFullYear() }} IEP Thales de Mileto
              </p>
            </v-card>
          </v-col>
        </v-row>
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
definePageMeta({ layout: false })

const authStore = useAuthStore()
const router = useRouter()
const config = useRuntimeConfig()

const formRef = ref()
const loading = ref(false)
const showPass = ref(false)
const showAdvanced = ref(false)
const demoMode = ref(false)
const errorMsg = ref('')
const loginApi = ref(config.public.loginApiBase)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  required: (v: string) => !!v || 'Campo requerido',
}

onMounted(() => {
  authStore.restoreSession()

  if (authStore.isAuthenticated) {
    router.replace('/')
  }
})

async function handleLogin() {
  const { valid } = await formRef.value.validate()
  if (!valid) return

  loading.value = true
  errorMsg.value = ''

  try {
    if (demoMode.value) {
      if (form.password !== '123456') {
        throw new Error('Contraseña incorrecta. En modo demo usa: 123456')
      }

      authStore.loginDemo(form.username)
    } else {
      await authStore.login(form.username, form.password, loginApi.value)
    }

    router.push('/')
  } catch (err: any) {
    errorMsg.value = err.message || 'Error al iniciar sesión'
  } finally {
    loading.value = false
  }
}
</script>

<style>
html,
body,
#__nuxt,
.v-application,
.v-application__wrap {
  min-height: 100vh !important;
  background: transparent !important;
}

.login-bg {
  position: fixed;
  inset: 0;
  background-image: url("~/assets/fondo-login.png");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  z-index: 0;
}

.login-mask {
  position: fixed;
  inset: 0;
  background: rgba(229, 210, 215, 0.72);
  z-index: 1;
}

.login-page {
  position: relative;
  z-index: 2;
  min-height: 100vh !important;
  background: transparent !important;
}

.login-content {
  min-height: 100vh;
}

.login-card {
  background: rgba(255, 255, 255, 0.92) !important;
  backdrop-filter: blur(12px);
  border: 1px solid rgba(89, 10, 21, 0.12);
  border-radius: 22px !important;
}

.logo-box {
  width: 90px;
  height: 90px;
  object-fit: contain;
}
</style>
