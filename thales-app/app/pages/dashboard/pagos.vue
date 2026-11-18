<template>
  <v-container class="py-8">
    <div class="mb-6">
      <h1 class="text-h5 font-weight-bold">
        {{ actual.titulo }}
      </h1>

      <p class="text-body-2 text-grey-darken-1 mb-0">
        {{ actual.descripcion }}
      </p>
    </div>

    <DeudasModulo v-if="moduloActivo === 'deudas'" />

    <PagosConsultaModulo
      v-else-if="moduloActivo === 'pagos'"
    />

    <ComprobantesModulo
      v-else-if="moduloActivo === 'comprobantes'"
    />

    <IntentosPagoModulo
      v-else-if="moduloActivo === 'intentos'"
    />

    <PasarelasPagoModulo
      v-else-if="moduloActivo === 'pasarelas'"
    />

    <v-alert
      v-else
      type="warning"
      variant="tonal"
    >
      El submódulo solicitado no existe.
    </v-alert>
  </v-container>
</template>

<script setup lang="ts">
import DeudasModulo from '~/components/pagos/submodulos/DeudasModulo.vue'
import PagosConsultaModulo from '~/components/pagos/submodulos/PagosConsultaModulo.vue'
import ComprobantesModulo from '~/components/pagos/submodulos/ComprobantesModulo.vue'
import IntentosPagoModulo from '~/components/pagos/submodulos/IntentosPagoModulo.vue'
import PasarelasPagoModulo from '~/components/pagos/submodulos/PasarelasPagoModulo.vue'
definePageMeta({
  middleware: 'auth',
})

const route = useRoute()

const modulos: Record<string, { titulo: string; descripcion: string }> = {
  deudas: {
    titulo: 'Deudas',
    descripcion:
      'Consulta y generación de obligaciones por matrícula o pensión.',
  },

  pagos: {
    titulo: 'Pagos',
    descripcion:
      'Consulta de pagos realizados por los estudiantes.',
  },

  comprobantes: {
    titulo: 'Comprobantes',
    descripcion:
      'Consulta de comprobantes asociados a pagos.',
  },

  intentos: {
    titulo: 'Intentos de pago',
    descripcion:
      'Historial de operaciones realizadas mediante pasarelas.',
  },

  pasarelas: {
    titulo: 'Pasarelas de pago',
    descripcion:
      'Consulta de proveedores y configuración de pasarelas.',
  },
}

const moduloActivo = computed(() =>
  String(route.query.modulo || 'deudas')
)

const actual = computed(() =>
  modulos[moduloActivo.value] || {
    titulo: 'Pagos',
    descripcion: 'Submódulo no encontrado.',
  }
)
</script>
