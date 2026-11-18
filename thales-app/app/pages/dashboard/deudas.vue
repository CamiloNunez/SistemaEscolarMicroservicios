<template>
  <v-container>
    <v-card>
      <v-card-title>
        Mis deudas
      </v-card-title>

      <v-card-text>
        <v-alert type="info">
          Módulo de pagos con Stripe en pruebas.
        </v-alert>

        <v-list>
          <v-list-item
            v-for="deuda in deudas"
            :key="deuda.id"
            :title="deuda.concepto"
            :subtitle="`Monto: S/ ${deuda.monto}`"
          >
            <template #append>
              <v-btn
                color="primary"
                @click="pagar(deuda.id)"
              >
                Pagar
              </v-btn>
            </template>
          </v-list-item>
        </v-list>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { usePagosHttp } from '~/composables/usePagosHttp'
const { notify } = useUiFeedback()

definePageMeta({
  middleware: ['auth']
})

const { crearIntentoStripe } = usePagosHttp()

const deudas = ref([
  {
    id: 'demo-deuda',
    concepto: 'Mensualidad pendiente',
    monto: 350
  }
])

async function pagar(id:string){

  const respuesta:any = await crearIntentoStripe(id)

  console.log('Stripe PaymentIntent', respuesta)

  notify(
    'Intento Stripe creado. ClientSecret recibido.'
  )
}
</script>