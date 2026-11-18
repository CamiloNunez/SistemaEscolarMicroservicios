<template>
  <v-container class="py-8">
    <div class="d-flex flex-wrap align-center justify-space-between ga-3 mb-6">
      <div class="d-flex align-center ga-3">
        <v-icon size="32" color="primary">mdi-shield-account</v-icon>
        <div>
          <h1 class="text-h5 font-weight-bold" style="color:#5b0014">Roles</h1>
          <p class="text-caption text-grey-darken-1 mb-0">Administración de roles y permisos del microservicio de login.</p>
        </div>
      </div>
      <div class="d-flex ga-2">
        <v-btn variant="tonal" color="primary" :loading="loading" @click="cargarRoles"><v-icon start>mdi-refresh</v-icon>Actualizar</v-btn>
        <v-btn color="primary" @click="abrirNuevo"><v-icon start>mdi-shield-plus</v-icon>Nuevo rol</v-btn>
      </div>
    </div>

    <v-alert v-if="errorMsg" type="error" variant="tonal" closable class="mb-4" @click:close="errorMsg=''">{{ errorMsg }}</v-alert>
    <v-alert v-if="successMsg" type="success" variant="tonal" closable class="mb-4" @click:close="successMsg=''">{{ successMsg }}</v-alert>

    <v-card class="main-card" elevation="3">
      <v-card-text>
        <v-text-field v-model="busqueda" prepend-inner-icon="mdi-magnify" label="Buscar rol" variant="outlined" density="compact" clearable class="mb-4" />
        <v-data-table :headers="headers" :items="filtrados" :loading="loading" item-value="id" no-data-text="No hay roles registrados">
          <template #item.nombre="{ item }"><v-chip color="primary" variant="tonal">{{ nombreVisible(item.nombre) }}</v-chip></template>
          <template #item.permisos="{ item }">
            <div class="d-flex flex-wrap ga-1 py-2"><v-chip v-for="p in item.permisos || []" :key="p" size="x-small" variant="outlined">{{ p }}</v-chip><span v-if="!item.permisos?.length" class="text-caption text-grey">Sin permisos</span></div>
          </template>
          <template #item.activo="{ item }"><v-chip :color="item.activo===false?'error':'success'" size="small" variant="tonal">{{ item.activo===false?'Inactivo':'Activo' }}</v-chip></template>
          <template #item.acciones="{ item }"><div class="d-flex ga-1 justify-end"><v-btn icon="mdi-pencil" size="small" variant="text" color="primary" @click="abrirEditar(item)"/><v-btn icon="mdi-delete" size="small" variant="text" color="error" @click="confirmarEliminar(item)"/></div></template>
        </v-data-table>
      </v-card-text>
    </v-card>

    <v-dialog v-model="dialog" max-width="650" persistent>
      <v-card>
        <v-card-title class="pa-5 d-flex align-center ga-2"><v-icon color="primary">mdi-shield-edit</v-icon>{{ editando?'Editar rol':'Nuevo rol' }}</v-card-title>
        <v-divider />
        <v-card-text class="pa-5">
          <v-form ref="formRef" @submit.prevent="guardar">
            <v-text-field v-model="form.nombre" label="Nombre del rol *" prefix="ROLE_" variant="outlined" :rules="[required]" hint="Ejemplo: SECRETARIA" persistent-hint class="mb-3" />
            <v-textarea v-model="form.descripcion" label="Descripción" variant="outlined" rows="2" class="mb-3" />
            <v-combobox v-model="form.permisos" :items="permisosSugeridos" label="Permisos" variant="outlined" multiple chips closable-chips hint="Puedes escribir permisos adicionales" persistent-hint class="mb-3" />
            <v-switch v-model="form.activo" color="success" label="Rol activo" hide-details />
          </v-form>
        </v-card-text>
        <v-card-actions class="pa-5 pt-0"><v-spacer/><v-btn variant="text" @click="cerrar">Cancelar</v-btn><v-btn color="primary" :loading="saving" @click="guardar">{{ editando?'Guardar cambios':'Crear rol' }}</v-btn></v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="deleteDialog" max-width="460">
      <v-card><v-card-title class="pa-5">Eliminar rol</v-card-title><v-card-text>¿Eliminar el rol <strong>{{ nombreVisible(seleccionado?.nombre || '') }}</strong>? Si está asignado a usuarios, el backend impedirá la eliminación.</v-card-text><v-card-actions class="pa-5 pt-0"><v-spacer/><v-btn variant="text" @click="deleteDialog=false">Cancelar</v-btn><v-btn color="error" :loading="deleting" @click="eliminar">Eliminar</v-btn></v-card-actions></v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
