<template>
    <div>
        <v-alert class="mb-4" :type="online ? 'info' : 'warning'" variant="tonal">
            {{ online ? 'Con conexión' : 'Sin conexión: se utiliza la copia local' }} &middot;
            {{ available ? 'PWA activa' : 'PWA no activa: el modo offline requiere producción' }}.
            <span v-if="cachedAt">Copia del {{ new Date(cachedAt).toLocaleString() }}.</span>
            <v-btn class="ml-2" size="small" :disabled="!online || !available" :loading="syncing" @click="sincronizar">Sincronizar</v-btn>
        </v-alert>
        <v-alert v-if="offlineError" type="warning" class="mb-4">{{ offlineError }}</v-alert>
        <v-card v-if="status.rows.length" class="pa-4 mb-4">
            <strong>Operaciones locales pendientes de confirmación</strong>
            <div v-for="op in status.rows" :key="op.id" class="my-3">
                {{ op.method }} &middot; {{ op.path }} &middot; {{ op.state }} &middot; {{ new Date(op.createdAt).toLocaleString() }}
                <div>{{ op.error }}</div>
                <v-btn v-if="op.state !== 'sending'" size="small" variant="text" color="error" @click="descartar(op.id)">Descartar tras verificar</v-btn>
            </div>
        </v-card>
        <div class="d-flex justify-space-between align-center mb-4 flex-wrap ga-3">
            <div class="d-flex ga-2">
                <v-btn variant="tonal" color="primary" :loading="loading" @click="cargar"
                    ><v-icon start>mdi-refresh</v-icon>Actualizar</v-btn
                ><v-btn color="primary" @click="nuevo"><v-icon start>mdi-plus</v-icon>Nueva asistencia</v-btn>
            </div>
        </div>
        <v-alert v-if="errorMsg" type="error" variant="tonal" closable class="mb-4" @click:close="errorMsg=''"
            >{{errorMsg}}</v-alert
        ><v-alert v-if="successMsg" type="success" variant="tonal" closable class="mb-4" @click:close="successMsg=''"
            >{{successMsg}}</v-alert
        ><v-card class="main-card pa-4 mb-4"
            ><v-row
                ><v-col cols="12" md="8"
                    ><v-text-field
                        v-model="buscar"
                        label="Buscar por estudiante, curso, docente o sección"
                        prepend-inner-icon="mdi-magnify"
                        variant="outlined"
                        density="compact"
                        clearable
                        hide-details /></v-col
                ><v-col cols="12" md="4"
                    ><v-select
                        v-model="estadoFiltro"
                        :items="['TODOS','PRESENTE','AUSENTE','TARDANZA','JUSTIFICADO']"
                        label="Estado"
                        variant="outlined"
                        density="compact"
                        hide-details /></v-col></v-row></v-card
        ><v-card class="main-card"
            ><v-data-table :headers="headers" :items="filtrados" :loading="loading"
                ><template #item.estado="{item}"><v-chip size="small">{{item.estado}}</v-chip></template
                ><template #item.actions="{item}"
                    ><div class="d-flex justify-end ga-1">
                        <v-btn icon="mdi-pencil" size="small" variant="text" @click="editar(item)" /><v-btn
                            icon="mdi-delete"
                            size="small"
                            variant="text"
                            color="error"
                            @click="eliminar(item)"
                        /></div></template></v-data-table></v-card
        ><v-dialog v-model="dialog" max-width="760"
            ><v-card class="pa-5"
                ><v-card-title>{{form.id?'Editar asistencia':'Nueva asistencia'}}</v-card-title
                ><v-card-text
                    ><v-row
                        ><v-col cols="12" md="6"
                            ><v-text-field
                                v-model="form.estudianteId"
                                label="Estudiante ID *"
                                variant="outlined" /></v-col
                        ><v-col cols="12" md="6"
                            ><v-text-field v-model="form.cursoId" label="Curso ID *" variant="outlined" /></v-col
                        ><v-col cols="12" md="6"
                            ><v-text-field v-model="form.docenteId" label="Docente ID *" variant="outlined" /></v-col
                        ><v-col cols="12" md="6"
                            ><v-text-field v-model="form.seccionId" label="Sección ID *" variant="outlined" /></v-col
                        ><v-col cols="12" md="6"
                            ><v-text-field
                                v-model="form.fecha"
                                type="datetime-local"
                                label="Fecha *"
                                variant="outlined" /></v-col
                        ><v-col cols="12" md="6"
                            ><v-select
                                v-model="form.estado"
                                :items="['PRESENTE','AUSENTE','TARDANZA','JUSTIFICADO']"
                                label="Estado *"
                                variant="outlined" /></v-col
                        ><v-col cols="12"
                            ><v-textarea
                                v-model="form.observacion"
                                label="Observación"
                                variant="outlined"
                                rows="3" /></v-col></v-row></v-card-text
                ><v-card-actions class="justify-end"
                    ><v-btn variant="text" @click="dialog=false">Cancelar</v-btn
                    ><v-btn color="primary" :loading="saving" @click="guardar">Guardar</v-btn></v-card-actions
                ></v-card
            ></v-dialog
        >
    </div>
