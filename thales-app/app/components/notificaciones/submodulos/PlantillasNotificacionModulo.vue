<template>
    <div>
        <div class="d-flex justify-space-between align-center mb-4 flex-wrap ga-3">
            <div class="d-flex ga-2">
                <v-btn variant="tonal" color="primary" :loading="loading" @click="cargar">
                    <v-icon start>mdi-refresh</v-icon>Actualizar</v-btn>
                <v-btn color="primary" @click="nueva">
                    <v-icon start>mdi-plus</v-icon>Nueva plantilla</v-btn>
            </div>
        </div>
        <v-alert v-if="errorMsg" type="error" variant="tonal" closable class="mb-4" @click:close="errorMsg=''">{{errorMsg}}</v-alert>
        <v-alert v-if="successMsg" type="success" variant="tonal" closable class="mb-4" @click:close="successMsg=''">{{successMsg}}</v-alert>
        <v-card class="main-card pa-4 mb-4">
            <v-text-field v-model="buscar" label="Buscar por código, nombre, evento o canal" prepend-inner-icon="mdi-magnify" variant="outlined" clearable hide-details /></v-card>
        <v-card class="main-card">
            <v-data-table :headers="headers" :items="filtradas" :loading="loading">
                <template #item.activo="{item}">
                    <v-chip size="small" :color="item.activo?'success':'default'">{{item.activo?'ACTIVA':'INACTIVA'}}</v-chip>
                </template>
                <template #item.actions="{item}">
                    <div class="d-flex justify-end ga-1">
                        <v-btn :icon="item.activo?'mdi-toggle-switch':'mdi-toggle-switch-off-outline'" size="small" variant="text" @click="toggle(item)" />
                        <v-btn icon="mdi-pencil" size="small" variant="text" @click="editar(item)" />
                        <v-btn icon="mdi-delete" size="small" variant="text" color="error" @click="eliminar(item)" />
                    </div>
                </template>
            </v-data-table>
        </v-card>

        <v-dialog v-model="dialog" max-width="760">
            <v-card class="pa-5">
                <v-card-title>{{form.id?'Editar plantilla':'Nueva plantilla'}}</v-card-title>
                <v-card-text>
                    <v-row>
                        <v-col cols="12" md="6">
                            <v-text-field v-model="form.codigo" label="Código *" variant="outlined" /></v-col>
                        <v-col cols="12" md="6">
                            <v-text-field v-model="form.nombre" label="Nombre *" variant="outlined" /></v-col>
                        <v-col cols="12" md="6">
                            <v-text-field v-model="form.evento" label="Evento *" variant="outlined" /></v-col>
                        <v-col cols="12" md="6">
                            <v-select v-model="form.canal" :items="['EMAIL','PUSH','SMS']" label="Canal *" variant="outlined" /></v-col>
                        <v-col cols="12">
                            <v-text-field v-model="form.asunto" label="Asunto" variant="outlined" /></v-col>
                        <v-col cols="12">
                            <v-textarea v-model="form.contenido" label="Contenido" variant="outlined" rows="5" /></v-col>
                        <v-col cols="12">
                            <v-switch v-model="form.activo" label="Plantilla activa" color="primary" /></v-col>
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
const {notificaciones}=useNotificacionesHttp()
    const loading=ref(false),saving=ref(false),dialog=ref(false),errorMsg=ref(''),successMsg=ref(''),buscar=ref('')
    const registros=ref<any[]>([])
    const form=reactive<any>({id:null,codigo:'',nombre:'',asunto:'',contenido:'',canal:'EMAIL',evento:'',activo:true})
    const headers=[{title:'Código',key:'codigo'},{title:'Nombre',key:'nombre'},{title:'Evento',key:'evento'},{title:'Canal',key:'canal'},{title:'Estado',key:'activo'},{title:'Acciones',key:'actions',sortable:false,align:'end' as const}]
    const filtradas=computed(()=>{const q=buscar.value.toLowerCase().trim();return registros.value.filter(x=>!q||[x.codigo,x.nombre,x.evento,x.canal].some(v=>String(v||'').toLowerCase().includes(q)))})
    function reset(){Object.assign(form,{id:null,codigo:'',nombre:'',asunto:'',contenido:'',canal:'EMAIL',evento:'',activo:true})}
    function nueva(){reset();dialog.value=true}
    function editar(x:any){Object.assign(form,x);dialog.value=true}
    async function cargar(){loading.value=true;try{const d=await notificaciones('/api/plantillas-notificacion');registros.value=Array.isArray(d)?d:[]}catch(e:any){errorMsg.value=e.message}finally{loading.value=false}}
    async function guardar(){if(!form.codigo||!form.nombre||!form.evento||!form.canal){errorMsg.value='Código, nombre, evento y canal son obligatorios.';return}saving.value=true;try{const body=JSON.stringify({codigo:form.codigo,nombre:form.nombre,asunto:form.asunto,contenido:form.contenido,canal:form.canal,evento:form.evento,activo:form.activo});await notificaciones(form.id?`/api/plantillas-notificacion/${form.id}`:'/api/plantillas-notificacion',{method:form.id?'PUT':'POST',body});dialog.value=false;successMsg.value=form.id?'Plantilla actualizada.':'Plantilla creada.';await cargar()}catch(e:any){errorMsg.value=e.message}finally{saving.value=false}}
    async function toggle(x:any){try{await notificaciones(`/api/plantillas-notificacion/${x.id}/${x.activo?'desactivar':'activar'}`,{method:'PATCH'});await cargar()}catch(e:any){errorMsg.value=e.message}}
    async function eliminar(x:any){if(!await confirmAction('¿Eliminar esta plantilla?'))return;try{await notificaciones(`/api/plantillas-notificacion/${x.id}`,{method:'DELETE'});await cargar()}catch(e:any){errorMsg.value=e.message}}
    onMounted(cargar)
</script>