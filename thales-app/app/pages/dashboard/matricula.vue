<template>
  <v-container class="py-8">
    <div class="mb-6">
      <h1 class="text-h5 font-weight-bold">{{ tituloActual }}</h1>
      <p class="text-body-2 text-grey-darken-1 mb-0">
        {{ descripcionActual }}
      </p>
    </div>

    <MatriculasCrud v-if="moduloActivo === 'matriculas'" />
    <EstudiantesModulo v-else-if="moduloActivo === 'estudiantes'" />
    <ApoderadosModulo v-else-if="moduloActivo === 'apoderados'" />
    <DetallesMatriculaModulo v-else-if="moduloActivo === 'detalles-matricula'" />
    <DocumentosModulo
      v-else-if="moduloActivo === 'documentos-matricula' || moduloActivo === 'documentos'"
    />
    <ProgramasAcademicosModulo v-else-if="moduloActivo === 'programas-academicos'" />
    <GradosModulo v-else-if="moduloActivo === 'grados'" />
    <SeccionesModulo v-else-if="moduloActivo === 'secciones'" />
    <PeriodosAcademicosModulo v-else-if="moduloActivo === 'periodos-academicos'" />
    <CursosModulo v-else-if="moduloActivo === 'cursos'" />
    <PlanesEstudioModulo v-else-if="moduloActivo === 'planes-estudio'" />
    <VacantesModulo v-else-if="moduloActivo === 'vacantes'" />
    <CatalogosAcademicosModulo v-else-if="moduloActivo === 'catalogos'" />

    <v-alert v-else type="warning" variant="tonal">
      El submódulo solicitado no existe.
    </v-alert>
  </v-container>
</template>

<script setup lang="ts">
import MatriculasCrud from '~/components/matricula/submodulos/MatriculasCrud.vue'
import EstudiantesModulo from '~/components/matricula/submodulos/EstudiantesModulo.vue'
import ApoderadosModulo from '~/components/matricula/submodulos/ApoderadosModulo.vue'
import DetallesMatriculaModulo from '~/components/matricula/submodulos/DetallesMatriculaModulo.vue'
import DocumentosModulo from '~/components/matricula/submodulos/DocumentosModulo.vue'
import ProgramasAcademicosModulo from '~/components/matricula/submodulos/ProgramasAcademicosModulo.vue'
import GradosModulo from '~/components/matricula/submodulos/GradosModulo.vue'
import SeccionesModulo from '~/components/matricula/submodulos/SeccionesModulo.vue'
import PeriodosAcademicosModulo from '~/components/matricula/submodulos/PeriodosAcademicosModulo.vue'
import CursosModulo from '~/components/matricula/submodulos/CursosModulo.vue'
import PlanesEstudioModulo from '~/components/matricula/submodulos/PlanesEstudioModulo.vue'
import VacantesModulo from '~/components/matricula/submodulos/VacantesModulo.vue'
import CatalogosAcademicosModulo from '~/components/matricula/submodulos/CatalogosAcademicosModulo.vue'
definePageMeta({
  middleware: 'auth',
})

const route = useRoute()

const modulos: Record<string, { titulo: string; descripcion: string }> = {
  matriculas: {
    titulo: 'Matrículas',
    descripcion: 'Registro, edición, consulta y eliminación de matrículas.',
  },
  estudiantes: {
    titulo: 'Estudiantes',
    descripcion: 'Gestión de información de estudiantes.',
  },
  apoderados: {
    titulo: 'Apoderados',
    descripcion: 'Registro de padres, madres o tutores responsables.',
  },
  'detalles-matricula': {
    titulo: 'Detalle de matrícula',
    descripcion: 'Cursos inscritos y estado del detalle académico de la matrícula.',
  },
  'documentos-matricula': {
    titulo: 'Documentos de matrícula',
    descripcion: 'Requisitos y documentos asociados al proceso de matrícula.',
  },
  documentos: {
    titulo: 'Documentos de matrícula',
    descripcion: 'Requisitos y documentos asociados al proceso de matrícula.',
  },
  'programas-academicos': {
    titulo: 'Programas académicos',
    descripcion: 'Configuración de niveles y programas académicos.',
  },
  grados: {
    titulo: 'Grados',
    descripcion: 'Gestión de grados por programa académico.',
  },
  secciones: {
    titulo: 'Secciones',
    descripcion: 'Gestión de secciones, turnos, capacidad y aula.',
  },
  'periodos-academicos': {
    titulo: 'Periodos académicos',
    descripcion: 'Configuración de periodos y ventanas de matrícula.',
  },
  cursos: {
    titulo: 'Cursos',
    descripcion: 'Catálogo de cursos y áreas curriculares.',
  },
  'planes-estudio': {
    titulo: 'Planes de estudio',
    descripcion: 'Cursos por programa, grado y periodo académico.',
  },
  vacantes: {
    titulo: 'Vacantes',
    descripcion: 'Disponibilidad por sección y periodo académico.',
  },
  catalogos: {
    titulo: 'Catálogos académicos',
    descripcion: 'Resumen de catálogos académicos del módulo de matrícula.',
  },
}

const moduloActivo = computed(() =>
  String(route.query.modulo || 'matriculas')
)

const tituloActual = computed(() =>
  modulos[moduloActivo.value]?.titulo || 'Matrícula'
)

const descripcionActual = computed(() =>
  modulos[moduloActivo.value]?.descripcion || 'Submódulo no encontrado.'
)
</script>
