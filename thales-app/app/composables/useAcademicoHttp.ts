import { useAuthStore } from '~/stores/auth'


export function useAcademicoHttp() {

  const config = useRuntimeConfig()
  const authStore = useAuthStore()

  const { $apiFetch } = useNuxtApp()


  const academicoApi = String(
    config.public.academicoApiBase || ''
  ).replace(/\/$/, '')


  const matriculaApi = String(
    config.public.matriculaApiBase || ''
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
        'El módulo Académico requiere una sesión real.'
      )

    }

  }



  async function request(
    base:string,
    path:string,
    options:any = {}
  ) {

    validarSesion()


    if (!base) {

      throw new Error(
        'No se encuentra configurada la URL del microservicio.'
      )

    }


    try {

      return await $apiFetch(
        `${base}${path}`,
        {
          ...options
        }
      )


    } catch(error:any) {

      throw error

    }

  }



  const academico = (
    path:string,
    options:any = {}
  ) => {

    if (!academicoApi) {

      throw new Error(
        'Falta configurar NUXT_PUBLIC_ACADEMICO_API_BASE.'
      )

    }


    return request(
      academicoApi,
      path,
      options
    )

  }



  const matricula = (
    path:string,
    options:any = {}
  ) => {

    if (!matriculaApi) {

      throw new Error(
        'Falta configurar NUXT_PUBLIC_MATRICULA_API_BASE.'
      )

    }


    return request(
      matriculaApi,
      path,
      options
    )

  }



  return {

    academicoApi,

    matriculaApi,

    academico,

    matricula

  }

}