definePageMeta({ middleware:'auth' })
interface RolItem { id:string; nombre:string; descripcion?:string; permisos?:string[]; activo?:boolean }
const authStore=useAuthStore(), config=useRuntimeConfig()
const roles=ref<RolItem[]>([]), loading=ref(false), saving=ref(false), deleting=ref(false), busqueda=ref(''), errorMsg=ref(''), successMsg=ref('')
const dialog=ref(false), deleteDialog=ref(false), editando=ref(false), seleccionado=ref<RolItem|null>(null), formRef=ref<any>(null)
const form=reactive({nombre:'',descripcion:'',permisos:[] as string[],activo:true})
const permisosSugeridos=['READ','WRITE','UPDATE','DELETE','MANAGE_USERS','MANAGE_ROLES','REPORTS']
const required=(v:any)=>!!String(v??'').trim()||'Campo obligatorio'
const headers=[{title:'Rol',key:'nombre'},{title:'Descripción',key:'descripcion'},{title:'Permisos',key:'permisos',sortable:false},{title:'Estado',key:'activo'},{title:'Acciones',key:'acciones',sortable:false,align:'end' as const}]
const filtrados=computed(()=>{const q=busqueda.value.trim().toLowerCase(); if(!q)return roles.value; return roles.value.filter(r=>[r.nombre,r.descripcion,...(r.permisos||[])].filter(Boolean).join(' ').toLowerCase().includes(q))})
function nombreVisible(v:string){return String(v||'').replace(/^ROLE_/,'')}
function reset(){Object.assign(form,{nombre:'',descripcion:'',permisos:[],activo:true})}
function abrirNuevo(){editando.value=false;seleccionado.value=null;reset();dialog.value=true}
function abrirEditar(r:RolItem){editando.value=true;seleccionado.value=r;Object.assign(form,{nombre:nombreVisible(r.nombre),descripcion:r.descripcion||'',permisos:[...(r.permisos||[])],activo:r.activo!==false});dialog.value=true}
function cerrar(){dialog.value=false;seleccionado.value=null;formRef.value?.resetValidation?.()}
function confirmarEliminar(r:RolItem){seleccionado.value=r;deleteDialog.value=true}
async function api(path:string,options:RequestInit={}){if(!authStore.token)throw new Error('No hay token de autenticación.');const base=String(config.public.loginApiBase).trim().replace(/\/+$/,'');const headers:any={Accept:'application/json',Authorization:`Bearer ${authStore.token}`,...(options.headers||{})};const res=await fetch(base+path,{...options,headers});const data=await res.json().catch(()=>null);if(!res.ok)throw new Error(data?.mensaje||data?.message||`Error HTTP ${res.status}`);return data}
async function cargarRoles(){loading.value=true;errorMsg.value='';try{const data=await api('/api/auth/roles');roles.value=Array.isArray(data)?data:[]}catch(e:any){errorMsg.value=e?.message||'No se pudieron cargar los roles.'}finally{loading.value=false}}
async function guardar(){const result=await formRef.value?.validate?.();if(result&&!result.valid)return;saving.value=true;errorMsg.value='';successMsg.value='';try{const body={nombre:form.nombre.trim(),descripcion:form.descripcion.trim(),permisos:form.permisos.map(String).map(x=>x.trim().toUpperCase()).filter(Boolean),activo:form.activo};const path=editando.value?`/api/auth/roles/${encodeURIComponent(seleccionado.value!.id)}`:'/api/auth/roles';await api(path,{method:editando.value?'PUT':'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)});successMsg.value=editando.value?'Rol actualizado correctamente.':'Rol creado correctamente.';cerrar();await cargarRoles()}catch(e:any){errorMsg.value=e?.message||'No se pudo guardar el rol.'}finally{saving.value=false}}
async function eliminar(){if(!seleccionado.value)return;deleting.value=true;errorMsg.value='';successMsg.value='';try{await api(`/api/auth/roles/${encodeURIComponent(seleccionado.value.id)}`,{method:'DELETE'});deleteDialog.value=false;successMsg.value='Rol eliminado correctamente.';seleccionado.value=null;await cargarRoles()}catch(e:any){errorMsg.value=e?.message||'No se pudo eliminar el rol.'}finally{deleting.value=false}}
onMounted(cargarRoles)
</script>
