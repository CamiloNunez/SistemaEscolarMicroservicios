import '@mdi/font/css/materialdesignicons.css'
import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

export default defineNuxtPlugin((app) => {
  const vuetify = createVuetify({
    components,
    directives,
    icons: {
      defaultSet: 'mdi',
    },
    // Controles compactos para un panel administrativo denso y sobrio
    defaults: {
      VTextField: { density: 'compact', color: 'primary' },
      VSelect: { density: 'compact', color: 'primary' },
      VAutocomplete: { density: 'compact', color: 'primary' },
      VCombobox: { density: 'compact', color: 'primary' },
      VTextarea: { density: 'compact', color: 'primary' },
      VFileInput: { density: 'compact', color: 'primary' },
      VSwitch: { density: 'compact', color: 'primary' },
      VCheckbox: { density: 'compact', color: 'primary' },
      VDataTable: { density: 'compact' },
      VTable: { density: 'compact' },
      VList: { density: 'compact' },
      VAlert: { density: 'compact' },
      VChip: { size: 'small', label: true },
    },
    theme: {
      defaultTheme: 'thalesTheme',
      themes: {
        thalesTheme: {
          dark: false,
          colors: {
            primary:    '#5b0014',
            secondary:  '#d6a629',
            background: '#f7f3e8',
            surface:    '#ffffff',
            error:      '#B00020',
            success:    '#2E7D32',
          },
        },
      },
    },
  })

  app.vueApp.use(vuetify)
})