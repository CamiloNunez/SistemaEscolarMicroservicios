<template>
    <div>
        <div class="d-flex justify-space-between align-center mb-4 flex-wrap ga-3">
            <div class="d-flex ga-2">
                <v-btn variant="tonal" color="primary" :loading="loading" @click="cargar">
                    <v-icon start>mdi-refresh</v-icon>Actualizar</v-btn>
                <v-btn color="primary" @click="nuevo">
                    <v-icon start>mdi-plus</v-icon>Nuevo destinatario</v-btn>
            </div>
        </div>
        <v-alert v-if="errorMsg" type="error" variant="tonal" closable class="mb-4" @click:close="errorMsg=''">{{errorMsg}}</v-alert>
        <v-card class="main-card">
            <v-data-table :headers="headers" :items="registros" :loading="loading">
                <template #item.actions="{item}">
                    <div class="d-flex justify-end ga-1">
                        <v-btn v-if="item.estadoEntrega!=='ENTREGADO'" icon="mdi-check" size="small" variant="text" color="success" @click="entregado(item)" />
                        <v-btn v-if="item.estadoEntrega!=='FALLIDO'" icon="mdi-alert-outline" size="small" variant="text" color="warning" @click="fallido(item)" />
                        <v-btn icon="mdi-delete" size="small" variant="text" color="error" @click="eliminar(item)" />
                    </div>
                </template>
            </v-data-table>
        </v-card>

        <v-dialog v-model="dialog" max-width="720">
            <v-card class="pa-5">
                <v-card-title>Nuevo destinatario</v-card-title>
                <v-card-text>
                    <v-row>
                        <v-col cols="12" md="6">
                            <v-text-field v-model="form.notificacionId" label="Notificación ID *" variant="outlined" /></v-col>
                        <v-col cols="12" md="6">
                            <v-text-field v-model="form.usuarioId" label="Usuario ID *" variant="outlined" /></v-col>
                        <v-col cols="12">
                            <v-text-field v-model="form.direccionDestino" label="Dirección destino *" variant="outlined" /></v-col>
                        <v-col cols="12" md="6">
                            <v-select v-model="form.tipoDestinatario" :items="['ESTUDIANTE','PADRE','DOCENTE','ADMINISTRATIVO']" label="Tipo destinatario" variant="outlined" /></v-col>
                        <v-col cols="12" md="6">
                            <v-select v-model="form.estadoEntrega" :items="['PENDIENTE','ENTREGADO','FALLIDO']" label="Estado" variant="outlined" /></v-col>
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
    const loading=ref(false),saving=ref(false),dialog=ref(false),errorMsg=ref('')
    const registros=ref<any[]>([])
    const form=reactive<any>({notificacionId:'',usuarioId:'',direccionDestino:'',tipoDestinatario:'ESTUDIANTE',estadoEntrega:'PENDIENTE'})
    const headers=[{title:'Notificación',key:'notificacionId'},{title:'Usuario',key:'usuarioId'},{title:'Destino',key:'direccionDestino'},{title:'Tipo',key:'tipoDestinatario'},{title:'Estado',key:'estadoEntrega'},{title:'Acciones',key:'actions',sortable:false,align:'end' as const}]
    function nuevo(){Object.assign(form,{notificacionId:'',usuarioId:'',direccionDestino:'',tipoDestinatario:'ESTUDIANTE',estadoEntrega:'PENDIENTE'});dialog.value=true}
    async function cargar(){loading.value=true;try{const d=await notificaciones('/api/destinatarios-notificacion');registros.value=Array.isArray(d)?d:[]}catch(e:any){errorMsg.value=e.message}finally{loading.value=false}}
    async function guardar(){if(!form.notificacionId||!form.usuarioId||!form.direccionDestino){errorMsg.value='Notificación, usuario y dirección son obligatorios.';return}saving.value=true;try{await notificaciones('/api/destinatarios-notificacion',{method:'POST',body:JSON.stringify(form)});dialog.value=false;await cargar()}catch(e:any){errorMsg.value=e.message}finally{saving.value=false}}
    async function entregado(x:any){try{await notificaciones(`/api/destinatarios-notificacion/${x.id}/entregado`,{method:'PATCH'});await cargar()}catch(e:any){errorMsg.value=e.message}}
    async function fallido(x:any){try{await notificaciones(`/api/destinatarios-notificacion/${x.id}/fallido`,{method:'PATCH'});await cargar()}catch(e:any){errorMsg.value=e.message}}
    async function eliminar(x:any){if(!await confirmAction('¿Eliminar este destinatario?'))return;try{await notificaciones(`/api/destinatarios-notificacion/${x.id}`,{method:'DELETE'});await cargar()}catch(e:any){errorMsg.value=e.message}}
    onMounted(cargar)
</script>