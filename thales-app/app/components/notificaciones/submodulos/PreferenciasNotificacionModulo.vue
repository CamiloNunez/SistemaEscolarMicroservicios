<template>
    <div>
        <div class="d-flex justify-space-between align-center mb-4">

            <v-btn variant="tonal" color="primary" :loading="loading" @click="cargar">
                <v-icon start>mdi-refresh</v-icon>Actualizar</v-btn>
        </div>
        <v-alert v-if="errorMsg" type="error" variant="tonal" closable class="mb-4" @click:close="errorMsg=''">{{errorMsg}}</v-alert>
        <v-card class="main-card">
            <v-data-table :headers="headers" :items="registros" :loading="loading">
                <template #item.actions="{item}">
                    <v-btn icon="mdi-pencil" size="small" variant="text" @click="editar(item)" />
                </template>
            </v-data-table>
        </v-card>

        <v-dialog v-model="dialog" max-width="650">
            <v-card class="pa-5">
                <v-card-title>Editar preferencias</v-card-title>
                <v-card-text>
                    <v-text-field :model-value="form.usuarioId" label="Usuario ID" variant="outlined" readonly />
                    <v-switch v-model="form.correoHabilitado" label="Correo habilitado" color="primary" />
                    <v-switch v-model="form.pushHabilitado" label="Push habilitado" color="primary" />
                    <v-switch v-model="form.notificacionesPago" label="Notificaciones de pagos" color="primary" />
                    <v-switch v-model="form.notificacionesMatricula" label="Notificaciones de matrícula" color="primary" />
                    <v-switch v-model="form.notificacionesAcademicas" label="Notificaciones académicas" color="primary" />
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
    const {notificaciones}=useNotificacionesHttp()
    const loading=ref(false),saving=ref(false),dialog=ref(false),errorMsg=ref('')
    const registros=ref<any[]>([])
    const form=reactive<any>({usuarioId:'',correoHabilitado:true,pushHabilitado:true,notificacionesPago:true,notificacionesMatricula:true,notificacionesAcademicas:true})
    const headers=[{title:'Usuario',key:'usuarioId'},{title:'Correo',key:'correoHabilitado'},{title:'Push',key:'pushHabilitado'},{title:'Pagos',key:'notificacionesPago'},{title:'Matrícula',key:'notificacionesMatricula'},{title:'Académicas',key:'notificacionesAcademicas'},{title:'Acciones',key:'actions',sortable:false,align:'end' as const}]
    function editar(x:any){Object.assign(form,x);dialog.value=true}
    async function cargar(){loading.value=true;try{const d=await notificaciones('/api/preferencias-notificacion');registros.value=Array.isArray(d)?d:[]}catch(e:any){errorMsg.value=e.message}finally{loading.value=false}}
    async function guardar(){saving.value=true;try{await notificaciones(`/api/preferencias-notificacion/usuario/${form.usuarioId}`,{method:'PUT',body:JSON.stringify({correoHabilitado:form.correoHabilitado,pushHabilitado:form.pushHabilitado,notificacionesPago:form.notificacionesPago,notificacionesMatricula:form.notificacionesMatricula,notificacionesAcademicas:form.notificacionesAcademicas})});dialog.value=false;await cargar()}catch(e:any){errorMsg.value=e.message}finally{saving.value=false}}
    onMounted(cargar)
</script>