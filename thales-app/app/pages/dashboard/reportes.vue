<template>
  <v-container class="py-8">
    <div class="mb-6">
      <h1 class="text-h5 font-weight-bold">{{ actual.titulo }}</h1>
      <p class="text-body-2 text-grey-darken-1 mb-0">
        {{ actual.descripcion }}
      </p>
    </div>

    <ReportesResumenModulo v-if="moduloActivo === 'resumen'" />
    <IndicadoresModulo v-else-if="moduloActivo === 'indicadores'" />
    <EventosAuditoriaModulo v-else-if="moduloActivo === 'auditoria'" />
    <ReportesGeneradosModulo v-else-if="moduloActivo === 'generados'" />

    <v-alert v-else type="warning" variant="tonal">
      El submódulo solicitado no existe.
    </v-alert>
  </v-container>
</template>

<script setup lang="ts">
import ReportesResumenModulo from '~/components/reportes/submodulos/ReportesResumenModulo.vue'
import IndicadoresModulo from '~/components/reportes/submodulos/IndicadoresModulo.vue'
import EventosAuditoriaModulo from '~/components/reportes/submodulos/EventosAuditoriaModulo.vue'
import ReportesGeneradosModulo from '~/components/reportes/submodulos/ReportesGeneradosModulo.vue'
definePageMeta({
  middleware: 'auth',
})

const route = useRoute()

const modulos: Record<string, { titulo: string; descripcion: string }> = {
  resumen: {
    titulo: 'Reportes y estadísticas',
    descripcion: 'Vista general de indicadores, auditoría y reportes generados.',
  },
  indicadores: {
    titulo: 'Indicadores',
    descripcion: 'Disponibilidad, rendimiento, uso y métricas de negocio.',
  },
  auditoria: {
    titulo: 'Auditoría',
    descripcion: 'Trazabilidad de acciones realizadas por los microservicios.',
  },
  generados: {
    titulo: 'Reportes generados',
    descripcion: 'Solicitudes y estado de generación de archivos PDF, Excel o CSV.',
  },
}

const moduloActivo = computed(() =>
  String(route.query.modulo || 'resumen')
)

const actual = computed(() =>
  modulos[moduloActivo.value] || {
    titulo: 'Reportes',
    descripcion: 'Submódulo no encontrado.',
  }
)
</script>
