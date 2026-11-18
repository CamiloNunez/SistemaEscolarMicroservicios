type SnackbarColor = 'primary' | 'success' | 'warning' | 'error' | 'info'

interface SnackbarState {
  visible: boolean
  text: string
  color: SnackbarColor
  icon: string
  timeout: number
}

interface ConfirmState {
  visible: boolean
  title: string
  message: string
  confirmText: string
  cancelText: string
  color: SnackbarColor
  resolver: ((value: boolean) => void) | null
}

const snackbarDefaults: SnackbarState = {
  visible: false,
  text: '',
  color: 'primary',
  icon: 'mdi-information-outline',
  timeout: 3400,
}

const confirmDefaults: ConfirmState = {
  visible: false,
  title: 'Confirmar acción',
  message: '',
  confirmText: 'Confirmar',
  cancelText: 'Cancelar',
  color: 'primary',
  resolver: null,
}

function useSnackbarState() {
  return useState<SnackbarState>('app-ui-snackbar', () => ({ ...snackbarDefaults }))
}

function useConfirmState() {
  return useState<ConfirmState>('app-ui-confirm', () => ({ ...confirmDefaults }))
}

function defaultIcon(color: SnackbarColor) {
  switch (color) {
    case 'success':
      return 'mdi-check-circle-outline'
    case 'warning':
      return 'mdi-alert-outline'
    case 'error':
      return 'mdi-alert-circle-outline'
    case 'info':
      return 'mdi-information-outline'
    default:
      return 'mdi-bell-outline'
  }
}

export function useUiFeedbackState() {
  const snackbar = useSnackbarState()
  const confirm = useConfirmState()

  return {
    snackbar,
    confirm,
  }
}

export function uiNotify(
  text: string,
  options: Partial<Omit<SnackbarState, 'visible' | 'text'>> = {}
) {
  const snackbar = useSnackbarState()
  const color = options.color || 'primary'

  snackbar.value = {
    ...snackbarDefaults,
    ...options,
    visible: true,
    text,
    color,
    icon: options.icon || defaultIcon(color),
  }
}

export function closeUiNotify() {
  const snackbar = useSnackbarState()
  snackbar.value.visible = false
}

export function uiConfirm(
  message: string,
  options: Partial<Omit<ConfirmState, 'visible' | 'message' | 'resolver'>> = {}
) {
  const confirm = useConfirmState()
  const looksDestructive = /eliminar|anular|desactivar|no se puede deshacer|definitivamente/i.test(message)
  const color = options.color || (looksDestructive ? 'error' : 'primary')

  if (confirm.value.resolver) {
    confirm.value.resolver(false)
  }

  return new Promise<boolean>((resolve) => {
    confirm.value = {
      ...confirmDefaults,
      ...options,
      visible: true,
      message,
      color,
      title: options.title || (looksDestructive ? 'Confirmar acción' : 'Confirmación requerida'),
      confirmText: options.confirmText || (looksDestructive ? 'Sí, continuar' : 'Aceptar'),
      cancelText: options.cancelText || 'Cancelar',
      resolver: resolve,
    }
  })
}

export function resolveUiConfirm(value: boolean) {
  const confirm = useConfirmState()
  const resolver = confirm.value.resolver

  confirm.value = {
    ...confirmDefaults,
  }

  resolver?.(value)
}

export function useUiFeedback() {
  return {
    notify: uiNotify,
    confirm: uiConfirm,
    closeNotify: closeUiNotify,
    resolveConfirm: resolveUiConfirm,
    ...useUiFeedbackState(),
  }
}
