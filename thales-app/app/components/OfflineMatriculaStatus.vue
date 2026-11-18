<template>
  <v-alert v-if="!available" type="warning" variant="tonal" class="mb-4">
    El almacenamiento pendiente requiere la PWA activa en una compilación de producción.
  </v-alert>
  <v-alert v-if="offlineError" type="error" class="mb-4">{{ offlineError }}</v-alert>
  <v-alert v-if="status.rows.length" type="warning" variant="tonal" class="mb-4">
    {{ status.rows.length }} operación(es) guardada(s) en este dispositivo, pendientes de confirmación.
    Se reintentará al recuperarse el servicio mientras la aplicación esté abierta y tu sesión siga vigente.
    <div v-for="row in status.rows" :key="row.id" class="mt-2">
      {{ row.method }} {{ row.path }} — {{ row.state }}
      <span v-if="row.error">: {{ row.error }}</span>
    </div>
    <v-btn class="mt-2" variant="text" :loading="syncing" @click="sync">Reintentar ahora</v-btn>
  </v-alert>
  <v-alert v-else-if="status.history.length" type="success" variant="tonal" class="mb-4">
    Última operación confirmada por el servidor: {{ status.history[status.history.length - 1]?.syncedAt }}
  </v-alert>
</template>

<script setup lang="ts">
const emit = defineEmits<{ synced: [] }>()
const { status, available, offlineError, syncing, sync } = useOfflineAttendance('matricula')
watch(() => status.value.history.map(row => row.id).join(','), (value, previous) => {
  if (value && value !== previous) emit('synced')
})
</script>
