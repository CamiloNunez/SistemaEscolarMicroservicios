<template>
  <v-container class="py-8">
    <v-alert
      v-if="errorMsg"
      type="error"
      variant="tonal"
      class="mb-4"
    >
      {{ errorMsg }}
    </v-alert>

    <v-card class="main-card pa-6" :loading="loading">
      <div class="d-flex align-center mb-6">
        <v-avatar size="90" class="header-avatar mr-5">
          <v-icon size="45">mdi-account</v-icon>
        </v-avatar>

        <div>
          <h2 class="text-h5 font-weight-bold">
            {{ authStore.displayName }}
          </h2>
          <p class="text-caption mb-0">
            {{ authStore.displayRole }}
          </p>
          <v-chip
            v-if="authStore.user?.perfil"
            size="small"
            color="success"
            variant="tonal"
            class="mt-2"
          >
            Perfil vinculado
          </v-chip>
        </div>
      </div>

      <v-divider class="mb-5" />

      <v-row>
        <v-col cols="12" md="6">
          <v-text-field label="Nombre completo" :model-value="authStore.user?.nombreCompleto" readonly variant="outlined" />
        </v-col>
        <v-col cols="12" md="6">
          <v-text-field label="Usuario" :model-value="authStore.user?.username" readonly variant="outlined" />
        </v-col>
        <v-col cols="12" md="6">
          <v-text-field label="Correo" :model-value="authStore.user?.email" readonly variant="outlined" />
        </v-col>
        <v-col cols="12" md="6">
          <v-text-field label="Tipo de usuario" :model-value="authStore.user?.tipoUsuario || 'Sin tipo'" readonly variant="outlined" />
        </v-col>
        <v-col cols="12">
          <v-text-field label="Roles" :model-value="rolesTexto" readonly variant="outlined" />
        </v-col>
        <v-col v-if="authStore.user?.grado" cols="12" md="6">
          <v-text-field label="Grado" :model-value="authStore.user?.grado" readonly variant="outlined" />
        </v-col>
        <v-col v-if="authStore.user?.seccion" cols="12" md="6">
          <v-text-field label="Sección" :model-value="authStore.user?.seccion" readonly variant="outlined" />
        </v-col>
      </v-row>

      <template v-if="authStore.user?.perfil">
        <v-divider class="my-5" />
        <h3 class="text-subtitle-1 font-weight-bold mb-4">Perfil funcional</h3>
        <v-row>
          <v-col cols="12" md="6">
            <v-text-field label="Entidad vinculada" :model-value="authStore.user.perfil.entidadId" readonly variant="outlined" />
          </v-col>
          <v-col cols="12" md="6">
            <v-text-field label="Código de perfil" :model-value="authStore.user.perfil.codigoPerfil" readonly variant="outlined" />
          </v-col>
          <v-col cols="12" md="6">
            <v-text-field label="Origen" :model-value="authStore.user.perfil.origen" readonly variant="outlined" />
          </v-col>
          <v-col cols="12" md="6">
            <v-text-field label="Estado del perfil" :model-value="authStore.user.perfil.activo ? 'ACTIVO' : 'INACTIVO'" readonly variant="outlined" />
          </v-col>
        </v-row>
      </template>

      <v-alert v-else type="warning" variant="tonal" class="mt-4">
        Este usuario está autenticado, pero todavía no tiene una relación en perfiles_usuario.
      </v-alert>

      <div class="d-flex justify-end mt-5">
        <v-btn color="primary" variant="tonal" :loading="loading" @click="cargarPerfil">
          <v-icon start>mdi-refresh</v-icon>
          Actualizar perfil
        </v-btn>
      </div>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
definePageMeta({ middleware: 'auth' })

const authStore = useAuthStore()
const config = useRuntimeConfig()
const loading = ref(false)
const errorMsg = ref('')

const rolesTexto = computed(() => authStore.user?.roles?.join(', ') || 'Sin roles')

async function cargarPerfil() {
  loading.value = true
  errorMsg.value = ''
  try {
    await authStore.refreshCurrentUser(String(config.public.loginApiBase))
  } catch (error: any) {
    errorMsg.value = error?.message || 'No se pudo consultar el perfil.'
  } finally {
    loading.value = false
  }
}

onMounted(cargarPerfil)
</script>
