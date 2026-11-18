<template>
  <v-container class="py-8">
    <div class="d-flex flex-wrap align-center justify-space-between ga-3 mb-6">
      <div class="d-flex align-center ga-3">
        <v-icon size="32" color="primary">mdi-account-group</v-icon>
        <div>
          <h1 class="text-h5 font-weight-bold" style="color:#5b0014">Usuarios</h1>
          <p class="text-caption text-grey-darken-1 mb-0">Administración de cuentas, roles y perfiles asociados.</p>
        </div>
      </div>
      <div class="d-flex ga-2">
        <v-btn variant="tonal" color="primary" :loading="loading" @click="cargarDatos">
          <v-icon start>mdi-refresh</v-icon>Actualizar
        </v-btn>
        <v-btn color="primary" @click="abrirNuevo">
          <v-icon start>mdi-account-plus</v-icon>Nuevo usuario
        </v-btn>
      </div>
    </div>

    <v-alert v-if="errorMsg" type="error" variant="tonal" closable class="mb-4" @click:close="errorMsg=''">
      {{ errorMsg }}
    </v-alert>
    <v-alert v-if="successMsg" type="success" variant="tonal" closable class="mb-4" @click:close="successMsg=''">
      {{ successMsg }}
    </v-alert>

    <v-card class="main-card" elevation="3">
      <v-card-text>
        <v-text-field v-model="busqueda" prepend-inner-icon="mdi-magnify" label="Buscar usuario" variant="outlined" density="compact" clearable class="mb-4" />

        <v-data-table :headers="headers" :items="usuariosFiltrados" :loading="loading" item-value="id" no-data-text="No hay usuarios registrados">
          <template #item.nombreCompleto="{ item }">
            <div class="py-2">
              <div class="font-weight-medium">{{ item.nombreCompleto || '-' }}</div>
              <div class="text-caption text-grey">@{{ item.username }}</div>
            </div>
          </template>

          <template #item.tipoUsuario="{ item }">
            <v-chip size="small" variant="tonal" color="primary">{{ item.tipoUsuario || 'SIN TIPO' }}</v-chip>
          </template>

          <template #item.roles="{ item }">
            <div class="d-flex flex-wrap ga-1 py-2">
              <v-chip v-for="rol in item.roles" :key="rol" size="x-small" variant="outlined">{{ nombreRol(rol) }}</v-chip>
              <span v-if="!item.roles?.length" class="text-caption text-grey">Sin roles</span>
            </div>
          </template>

          <template #item.activo="{ item }">
            <v-chip :color="item.activo === false ? 'error' : 'success'" size="small" variant="tonal">
              {{ item.activo === false ? 'Inactivo' : 'Activo' }}
            </v-chip>
          </template>

          <template #item.acciones="{ item }">
            <div class="d-flex ga-1 justify-end">
              <v-btn icon="mdi-pencil" size="small" variant="text" color="primary" @click="abrirEditar(item)" />
              <v-btn icon="mdi-delete" size="small" variant="text" color="error" @click="confirmarEliminar(item)" />
            </div>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>

    <v-dialog v-model="dialog" max-width="760" persistent>
      <v-card>
        <v-card-title class="d-flex align-center ga-2 pa-5">
          <v-icon color="primary">{{ editando ? 'mdi-account-edit' : 'mdi-account-plus' }}</v-icon>
          {{ editando ? 'Editar usuario' : 'Nuevo usuario' }}
        </v-card-title>
        <v-divider />
        <v-card-text class="pa-5">
          <v-form ref="formRef" @submit.prevent="guardar">
            <v-row>
              <v-col cols="12" md="6"><v-text-field v-model="form.nombreCompleto" label="Nombre completo *" variant="outlined" :rules="[required]" /></v-col>
              <v-col cols="12" md="6"><v-text-field v-model="form.username" label="Usuario *" variant="outlined" :rules="[required]" /></v-col>
              <v-col cols="12" md="6"><v-text-field v-model="form.email" label="Correo *" type="email" variant="outlined" :rules="[required]" /></v-col>
              <v-col cols="12" md="6"><v-text-field v-model="form.password" :label="editando ? 'Nueva contraseña (opcional)' : 'Contraseña *'" type="password" variant="outlined" :rules="editando ? [] : [required]" /></v-col>
              <v-col cols="12" md="6"><v-text-field v-model="form.cedula" label="DNI / Cédula" variant="outlined" /></v-col>
              <v-col cols="12" md="6"><v-text-field v-model="form.telefono" label="Teléfono" variant="outlined" /></v-col>
              <v-col cols="12" md="6">
                <v-select v-model="form.tipoUsuario" :items="tiposUsuario" label="Tipo de usuario" variant="outlined" clearable />
              </v-col>
              <v-col cols="12" md="6">
                <v-select v-model="form.roles" :items="rolesActivos" item-title="descripcionVisible" item-value="nombre" label="Roles *" variant="outlined" multiple chips closable-chips :rules="[rolesRequired]" />
              </v-col>
              <v-col cols="12" md="6"><v-text-field v-model="form.grado" label="Grado" variant="outlined" /></v-col>
              <v-col cols="12" md="6"><v-text-field v-model="form.seccion" label="Sección" variant="outlined" /></v-col>
              <v-col cols="12"><v-switch v-model="form.activo" color="success" label="Usuario activo" hide-details /></v-col>
            </v-row>
          </v-form>
        </v-card-text>
        <v-card-actions class="pa-5 pt-0">
          <v-spacer />
          <v-btn variant="text" @click="cerrarDialog">Cancelar</v-btn>
          <v-btn color="primary" :loading="saving" @click="guardar">{{ editando ? 'Guardar cambios' : 'Crear usuario' }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="deleteDialog" max-width="460">
      <v-card>
        <v-card-title class="pa-5">Eliminar usuario</v-card-title>
        <v-card-text>¿Eliminar a <strong>{{ seleccionado?.nombreCompleto }}</strong>? Esta acción no se puede deshacer.</v-card-text>
        <v-card-actions class="pa-5 pt-0">
          <v-spacer />
          <v-btn variant="text" @click="deleteDialog=false">Cancelar</v-btn>
          <v-btn color="error" :loading="deleting" @click="eliminar">Eliminar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { useAuthStore, type UserProfile } from '~/stores/auth'
definePageMeta({ middleware: 'auth' })

interface RolItem { id: string; nombre: string; descripcion?: string; permisos?: string[]; activo?: boolean; descripcionVisible?: string }
interface BackendUser {
  id: string; username: string; email: string; nombreCompleto: string; cedula?: string; telefono?: string;
  tipoUsuario?: string; roles: string[]; activo?: boolean; grado?: string | null; seccion?: string | null; perfil?: UserProfile | null
}

const authStore = useAuthStore()
const config = useRuntimeConfig()
const loading = ref(false), saving = ref(false), deleting = ref(false)
const errorMsg = ref(''), successMsg = ref(''), busqueda = ref('')
const usuarios = ref<BackendUser[]>([]), roles = ref<RolItem[]>([])
const dialog = ref(false), deleteDialog = ref(false), editando = ref(false)
const seleccionado = ref<BackendUser | null>(null)
const formRef = ref<any>(null)

const tiposUsuario = ['ESTUDIANTE','PROFESOR','ADMINISTRATIVO','PADRE','DIRECTOR','COORDINADOR']
const form = reactive({ username:'', email:'', password:'', nombreCompleto:'', cedula:'', telefono:'', tipoUsuario:null as string|null, roles:[] as string[], grado:'', seccion:'', activo:true })
const required = (v:any) => !!String(v ?? '').trim() || 'Campo obligatorio'
const rolesRequired = (v:any[]) => (Array.isArray(v) && v.length > 0) || 'Selecciona al menos un rol'

const headers = [
  { title:'Usuario', key:'nombreCompleto' }, { title:'Correo', key:'email' }, { title:'Tipo', key:'tipoUsuario' },
  { title:'Roles', key:'roles', sortable:false },{ title:'Estado', key:'activo' },
  { title:'Acciones', key:'acciones', sortable:false, align:'end' as const },
]
const rolesActivos = computed(() => roles.value.filter(r => r.activo !== false).map(r => ({...r, descripcionVisible: `${nombreRol(r.nombre)}${r.descripcion ? ' - '+r.descripcion : ''}`})))
const usuariosFiltrados = computed(() => {
  const q = busqueda.value.trim().toLowerCase(); if (!q) return usuarios.value
  return usuarios.value.filter(u => [u.nombreCompleto,u.username,u.email,u.tipoUsuario,...(u.roles||[])].filter(Boolean).join(' ').toLowerCase().includes(q))
})

function nombreRol(v:string){ return String(v||'').replace(/^ROLE_/,'') || 'ROL' }
function resetForm(){ Object.assign(form,{username:'',email:'',password:'',nombreCompleto:'',cedula:'',telefono:'',tipoUsuario:null,roles:[],grado:'',seccion:'',activo:true}) }
function abrirNuevo(){ editando.value=false; seleccionado.value=null; resetForm(); dialog.value=true }
function abrirEditar(u:BackendUser){
  editando.value=true; seleccionado.value=u
  Object.assign(form,{username:u.username,email:u.email,password:'',nombreCompleto:u.nombreCompleto,cedula:u.cedula||'',telefono:u.telefono||'',tipoUsuario:u.tipoUsuario||null,roles:[...(u.roles||[])],grado:u.grado||'',seccion:u.seccion||'',activo:u.activo!==false})
  dialog.value=true
}
function cerrarDialog(){ dialog.value=false; seleccionado.value=null; formRef.value?.resetValidation?.() }
function confirmarEliminar(u:BackendUser){ seleccionado.value=u; deleteDialog.value=true }

async function api(path:string, options:RequestInit={}){
  if (!authStore.token) throw new Error('No hay token de autenticación.')
  const base=String(config.public.loginApiBase).trim().replace(/\/+$/,'')
  const headers:any={Accept:'application/json',Authorization:`Bearer ${authStore.token}`,...(options.headers||{})}
  const response=await fetch(base+path,{...options,headers}); const data=await response.json().catch(()=>null)
  if(!response.ok) throw new Error(data?.mensaje||data?.message||`Error HTTP ${response.status}`)
  return data
}

async function cargarDatos(){
  loading.value=true; errorMsg.value=''
  try{
    const [listaUsuarios, listaRoles, perfiles]=await Promise.all([api('/api/auth/usuarios'),api('/api/auth/roles'),api('/api/auth/perfiles')])
    roles.value=Array.isArray(listaRoles)?listaRoles:[]
    const perfilesMap=new Map<string,UserProfile>((Array.isArray(perfiles)?perfiles:[]).map((p:UserProfile)=>[String(p.usuarioId),p]))
    usuarios.value=(Array.isArray(listaUsuarios)?listaUsuarios:[]).map((u:any)=>({...u,id:String(u.id||''),roles:Array.isArray(u.roles)?u.roles.map((r:any)=>typeof r==='string'?r:r?.nombre).filter(Boolean):[],perfil:perfilesMap.get(String(u.id))||null}))
  }catch(e:any){ errorMsg.value=e?.message||'No se pudieron cargar los datos.' }finally{ loading.value=false }
}

async function guardar(){
  const result=await formRef.value?.validate?.(); if(result && !result.valid) return
  saving.value=true; errorMsg.value=''; successMsg.value=''
  try{
    const body:any={username:form.username.trim(),email:form.email.trim(),nombreCompleto:form.nombreCompleto.trim(),cedula:form.cedula||null,telefono:form.telefono||null,tipoUsuario:form.tipoUsuario,roles:form.roles,grado:form.grado||null,seccion:form.seccion||null,activo:form.activo}
    if(form.password.trim()) body.password=form.password
    const path=editando.value?`/api/auth/usuarios/${encodeURIComponent(seleccionado.value!.id)}`:'/api/auth/registro'
    await api(path,{method:editando.value?'PUT':'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)})
    successMsg.value=editando.value?'Usuario actualizado correctamente.':'Usuario creado correctamente.'
    cerrarDialog(); await cargarDatos()
  }catch(e:any){ errorMsg.value=e?.message||'No se pudo guardar el usuario.' }finally{ saving.value=false }
}

async function eliminar(){
  if(!seleccionado.value) return; deleting.value=true; errorMsg.value=''; successMsg.value=''
  try{ await api(`/api/auth/usuarios/${encodeURIComponent(seleccionado.value.id)}`,{method:'DELETE'}); deleteDialog.value=false; successMsg.value='Usuario eliminado correctamente.'; seleccionado.value=null; await cargarDatos() }
  catch(e:any){ errorMsg.value=e?.message||'No se pudo eliminar el usuario.' } finally{ deleting.value=false }
}

onMounted(cargarDatos)
</script>
