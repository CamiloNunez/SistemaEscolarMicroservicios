import { useAuthStore } from '~/stores/auth'

export const useMatriculaHttp = () => {

  const config = useRuntimeConfig()
  const authStore = useAuthStore()

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


    if (!matriculaApi) {
      throw new Error(
        'Falta configurar NUXT_PUBLIC_MATRICULA_API_BASE.'
      )
    }

  }



  const { $apiFetch } = useNuxtApp()



  async function request(
    endpoint: string,
    options: any = {}
  ) {

    validarSesion()


    try {

      return await $apiFetch(
        `${matriculaApi}${endpoint}`,
        {
          ...options
        }
      )


    } catch(error:any) {

      throw error

    }

  }



  const matricula = (
    endpoint:string,
    options:any = {}
  ) => {

    return request(endpoint, options)

  }



  const obtener = (
    endpoint:string
  ) => {

    return request(endpoint)

  }



  const crear = (
    endpoint:string,
    body:any
  ) => {

    return request(
      endpoint,
      {
        method:'POST',
        body
      }
    )

  }



  const actualizar = (
    endpoint:string,
    body:any
  ) => {

    return request(
      endpoint,
      {
        method:'PUT',
        body
      }
    )

  }



  const eliminar = (
    endpoint:string
  ) => {

    return request(
      endpoint,
      {
        method:'DELETE'
      }
    )

  }



  return {

    matriculaApi,

    matricula,

    obtener,

    crear,

    actualizar,

    eliminar

  }

}