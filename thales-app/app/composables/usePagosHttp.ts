import { useAuthStore } from '~/stores/auth'


export function usePagosHttp() {

  const authStore = useAuthStore()
  const config = useRuntimeConfig()

  const { $apiFetch } = useNuxtApp()


  const pagosApi = String(
    config.public.pagosApiBase || ''
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
        'El módulo de Pagos requiere una sesión real.'
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



  const pagos = (
    path:string,
    options:any = {}
  ) => {

    if (!pagosApi) {

      throw new Error(
        'Falta configurar NUXT_PUBLIC_PAGOS_API_BASE.'
      )

    }


    return request(
      pagosApi,
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


  const crearIntentoStripe = (deudaId:string) => {

    return pagos(
      '/api/pagos/stripe/crear-intento',
      {
        method: 'POST',
        body: {
          deudaId
        }
      }
    )

  }



  return {

    pagosApi,

    matriculaApi,

    pagos,

    matricula,

    crearIntentoStripe

  }

}