import { useAuthStore } from '~/stores/auth'


export function useNotificacionesHttp() {

  const config = useRuntimeConfig()
  const authStore = useAuthStore()

  const { $apiFetch } = useNuxtApp()


  const notificacionesApi = String(
    config.public.notificacionesApiBase || ''
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
        'El módulo de Notificaciones requiere una sesión real.'
      )

    }


    if (!notificacionesApi) {

      throw new Error(
        'Falta configurar NUXT_PUBLIC_NOTIFICACIONES_API_BASE.'
      )

    }

  }



  async function request(
    path:string,
    options:any = {}
  ) {

    validarSesion()


    try {

      return await $apiFetch(
        `${notificacionesApi}${path}`,
        {
          ...options
        }
      )


    } catch(error:any) {

      throw error

    }

  }



  const notificaciones = (
    path:string,
    options:any = {}
  ) => {

    return request(
      path,
      options
    )

  }



  return {

    notificacionesApi,

    notificaciones

  }

}