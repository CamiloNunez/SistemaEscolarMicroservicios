const NETWORK_ERROR_MSG =
  'El servicio no se encuentra disponible. Comuníquese con su área de tecnología.'


export function traducirErrorRed(error: unknown): Error {

  if (error instanceof Error) {

    const msg = error.message.toLowerCase()

    if (
      error.name === 'TypeError' ||
      msg.includes('failed to fetch') ||
      msg.includes('networkerror') ||
      msg.includes('fetch failed') ||
      msg.includes('load failed') ||
      msg.includes('network request failed') ||
      msg.includes('cors')
    ) {
      return new Error(NETWORK_ERROR_MSG)
    }

    return error
  }


  const texto = String(error || '').toLowerCase()


  if (
    texto.includes('failed to fetch') ||
    texto.includes('networkerror') ||
    texto.includes('cors')
  ) {
    return new Error(NETWORK_ERROR_MSG)
  }


  return new Error(
    String(error || 'Error desconocido')
  )
}