<template>
  <div>
    <div class="d-flex justify-space-between align-center mb-4 flex-wrap ga-3">
      <div class="d-flex ga-2">
        <v-btn variant="tonal" color="primary" :loading="loading" @click="cargar">
          <v-icon start>mdi-refresh</v-icon>Actualizar
        </v-btn>
        <v-btn color="primary" @click="nueva">
          <v-icon start>mdi-plus</v-icon>Nueva notificación
        </v-btn>
      </div>
    </div>

    <v-alert v-if="errorMsg" type="error" variant="tonal" closable class="mb-4" @click:close="errorMsg=''">
      {{ errorMsg }}
    </v-alert>
    <v-alert v-if="successMsg" type="success" variant="tonal" closable class="mb-4" @click:close="successMsg=''">
      {{ successMsg }}
    </v-alert>

    <v-card class="main-card pa-4 mb-4">
      <v-row>
        <v-col cols="12" md="8">
          <v-text-field v-model="buscar" label="Buscar por evento, referencia, asunto o canal" prepend-inner-icon="mdi-magnify" variant="outlined" clearable hide-details />
        </v-col>
        <v-col cols="12" md="4">
          <v-select v-model="estadoFiltro" :items="['TODOS','PENDIENTE','ENVIADA','FALLIDA']" label="Estado" variant="outlined" hide-details />
        </v-col>
      </v-row>
    </v-card>

    <v-card class="main-card">
      <v-data-table :headers="headers" :items="filtradas" :loading="loading" item-value="id">
        <template #item.canal="{item}">
          <v-chip size="small" variant="tonal">{{ item.canal }}</v-chip>
        </template>
        <template #item.estado="{item}">
          <v-chip size="small" variant="tonal" :color="colorEstado(item.estado)">{{ item.estado }}</v-chip>
        </template>
        <template #item.fechaCreacion="{item}">{{ fecha(item.fechaCreacion) }}</template>
        <template #item.actions="{item}">
          <div class="d-flex justify-end ga-1">
            <v-btn v-if="item.estado !== 'ENVIADA'" icon="mdi-check-circle-outline" size="small" variant="text" color="success" @click="marcarEnviada(item)" />
            <v-btn v-if="item.estado !== 'FALLIDA'" icon="mdi-alert-circle-outline" size="small" variant="text" color="warning" @click="marcarFallida(item)" />
            <v-btn icon="mdi-pencil" size="small" variant="text" @click="editar(item)" />
            <v-btn icon="mdi-delete" size="small" variant="text" color="error" @click="eliminar(item)" />
          </div>
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-model="dialog" max-width="760">
      <v-card class="pa-5">
        <v-card-title>{{ form.id ? 'Editar notificación' : 'Nueva notificación' }}</v-card-title>
        <v-card-text>
          <v-row>
            <v-col cols="12" md="6"><v-text-field v-model="form.plantillaId" label="Plantilla ID" variant="outlined" /></v-col>
            <v-col cols="12" md="6"><v-text-field v-model="form.tipoEvento" label="Tipo de evento *" variant="outlined" /></v-col>
            <v-col cols="12" md="6"><v-text-field v-model="form.referenciaId" label="Referencia ID" variant="outlined" /></v-col>
            <v-col cols="12" md="6"><v-select v-model="form.canal" :items="['EMAIL','PUSH','SMS']" label="Canal *" variant="outlined" /></v-col>
            <v-col cols="12"><v-text-field v-model="form.asunto" label="Asunto *" variant="outlined" /></v-col>
            <v-col cols="12"><v-textarea v-model="form.contenido" label="Contenido *" variant="outlined" rows="5" /></v-col>
            <v-col cols="12" md="6"><v-select v-model="form.estado" :items="['PENDIENTE','ENVIADA','FALLIDA']" label="Estado" variant="outlined" /></v-col>
          </v-row>
        </v-card-text>
        <v-card-actions class="justify-end">
          <v-btn variant="text" @click="dialog=false">Cancelar</v-btn>
          <v-btn color="primary" :loading="saving" @click="guardar">Guardar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
