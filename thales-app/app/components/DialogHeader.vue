<template>
  <header class="dialog-header">
    <div class="dialog-header__text">
      <h2 class="dialog-header__title">
        <slot name="title">{{ title }}</slot>
      </h2>
      <p v-if="subtitle || $slots.subtitle" class="dialog-header__subtitle">
        <slot name="subtitle">{{ subtitle }}</slot>
      </p>
    </div>

    <v-btn
      v-if="closable"
      icon="mdi-close"
      variant="text"
      size="small"
      density="comfortable"
      class="dialog-header__close"
      aria-label="Cerrar"
      @click="emit('close')"
    />
  </header>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string
    subtitle?: string
    closable?: boolean
  }>(),
  {
    title: '',
    subtitle: '',
    closable: true,
  }
)

const emit = defineEmits<{ close: [] }>()
</script>

<style scoped>
/* Cabecera compacta de diálogos: título + descripción breve + cerrar,
   separada del contenido por una línea sutil. */
.dialog-header {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding-bottom: var(--space-3);
  margin-bottom: var(--space-4);
  border-bottom: 1px solid var(--color-border-soft);
}

.dialog-header__text {
  flex: 1;
  min-width: 0;
}

.dialog-header__title {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  line-height: 1.4;
  color: var(--color-title);
}

.dialog-header__subtitle {
  margin: 2px 0 0;
  font-size: 0.8125rem;
  line-height: 1.4;
  color: var(--color-muted) !important;
}

/* Alinea ópticamente la X con la primera línea del título y la esquina */
.dialog-header__close {
  flex-shrink: 0;
  margin-top: -4px;
  margin-right: -8px;
  color: var(--color-muted);
}

.dialog-header__close:hover {
  color: var(--color-title);
}
</style>
