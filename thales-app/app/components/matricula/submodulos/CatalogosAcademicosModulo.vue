<template>
  <v-card class="main-card pa-6">
    <div class="d-flex align-center ga-3 mb-4">
      <v-icon size="30" color="primary">mdi-book-cog-outline</v-icon>
      <div>
        <h2 class="text-h6 font-weight-bold mb-0">Catálogos académicos</h2>
        <p class="text-body-2 text-grey-darken-1 mb-0">
          Vista resumen. Cada catálogo también tiene su propia opción anidada en el menú de Matrícula.
        </p>
      </div>
    </div>

    <v-row>
      <v-col v-for="item in catalogos" :key="item.modulo" cols="12" md="6" lg="4">
        <v-card variant="outlined" class="pa-4 h-100">
          <div class="d-flex align-start ga-3">
            <v-avatar color="primary" variant="tonal" size="38">
              <v-icon>{{ item.icon }}</v-icon>
            </v-avatar>

            <div class="flex-grow-1">
              <p class="font-weight-bold mb-1">{{ item.title }}</p>
              <p class="text-caption text-grey-darken-1 mb-3">{{ item.desc }}</p>
              <v-btn
                size="small"
                variant="tonal"
                color="primary"
                :to="{ path: '/dashboard/matricula', query: { modulo: item.modulo } }"
              >
                Abrir
              </v-btn>
            </div>
          </div>
        </v-card>
      </v-col>
    </v-row>
  </v-card>
</template>

<script setup lang="ts">
const catalogos = [
  { modulo: 'programas-academicos', title: 'Programas académicos', desc: 'Inicial, primaria, secundaria, duración y nivel educativo.', icon: 'mdi-school-outline' },
  { modulo: 'grados', title: 'Grados', desc: 'Configuración de grados por programa.', icon: 'mdi-numeric' },
  { modulo: 'secciones', title: 'Secciones', desc: 'Aulas, turnos, capacidad y tutor.', icon: 'mdi-google-classroom' },
  { modulo: 'periodos-academicos', title: 'Periodos académicos', desc: 'Año escolar y ventanas de matrícula.', icon: 'mdi-calendar-range' },
  { modulo: 'cursos', title: 'Cursos', desc: 'Cursos, áreas y horas semanales.', icon: 'mdi-book-open-variant' },
  { modulo: 'planes-estudio', title: 'Planes de estudio', desc: 'Cursos por grado, programa y periodo.', icon: 'mdi-book-cog-outline' },
  { modulo: 'vacantes', title: 'Vacantes', desc: 'Disponibilidad por sección y periodo.', icon: 'mdi-counter' },
]
</script>
