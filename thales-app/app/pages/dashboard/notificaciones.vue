<template>
  <v-container class="py-8">
    <div class="mb-6">
      <h1 class="text-h5 font-weight-bold">{{ actual.titulo }}</h1>
      <p class="text-body-2 text-grey-darken-1 mb-0">{{ actual.descripcion }}</p>
    </div>

    <NotificacionesModulo v-if="moduloActivo==='notificaciones'" />
    <PlantillasNotificacionModulo v-else-if="moduloActivo==='plantillas'" />
    <DestinatariosNotificacionModulo v-else-if="moduloActivo==='destinatarios'" />
    <PreferenciasNotificacionModulo v-else-if="moduloActivo==='preferencias'" />
    <v-alert v-else type="warning" variant="tonal">El submódulo solicitado no existe.</v-alert>
  </v-container>
</template>

<script setup lang="ts">
import NotificacionesModulo from '~/components/notificaciones/submodulos/NotificacionesModulo.vue'
import PlantillasNotificacionModulo from '~/components/notificaciones/submodulos/PlantillasNotificacionModulo.vue'
import DestinatariosNotificacionModulo from '~/components/notificaciones/submodulos/DestinatariosNotificacionModulo.vue'
import PreferenciasNotificacionModulo from '~/components/notificaciones/submodulos/PreferenciasNotificacionModulo.vue'
definePageMeta({ middleware: 'auth' })

const route=useRoute()

const modulos:any={
  notificaciones:{titulo:'Notificaciones',descripcion:'Registro, consulta y seguimiento de notificaciones.'},
  plantillas:{titulo:'Plantillas',descripcion:'Configuración de plantillas de notificación.'},
  destinatarios:{titulo:'Destinatarios',descripcion:'Destinos y estado de entrega.'},
  preferencias:{titulo:'Preferencias',descripcion:'Preferencias de notificación por usuario.'},
}

const moduloActivo=computed(()=>String(route.query.modulo||'notificaciones'))
const actual=computed(()=>modulos[moduloActivo.value]||{titulo:'Notificaciones',descripcion:'Submódulo no encontrado.'})
</script>
