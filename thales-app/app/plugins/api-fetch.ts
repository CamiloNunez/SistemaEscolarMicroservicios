export default defineNuxtPlugin(() => {

  const { $pinia } = useNuxtApp()

  const apiFetch = $fetch.create({

    onRequest({ options }) {

      const authStore = useAuthStore($pinia)

      authStore.restoreSession()


      const headers = new Headers()


      if (authStore.token) {

        headers.set(
          'Authorization',
          `Bearer ${authStore.token}`
        )

      }


      headers.set(
        'Content-Type',
        'application/json'
      )


      options.headers = headers

    },


    onRequestError({ error }) {

      const mensaje = String(
        error?.message || error
      )


      if (
        mensaje.includes('NetworkError') ||
        mensaje.includes('Failed to fetch') ||
        mensaje.includes('fetch')
      ) {

        throw new Error(
          'El servicio no se encuentra disponible. Comuníquese con su área de tecnología.'
        )

      }


      throw error

    },


    onResponseError({ response }) {

      const data:any = response._data


      throw new Error(
        data?.mensaje ||
        data?.message ||
        data?.error ||
        `Error HTTP ${response.status}`
      )

    }

  })


  return {

    provide: {

      apiFetch

    }

  }

})