const { confirm: confirmAction } = useUiFeedback()
const { notificaciones } = useNotificacionesHttp()

const loading=ref(false), saving=ref(false), dialog=ref(false)
const errorMsg=ref(''), successMsg=ref(''), buscar=ref(''), estadoFiltro=ref('TODOS')
const registros=ref<any[]>([])
const form=reactive<any>({id:null,plantillaId:'',tipoEvento:'',referenciaId:'',canal:'EMAIL',asunto:'',contenido:'',estado:'PENDIENTE'})

const headers=[
  {title:'Evento',key:'tipoEvento'},
  {title:'Referencia',key:'referenciaId'},
  {title:'Canal',key:'canal'},
  {title:'Asunto',key:'asunto'},
  {title:'Estado',key:'estado'},
  {title:'Creación',key:'fechaCreacion'},
  {title:'Acciones',key:'actions',sortable:false,align:'end' as const},
]

const filtradas=computed(()=>{
  const q=buscar.value.toLowerCase().trim()
  return registros.value.filter(x =>
    (!q || [x.tipoEvento,x.referenciaId,x.asunto,x.canal].some(v=>String(v||'').toLowerCase().includes(q))) &&
    (estadoFiltro.value==='TODOS' || x.estado===estadoFiltro.value)
  )
})

function reset(){Object.assign(form,{id:null,plantillaId:'',tipoEvento:'',referenciaId:'',canal:'EMAIL',asunto:'',contenido:'',estado:'PENDIENTE'})}
function nueva(){reset();dialog.value=true}
function editar(x:any){Object.assign(form,x);dialog.value=true}

async function cargar(){
  loading.value=true
  errorMsg.value=''
  try{
    const d=await notificaciones('/api/notificaciones')
    registros.value=Array.isArray(d)?d:[]
  }catch(e:any){
    errorMsg.value=e.message
  }finally{
    loading.value=false
  }
}

async function guardar(){
  if(!form.tipoEvento||!form.canal||!form.asunto||!form.contenido){
    errorMsg.value='Evento, canal, asunto y contenido son obligatorios.'
    return
  }

  saving.value=true
  errorMsg.value=''

  try{
    const body=JSON.stringify({
      plantillaId:form.plantillaId||null,
      tipoEvento:form.tipoEvento,
      referenciaId:form.referenciaId||null,
      canal:form.canal,
      asunto:form.asunto,
      contenido:form.contenido,
      estado:form.estado,
    })

    await notificaciones(
      form.id?`/api/notificaciones/${form.id}`:'/api/notificaciones',
      {method:form.id?'PUT':'POST',body}
    )

    dialog.value=false
    successMsg.value=form.id?'Notificación actualizada.':'Notificación registrada.'
    await cargar()
  }catch(e:any){
    errorMsg.value=e.message
  }finally{
    saving.value=false
  }
}

async function marcarEnviada(x:any){
  try{
    await notificaciones(`/api/notificaciones/${x.id}/enviada`,{method:'PATCH'})
    await cargar()
  }catch(e:any){errorMsg.value=e.message}
}

async function marcarFallida(x:any){
  try{
    await notificaciones(`/api/notificaciones/${x.id}/fallida`,{method:'PATCH'})
    await cargar()
  }catch(e:any){errorMsg.value=e.message}
}

async function eliminar(x:any){
  if(!await confirmAction('¿Eliminar esta notificación?'))return
  try{
    await notificaciones(`/api/notificaciones/${x.id}`,{method:'DELETE'})
    await cargar()
  }catch(e:any){errorMsg.value=e.message}
}

function fecha(v?:string){
  if(!v)return '-'
  const d=new Date(v)
  return Number.isNaN(d.getTime())?v:new Intl.DateTimeFormat('es-PE',{dateStyle:'medium',timeStyle:'short'}).format(d)
}

function colorEstado(e?:string){
  return e==='ENVIADA'?'success':e==='FALLIDA'?'error':'warning'
}

onMounted(cargar)
</script>
