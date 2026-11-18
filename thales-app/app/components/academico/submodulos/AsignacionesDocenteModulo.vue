<template>
    <div>
        <div class="d-flex justify-space-between align-center mb-4 flex-wrap ga-3">
            <div class="d-flex ga-2">
                <v-btn variant="tonal" color="primary" :loading="loading" @click="cargarTodo">
                    <v-icon start>mdi-refresh</v-icon>Actualizar</v-btn>
                <v-btn color="primary" @click="nuevo">
                    <v-icon start>mdi-plus</v-icon>Nueva asignación</v-btn>
            </div>
        </div>
        <v-alert v-if="errorMsg" type="error" variant="tonal" closable class="mb-4" @click:close="errorMsg=''">{{ errorMsg }}</v-alert>
        <v-alert v-if="successMsg" type="success" variant="tonal" closable class="mb-4" @click:close="successMsg=''">{{ successMsg }}</v-alert>
        <v-card class="main-card pa-4 mb-4">
            <v-text-field v-model="buscar" label="Buscar docente, curso, sección o periodo" prepend-inner-icon="mdi-magnify" variant="outlined" density="compact" clearable hide-details /></v-card>
        <v-card class="main-card">
            <v-data-table :headers="headers" :items="filtrados" :loading="loading">
                <template #item.docenteId="{item}">{{ docenteNombre(item.docenteId) }}</template>
                <template #item.cursoId="{item}">{{ catalogoNombre(cursos,item.cursoId) }}</template>
                <template #item.seccionId="{item}">{{ catalogoNombre(secciones,item.seccionId) }}</template>
                <template #item.periodoAcademicoId="{item}">{{ catalogoNombre(periodos,item.periodoAcademicoId) }}</template>
                <template #item.estado="{item}">
                    <v-chip size="small" :color="item?.estado==='ACTIVA'?'success':'default'">
                        {{ item?.estado }}
                    </v-chip>
                </template>
                <template #item.actions="{item}">
                    <div class="d-flex justify-end ga-1">
                        <v-btn icon="mdi-pencil" size="small" variant="text" @click="editar(item)" />
                        <v-btn icon="mdi-delete" size="small" variant="text" color="error" @click="eliminar(item)" />
                    </div>
                </template>
            </v-data-table>
        </v-card>
        <v-dialog v-model="dialog" max-width="750">
            <v-card class="pa-5">
                <v-card-title>{{form.id?'Editar asignación':'Nueva asignación'}}</v-card-title>
                <v-card-text>
                    <v-row>
                        <v-col cols="12" md="6">
                            <v-select v-model="form.docenteId" :items="docenteItems" label="Docente *" variant="outlined" /></v-col>
                        <v-col cols="12" md="6">
                            <v-select v-model="form.cursoId" :items="items(cursos)" label="Curso *" variant="outlined" /></v-col>
                        <v-col cols="12" md="6">
                            <v-select v-model="form.seccionId" :items="items(secciones)" label="Sección *" variant="outlined" /></v-col>
                        <v-col cols="12" md="6">
                            <v-select v-model="form.periodoAcademicoId" :items="items(periodos)" label="Periodo académico *" variant="outlined" /></v-col>
                        <v-col cols="12">
                            <v-select v-model="form.estado" :items="['ACTIVA','FINALIZADA','CANCELADA']" label="Estado" variant="outlined" /></v-col>
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
function normalizar(a: any[]) {
    return a
        .filter(Boolean)
        .map(x => ({
            ...x,
            id: x.id ?? x._id
        }))
    }

    const {
        academico,
        matricula
    } = useAcademicoHttp();
    const loading = ref(false),
        saving = ref(false),
        dialog = ref(false),
        errorMsg = ref(''),
        successMsg = ref(''),
        buscar = ref('');
    const asignaciones = ref<any[]>([]),
        docentes = ref<any[]>([]),
        cursos = ref<any[]>([]),
        secciones = ref<any[]>([]),
        periodos = ref<any[]>([]);
    const form = reactive < any > ({
        id: null,
        docenteId: '',
        cursoId: '',
        seccionId: '',
        periodoAcademicoId: '',
        estado: 'ACTIVA'
    });
    const headers = [{
        title: 'Docente',
        key: 'docenteId'
        }, {
            title: 'Curso',
            key: 'cursoId'
        }, {
            title: 'Sección',
            key: 'seccionId'
        }, {
            title: 'Periodo',
            key: 'periodoAcademicoId'
        }, {
            title: 'Estado',
            key: 'estado'
        }, {
            title: 'Acciones',
            key: 'actions',
            sortable: false,
            align: 'end'
    }] as const;

    function arr(d: any, key?: string) {
        if (Array.isArray(d)) return d;
        if (key && Array.isArray(d?.[key])) return d[key];
        return []
    }

    function nombre(x: any) {
        return x?.nombre || x?.codigo || x?.descripcion || x?.id || ''
    }

    function items(r: any) {
        return r.value.map((x: any) => ({
            title: nombre(x),
            value: x.id
        }))
    }
    const docenteItems = computed(() => docentes.value.map(x => ({
        title: `${x.codigoDocente} - ${x.especialidad}`,
        value: x.id
    })));

    function docenteNombre(id: string) {
        const x = docentes.value?.find(d => d.id === id);
        return x ? `${x.codigoDocente} - ${x.especialidad}` : id || ''
    }

    function catalogoNombre(r: any, id: string) {
        if (!r?.value || !Array.isArray(r.value)) {
            return id || ''
        }

        const item = r.value.find((x: any) => x.id === id)

        return nombre(item || { id })
    }

    const filtrados = computed(() => {
        const q = buscar.value.toLowerCase().trim();
        return asignaciones.value.filter(x => !q || [docenteNombre(x.docenteId), catalogoNombre(cursos, x.cursoId), catalogoNombre(secciones, x.seccionId), catalogoNombre(periodos, x.periodoAcademicoId), x.estado].some(v => String(v).toLowerCase().includes(q)))
    });

    function reset() {
        Object.assign(form, {
            id: null,
            docenteId: '',
            cursoId: '',
            seccionId: '',
            periodoAcademicoId: '',
            estado: 'ACTIVA'
        })
    }

    function nuevo() {
        reset();
        dialog.value = true
    }

    function editar(x: any) {
        Object.assign(form, x);
        dialog.value = true
    }
    async function cargarTodo() {
        loading.value = true;
        errorMsg.value = '';
        try {
            const [a, d, c, s, p] = await Promise.all([academico('/api/asignaciones-docente'), academico('/api/docentes'), matricula('/api/cursos'), matricula('/api/secciones'), matricula('/api/periodos-academicos')]);
            asignaciones.value = normalizar(arr(a));
            docentes.value = normalizar(arr(d));
            cursos.value = normalizar(arr(c, 'cursos'));
            secciones.value = normalizar(arr(s, 'secciones'));
            periodos.value = normalizar(arr(p, 'periodos'))
        } catch (e: any) {
            errorMsg.value = e.message
        } finally {
            loading.value = false
        }
    }
    async function guardar() {
        if (!form.docenteId || !form.cursoId || !form.seccionId || !form.periodoAcademicoId) {
            errorMsg.value = 'Completa docente, curso, sección y periodo.';
            return
        }
        saving.value = true;
        try {
            const body = JSON.stringify({
                docenteId: form.docenteId,
                cursoId: form.cursoId,
                seccionId: form.seccionId,
                periodoAcademicoId: form.periodoAcademicoId,
                estado: form.estado
            });
            await academico(form.id ? `/api/asignaciones-docente/${form.id}` : '/api/asignaciones-docente', {
                method: form.id ? 'PUT' : 'POST',
                body
            });
            dialog.value = false;
            successMsg.value = form.id ? 'Asignación actualizada.' : 'Asignación creada.';
            await cargarTodo()
        } catch (e: any) {
            errorMsg.value = e.message
        } finally {
            saving.value = false
        }
    }
    async function eliminar(x: any) {
        if (!await confirmAction('¿Eliminar esta asignación?')) return;
        try {
            await academico(`/api/asignaciones-docente/${x.id}`, {
                method: 'DELETE'
            });
            successMsg.value = 'Asignación eliminada.';
            await cargarTodo()
        } catch (e: any) {
            errorMsg.value = e.message
        }
    }
    onMounted(cargarTodo)
</script>