import { useAuthStore } from '~/stores/auth'


export function useReportesHttp() {

  const authStore = useAuthStore()
  const config = useRuntimeConfig()

  const { $apiFetch } = useNuxtApp()


  const reportesApi = String(
    config.public.reportesApiBase || ''
  ).replace(/\/$/, '')



  function validarSesion() {

    authStore.restoreSession()


    if (!authStore.token) {

      throw new Error(
        'No hay token de sesión. Inicia sesión nuevamente.'
      )

    }


    if (authStore.token.startsWith('demo-token-')) {

      throw new Error(
        'El módulo de Reportes requiere una sesión real.'
      )

    }


    if (!reportesApi) {

      throw new Error(
        'Falta configurar NUXT_PUBLIC_REPORTES_API_BASE.'
      )

    }

  }



  async function reportes(
    path:string,
    options:any = {}
  ) {

    validarSesion()


    try {

      return await $apiFetch(
        `${reportesApi}${path}`,
        {
          ...options
        }
      )


    } catch(error:any) {

      throw error

    }

  }



  return {

    reportesApi,

    reportes

  }

}