</template>

<script setup lang="ts">
    const { confirm: confirmAction } = useUiFeedback()
function normalizar(a: any[]) {
        return a.map((x) => ({ ...x, id: x.id || x._id }));
    }
    const { academico } = useAcademicoHttp();
    const { status, online, cachedAt, syncing, available, offlineError, sync, read, remember, enqueue, discard } = useOfflineAttendance();
    async function sincronizar() { await sync(); await cargar(); }
    async function descartar(id: string) {
        if (!await confirmAction('¿Descartar la operación local? Si hubo un envío incierto, verifica primero el registro en el servidor.')) return;
        try { await discard(id); } catch (e: any) { errorMsg.value = e.message; }
    }
    const loading = ref(false),
        saving = ref(false),
        dialog = ref(false),
        errorMsg = ref(""),
        successMsg = ref(""),
        buscar = ref(""),
        estadoFiltro = ref("TODOS"),
        registros = ref<any[]>([]);
    const form = reactive<any>({
        id: null,
        estudianteId: "",
        cursoId: "",
        docenteId: "",
        seccionId: "",
        fecha: "",
        estado: "PRESENTE",
        observacion: "",
    });
    const headers = [
        { title: "Estudiante", key: "estudianteId" },
        { title: "Curso", key: "cursoId" },
        { title: "Docente", key: "docenteId" },
        { title: "Sección", key: "seccionId" },
        { title: "Fecha", key: "fecha" },
        { title: "Estado", key: "estado" },
        { title: "Acciones", key: "actions", sortable: false, align: "end" as const },
    ];
    const filtrados = computed(() =>
        registros.value.filter((x) => {
            const q = buscar.value.toLowerCase().trim();
            return (
                (!q ||
                    [x.estudianteId, x.cursoId, x.docenteId, x.seccionId].some((v) =>
                        String(v || "")
                            .toLowerCase()
                            .includes(q)
                    )) &&
                (estadoFiltro.value === "TODOS" || x.estado === estadoFiltro.value)
            );
        })
    );
    function local(v: any) {
        return v ? String(v).slice(0, 16) : "";
    }
    function reset() {
        Object.assign(form, {
            id: null,
            estudianteId: "",
            cursoId: "",
            docenteId: "",
            seccionId: "",
            fecha: new Date().toISOString().slice(0, 16),
            estado: "PRESENTE",
            observacion: "",
        });
    }
    function nuevo() {
        reset();
        dialog.value = true;
    }
    function editar(x: any) {
        Object.assign(form, x, { fecha: local(x.fecha) });
        dialog.value = true;
    }
    async function cargar() {
        loading.value = true;
        try {
            errorMsg.value = '';
            if (!navigator.onLine) {
                registros.value = normalizar(await read());
                return;
            }
            const d = await academico("/api/asistencias");
            registros.value = normalizar(Array.isArray(d) ? d : []);
            await remember(JSON.parse(JSON.stringify(registros.value)));
        } catch (e: any) {
            errorMsg.value = e.message;
        } finally {
            loading.value = false;
        }
    }
    async function guardar() {
        saving.value = true;
        try {
            const body = JSON.stringify({
                estudianteId: form.estudianteId,
                cursoId: form.cursoId,
                docenteId: form.docenteId,
                seccionId: form.seccionId,
                fecha: form.fecha,
                estado: form.estado,
                observacion: form.observacion,
            });
            const path = form.id ? `/api/asistencias/${form.id}` : "/api/asistencias";
            const method = form.id ? "PUT" : "POST";
            if (available.value) {
                await enqueue(path, method, body);
                successMsg.value = 'Asistencia guardada en la cola local. Consulta su estado de sincronización; todavía no implica confirmación del servidor.';
            } else {
                if (!navigator.onLine) throw new Error('Activa la PWA antes de registrar sin conexión.');
                await academico(path, { method, body, retry: 0 });
                successMsg.value = form.id ? "Asistencia actualizada." : "Asistencia registrada.";
            }
            dialog.value = false;
            await cargar();
        } catch (e: any) {
            errorMsg.value = e.message;
        } finally {
            saving.value = false;
        }
    }
    async function eliminar(x: any) {
        if (!await confirmAction("¿Eliminar esta asistencia?")) return;
        try {
            if (available.value) {
                await enqueue(`/api/asistencias/${x.id}`, 'DELETE');
                successMsg.value = 'Eliminación en cola local; pendiente de confirmación del servidor.';
            } else {
                if (!navigator.onLine) throw new Error('Activa la PWA antes de eliminar sin conexión.');
                await academico(`/api/asistencias/${x.id}`, { method: "DELETE", retry: 0 });
            }
            await cargar();
        } catch (e: any) {
            errorMsg.value = e.message;
        }
    }
    watch(() => status.value.history.at(-1)?.id, () => { if (navigator.onLine) void cargar(); });
    onMounted(cargar);
</script>
