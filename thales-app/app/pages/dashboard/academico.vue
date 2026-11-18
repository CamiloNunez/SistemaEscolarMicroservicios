<template>
  <v-container class="py-8">
    <div class="mb-6"><h1 class="text-h5 font-weight-bold">{{ actual.titulo }}</h1><p class="text-body-2 text-grey-darken-1 mb-0">{{ actual.descripcion }}</p></div>
    <DocentesModulo v-if="moduloActivo==='docentes'" />
    <AsignacionesDocenteModulo v-else-if="moduloActivo==='asignaciones'" />
    <AsistenciasModulo v-else-if="moduloActivo==='asistencias'" />
    <NotasModulo v-else-if="moduloActivo==='notas'" />
    <PrerequisitosModulo v-else-if="moduloActivo==='prerequisitos'" />
    <v-alert v-else type="warning" variant="tonal">El submódulo solicitado no existe.</v-alert>
  </v-container>
</template>
<script setup lang="ts">
import DocentesModulo from '~/components/academico/submodulos/DocentesModulo.vue'
import AsignacionesDocenteModulo from '~/components/academico/submodulos/AsignacionesDocenteModulo.vue'
import AsistenciasModulo from '~/components/academico/submodulos/AsistenciasModulo.vue'
import NotasModulo from '~/components/academico/submodulos/NotasModulo.vue'
import PrerequisitosModulo from '~/components/academico/submodulos/PrerequisitosModulo.vue'
definePageMeta({middleware:'auth'})
const route=useRoute(); const modulos:any={docentes:{titulo:'Docentes',descripcion:'Gestión de docentes del plantel.'},asignaciones:{titulo:'Asignaciones docentes',descripcion:'Asignación de docentes a cursos, secciones y periodos.'},asistencias:{titulo:'Asistencias',descripcion:'Registro y mantenimiento de asistencia de estudiantes.'},notas:{titulo:'Notas',descripcion:'Registro, modificación, anulación y eliminación de calificaciones.'},prerequisitos:{titulo:'Prerrequisitos',descripcion:'Configuración de cursos requeridos o recomendados.'}};const moduloActivo=computed(()=>String(route.query.modulo||'docentes'));const actual=computed(()=>modulos[moduloActivo.value]||{titulo:'Académico',descripcion:'Submódulo no encontrado.'})
</script>
