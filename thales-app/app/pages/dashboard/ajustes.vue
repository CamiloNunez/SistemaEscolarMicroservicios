<template>
  <v-container class="py-8">
    <div class="d-flex align-center ga-3 mb-6">
      <v-icon size="32" color="primary">mdi-cog-outline</v-icon>
      <h1 class="text-h5 font-weight-bold" style="color:#5b0014">Ajustes</h1>
    </div>

    <v-card class="main-card pa-8 text-center" elevation="3">
      <v-icon size="80" color="secondary" class="mb-4">mdi-cog-outline</v-icon>
      <h2 class="text-h6 mb-2" style="color:#5b0014">Configuración del Sistema</h2>
      <p class="text-body-2 text-grey-darken-1">
        Módulo en desarrollo. Aquí podrás configurar los parámetros del sistema.
      </p>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
</script>