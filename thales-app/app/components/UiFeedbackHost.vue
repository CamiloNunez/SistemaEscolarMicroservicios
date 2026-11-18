<template>
  <div>
    <v-snackbar
      :model-value="snackbar.visible"
      location="top right"
      :timeout="snackbar.timeout"
      class="app-snackbar"
      @update:model-value="onSnackbarUpdate"
    >
      <div class="d-flex align-center ga-3">
        <v-icon :icon="snackbar.icon" />
        <span>{{ snackbar.text }}</span>
      </div>

      <template #actions>
        <v-btn variant="text" @click="closeNotify">
          Cerrar
        </v-btn>
      </template>
    </v-snackbar>

    <v-dialog
      :model-value="confirm.visible"
      max-width="480"
      persistent
      class="app-confirm-dialog"
      @update:model-value="onConfirmUpdate"
    >
      <v-card class="confirm-card">
        <v-card-text class="pa-6">
          <div class="d-flex ga-4 align-start">
            <v-avatar :color="confirm.color" variant="tonal" size="48">
              <v-icon :icon="confirmIcon" />
            </v-avatar>

            <div class="flex-grow-1">
              <div class="text-h6 font-weight-bold mb-2">
                {{ confirm.title }}
              </div>
              <p class="text-body-2 text-medium-emphasis mb-0">
                {{ confirm.message }}
              </p>
            </div>
          </div>
        </v-card-text>

        <v-card-actions class="px-6 pb-6 pt-0 justify-end ga-2">
          <v-btn variant="text" @click="resolveConfirm(false)">
            {{ confirm.cancelText }}
          </v-btn>
          <v-btn :color="confirm.color" @click="resolveConfirm(true)">
            {{ confirm.confirmText }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
const { snackbar, confirm, closeNotify, resolveConfirm } = useUiFeedback()

const confirmIcon = computed(() => {
  if (confirm.value.color === 'error') return 'mdi-alert-circle-outline'
  if (confirm.value.color === 'warning') return 'mdi-alert-outline'
  return 'mdi-help-circle-outline'
})

function onSnackbarUpdate(value: boolean) {
  if (!value) closeNotify()
}

function onConfirmUpdate(value: boolean) {
  if (!value) resolveConfirm(false)
}
</script>
