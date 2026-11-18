import vuetify, { transformAssetUrls } from 'vite-plugin-vuetify'
import { generatePwaAssets } from './scripts/pwa-assets.mjs'

export default defineNuxtConfig({
  hooks: {
    'nitro:build:public-assets': async (nitro) => {
      await generatePwaAssets(nitro.options.output.publicDir)
    },
  },
  // The authenticated dashboard renders on the client; its HTML shell contains no user data.
  routeRules: { '/dashboard/**': { ssr: false } },
  devtools: {
    enabled: true,
  },

  build: {
    transpile: ['vuetify'],
  },

  modules: [
    '@pinia/nuxt',

    (_options, nuxt) => {
      nuxt.hooks.hook('vite:extendConfig', (config) => {
        config.plugins = config.plugins || []

        config.plugins.push(
          vuetify({
            autoImport: true,
          })
        )
      })
    },
  ],

  vite: {
    vue: {
      template: {
        transformAssetUrls,
      },
    },
  },

  css: [
    '~/assets/main.css',
    '@mdi/font/css/materialdesignicons.css',
  ],

  runtimeConfig: {
    public: {
      loginApiBase: process.env.NUXT_PUBLIC_LOGIN_API_BASE || 'http://localhost:8080',
      matriculaApiBase: process.env.NUXT_PUBLIC_MATRICULA_API_BASE || 'http://localhost:8082',
      academicoApiBase: process.env.NUXT_PUBLIC_ACADEMICO_API_BASE || 'http://localhost:8083',
      pagosApiBase: process.env.NUXT_PUBLIC_PAGOS_API_BASE || 'http://localhost:8084',
      reportesApiBase: process.env.NUXT_PUBLIC_REPORTES_API_BASE || 'http://localhost:8085',
      notificacionesApiBase: process.env.NUXT_PUBLIC_NOTIFICACIONES_API_BASE || 'http://localhost:8086',
    },
  },

  app: {
    head: {
      title: 'IEP Thales de Mileto',
      link: [{ rel: 'manifest', href: '/manifest.webmanifest' }],

      meta: [
        { name: 'theme-color', content: '#1565c0' },
        {
          charset: 'utf-8',
        },
        {
          name: 'viewport',
          content: 'width=device-width, initial-scale=1',
        },
      ],
    },
  },
})
