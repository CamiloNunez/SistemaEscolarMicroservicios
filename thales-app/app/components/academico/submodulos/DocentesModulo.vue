<template>
    <div>
        <div class="d-flex justify-space-between align-center mb-4 flex-wrap ga-3">
            <div class="d-flex ga-2">
                <v-btn variant="tonal" color="primary" :loading="loading" @click="cargar">
                    <v-icon start>mdi-refresh</v-icon>Actualizar</v-btn>
                <v-btn color="primary" @click="nuevo">
                    <v-icon start>mdi-plus</v-icon>Nuevo docente</v-btn>
            </div>
        </div>
        <v-alert v-if="errorMsg" type="error" variant="tonal" closable class="mb-4" @click:close="errorMsg=''">{{ errorMsg }}</v-alert>
        <v-alert v-if="successMsg" type="success" variant="tonal" closable class="mb-4" @click:close="successMsg=''">{{ successMsg }}</v-alert>
        <v-card class="main-card pa-4 mb-4">
            <v-row>
                <v-col cols="12" md="8">
                    <v-text-field v-model="buscar" label="Buscar por usuario, código o especialidad" prepend-inner-icon="mdi-magnify" variant="outlined" density="compact" clearable hide-details /></v-col>
                <v-col cols="12" md="4">
                    <v-select v-model="estadoFiltro" :items="['TODOS','ACTIVO','INACTIVO','LICENCIA']" label="Estado" variant="outlined" density="compact" hide-details /></v-col>
            </v-row>
        </v-card>
        <v-card class="main-card">
            <v-data-table :headers="headers" :items="filtrados" :loading="loading" item-value="id">
                <template #item.estado="{ item }">
                    <v-chip size="small" :color="item.estado==='ACTIVO'?'success':item.estado==='LICENCIA'?'warning':'default'">{{ item.estado }}</v-chip>
                </template>
                <template #item.actions="{ item }">
                    <div class="d-flex justify-end ga-1">
                        <v-btn icon="mdi-pencil" size="small" variant="text" @click="editar(item)" />
                        <v-btn icon="mdi-delete" size="small" variant="text" color="error" @click="eliminar(item)" />
                    </div>
                </template>
            </v-data-table>
        </v-card>
        <v-dialog v-model="dialog" max-width="650">
            <v-card class="pa-5">
                <v-card-title>{{ form.id?'Editar docente':'Nuevo docente' }}</v-card-title>
                <v-card-text>
                    <v-row>
                        <v-col cols="12">
                            <v-text-field v-model="form.usuarioId" label="ID del usuario *" variant="outlined" /></v-col>
                        <v-col cols="12" md="6">
                            <v-text-field v-model="form.codigoDocente" label="Código docente *" variant="outlined" /></v-col>
                        <v-col cols="12" md="6">
                            <v-text-field v-model="form.especialidad" label="Especialidad *" variant="outlined" /></v-col>
                        <v-col cols="12">
                            <v-select v-model="form.estado" :items="['ACTIVO','INACTIVO','LICENCIA']" label="Estado" variant="outlined" /></v-col>
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
function normalizar(a:any[]){return a.map(x=>({...x,id:x.id||x._id}))}
    
    const { academico } = useAcademicoHttp()
    const loading=ref(false), saving=ref(false), dialog=ref(false), errorMsg=ref(''), successMsg=ref(''), buscar=ref(''), estadoFiltro=ref('TODOS'), docentes=ref<any[]>([])
    const form=reactive<any>({id:null,usuarioId:'',codigoDocente:'',especialidad:'',estado:'ACTIVO'})
    const headers=[{title:'Código',key:'codigoDocente'},{title:'Usuario ID',key:'usuarioId'},{title:'Especialidad',key:'especialidad'},{title:'Estado',key:'estado'},{title:'Acciones',key:'actions',sortable:false,align:'end' as const}]
    const filtrados=computed(()=>docentes.value.filter(x=>{const q=buscar.value.toLowerCase().trim(); return (!q||[x.usuarioId,x.codigoDocente,x.especialidad].some(v=>String(v||'').toLowerCase().includes(q)))&&(estadoFiltro.value==='TODOS'||x.estado===estadoFiltro.value)}))
    function reset(){Object.assign(form,{id:null,usuarioId:'',codigoDocente:'',especialidad:'',estado:'ACTIVO'})}
    function nuevo(){reset();dialog.value=true} function editar(x:any){Object.assign(form,x);dialog.value=true}
    async function cargar(){loading.value=true;errorMsg.value='';try{const d=await academico('/api/docentes');docentes.value=normalizar(Array.isArray(d)?d:[])}catch(e:any){errorMsg.value=e.message}finally{loading.value=false}}
    async function guardar(){if(!form.usuarioId||!form.codigoDocente||!form.especialidad){errorMsg.value='Usuario, código y especialidad son obligatorios.';return} saving.value=true;errorMsg.value='';try{const body=JSON.stringify({usuarioId:form.usuarioId,codigoDocente:form.codigoDocente,especialidad:form.especialidad,estado:form.estado});await academico(form.id?`/api/docentes/${form.id}`:'/api/docentes',{method:form.id?'PUT':'POST',body});dialog.value=false;successMsg.value=form.id?'Docente actualizado.':'Docente creado.';await cargar()}catch(e:any){errorMsg.value=e.message}finally{saving.value=false}}
    async function eliminar(x:any){if(!await confirmAction(`¿Eliminar docente ${x.codigoDocente}?`))return;try{await academico(`/api/docentes/${x.id}`,{method:'DELETE'});successMsg.value='Docente eliminado.';await cargar()}catch(e:any){errorMsg.value=e.message}}
    onMounted(cargar)
</script>