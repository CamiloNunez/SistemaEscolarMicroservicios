<template>
  <v-card class="main-card pa-6">
    <div class="d-flex justify-space-between align-start flex-wrap ga-4 mb-5">
      <div class="d-flex align-center ga-3">
        <v-avatar color="primary" variant="tonal">
          <v-icon>{{ icon }}</v-icon>
        </v-avatar>

        <div>
          <h2 class="text-h6 font-weight-bold mb-0">{{ titulo }}</h2>
          <p class="text-body-2 text-grey-darken-1 mb-0">
            {{ descripcion }}
          </p>
        </div>
      </div>

      <v-chip color="primary" variant="tonal" size="small">
        {{ endpoint }}
      </v-chip>
    </div>

    <v-alert type="info" variant="tonal" class="mb-5">
      Vista base creada para que el módulo ya exista en el menú y en la ruta.
      El CRUD completo se puede desarrollar después, sin mezclarlo con Matrículas.
    </v-alert>

    <v-row>
      <v-col cols="12" md="4">
        <v-card variant="outlined" class="pa-4 h-100">
          <p class="font-weight-bold mb-2">Acciones previstas</p>
          <div class="d-flex flex-wrap ga-2">
            <v-chip v-for="accion in acciones" :key="accion" size="small" variant="tonal">
              {{ accion }}
            </v-chip>
          </div>
        </v-card>
      </v-col>

      <v-col cols="12" md="8">
        <v-card variant="outlined" class="pa-4 h-100">
          <p class="font-weight-bold mb-2">Campos principales</p>
          <v-row dense>
            <v-col v-for="campo in campos" :key="campo" cols="12" sm="6" md="4">
              <v-chip size="small" variant="outlined" class="w-100 justify-center">
                {{ campo }}
              </v-chip>
            </v-col>
          </v-row>
        </v-card>
      </v-col>
    </v-row>

    <v-card variant="outlined" class="pa-4 mt-5">
      <div class="d-flex justify-space-between align-center flex-wrap ga-3">
        <div>
          <p class="font-weight-bold mb-1">Siguiente paso</p>
          <p class="text-body-2 text-grey-darken-1 mb-0">
            Reemplazar esta base por tabla, formulario, validaciones y llamadas fetch al endpoint indicado.
          </p>
        </div>

        <v-btn color="primary" variant="tonal" disabled>
          <v-icon start>mdi-plus</v-icon>
          Nuevo registro
        </v-btn>
      </div>
    </v-card>
  </v-card>
</template>

<script setup lang="ts">
defineProps<{
  titulo: string
  descripcion: string
  icon: string
  endpoint: string
  campos: string[]
  acciones?: string[]
}>()
</script>
