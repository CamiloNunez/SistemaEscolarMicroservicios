<template>
  <v-card class="main-card pa-3">
    <p class="text-caption text-grey-darken-1 mb-3 px-2">
      Submódulos
    </p>

    <v-list density="compact" nav class="pa-0">
      <v-list-item
        v-for="item in items"
        :key="item.value"
        :active="modelValue === item.value"
        :prepend-icon="item.icon"
        :title="item.title"
        :subtitle="item.subtitle"
        rounded="lg"
        class="mb-1"
        color="primary"
        @click="$emit('update:modelValue', item.value)"
      />
    </v-list>
  </v-card>
</template>

<script setup lang="ts">
interface SubmoduloItem {
  value: string
  title: string
  subtitle: string
  icon: string
}

defineProps<{
  modelValue: string
  items: SubmoduloItem[]
}>()

defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>
