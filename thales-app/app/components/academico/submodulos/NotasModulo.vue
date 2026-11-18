<template>
    <div>
        <div class="d-flex justify-space-between align-center mb-4 flex-wrap ga-3">
            <div class="d-flex ga-2">
                <v-btn variant="tonal" color="primary" :loading="loading" @click="cargar"
                    ><v-icon start>mdi-refresh</v-icon>Actualizar</v-btn
                ><v-btn color="primary" @click="nuevo"><v-icon start>mdi-plus</v-icon>Nueva nota</v-btn>
            </div>
        </div>
        <v-alert v-if="errorMsg" type="error" variant="tonal" closable class="mb-4" @click:close="errorMsg=''"
            >{{errorMsg}}</v-alert
        ><v-alert v-if="successMsg" type="success" variant="tonal" closable class="mb-4" @click:close="successMsg=''"
            >{{successMsg}}</v-alert
        ><v-card class="main-card pa-4 mb-4"
            ><v-text-field
                v-model="buscar"
                label="Buscar por estudiante, curso, docente, tipo o estado"
                prepend-inner-icon="mdi-magnify"
                variant="outlined"
                density="compact"
                clearable
                hide-details /></v-card
        ><v-card class="main-card"
            ><v-data-table :headers="headers" :items="filtrados" :loading="loading"
                ><template #item.calificacion="{item}"><strong>{{item.calificacion}}</strong></template
                ><template #item.estado="{item}"><v-chip size="small">{{item.estado}}</v-chip></template
                ><template #item.actions="{item}"
                    ><div class="d-flex justify-end ga-1">
                        <v-btn icon="mdi-pencil" size="small" variant="text" @click="editar(item)" /><v-btn
                            v-if="item.estado!=='ANULADA'"
                            icon="mdi-cancel"
                            size="small"
                            variant="text"
                            color="warning"
                            @click="anular(item)"
                        /><v-btn
                            icon="mdi-delete"
                            size="small"
                            variant="text"
                            color="error"
                            @click="eliminar(item)"
                        /></div></template></v-data-table></v-card
        ><v-dialog v-model="dialog" max-width="760"
            ><v-card class="pa-5"
                ><v-card-title>{{form.id?'Editar nota':'Nueva nota'}}</v-card-title
                ><v-card-text
                    ><v-row
                        ><v-col cols="12" md="6"
                            ><v-text-field
                                v-model="form.estudianteId"
                                label="Estudiante ID *"
                                variant="outlined"
                                :disabled="!!form.id" /></v-col
                        ><v-col cols="12" md="6"
                            ><v-text-field
                                v-model="form.cursoId"
                                label="Curso ID *"
                                variant="outlined"
                                :disabled="!!form.id" /></v-col
                        ><v-col cols="12" md="6"
                            ><v-text-field
                                v-model="form.periodoAcademicoId"
                                label="Periodo ID *"
                                variant="outlined"
                                :disabled="!!form.id" /></v-col
                        ><v-col cols="12" md="6"
                            ><v-text-field
                                v-model="form.docenteId"
                                label="Docente ID *"
                                variant="outlined"
                                :disabled="!!form.id" /></v-col
                        ><v-col cols="12" md="6"
                            ><v-select
                                v-model="form.tipoEvaluacion"
                                :items="['PARCIAL','FINAL','PRACTICA','TRABAJO']"
                                label="Tipo evaluación *"
                                variant="outlined" /></v-col
                        ><v-col cols="12" md="6"
                            ><v-text-field
                                v-model.number="form.calificacion"
                                type="number"
                                min="0"
                                max="20"
                                step="0.1"
                                label="Calificación *"
                                variant="outlined" /></v-col
                        ><v-col cols="12" md="6"
                            ><v-text-field
                                v-model="form.fechaEvaluacion"
                                type="datetime-local"
                                label="Fecha evaluación"
                                variant="outlined" /></v-col
                        ><v-col cols="12"
                            ><v-textarea
                                v-model="form.descripcion"
                                label="Descripción"
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
    const loading = ref(false),
        saving = ref(false),
        dialog = ref(false),
        errorMsg = ref(""),
        successMsg = ref(""),
        buscar = ref(""),
        registros = ref<any[]>([]);
    const form = reactive<any>({
        id: null,
        estudianteId: "",
        cursoId: "",
        periodoAcademicoId: "",
        docenteId: "",
        tipoEvaluacion: "PARCIAL",
        descripcion: "",
        calificacion: null,
        fechaEvaluacion: "",
    });
    const headers = [
        { title: "Estudiante", key: "estudianteId" },
        { title: "Curso", key: "cursoId" },
        { title: "Tipo", key: "tipoEvaluacion" },
        { title: "Nota", key: "calificacion" },
        { title: "Fecha", key: "fechaEvaluacion" },
        { title: "Estado", key: "estado" },
        { title: "Acciones", key: "actions", sortable: false, align: "end" as const },
    ];
    const filtrados = computed(() => {
        const q = buscar.value.toLowerCase().trim();
        return registros.value.filter(
            (x) =>
                !q ||
                [x.estudianteId, x.cursoId, x.docenteId, x.tipoEvaluacion, x.estado, x.descripcion].some((v) =>
                    String(v || "")
                        .toLowerCase()
                        .includes(q)
                )
        );
    });
    function reset() {
        Object.assign(form, {
            id: null,
            estudianteId: "",
            cursoId: "",
            periodoAcademicoId: "",
            docenteId: "",
            tipoEvaluacion: "PARCIAL",
            descripcion: "",
            calificacion: null,
            fechaEvaluacion: new Date().toISOString().slice(0, 16),
        });
    }
    function nuevo() {
        reset();
        dialog.value = true;
    }
    function editar(x: any) {
        Object.assign(form, x, { fechaEvaluacion: String(x.fechaEvaluacion || "").slice(0, 16) });
        dialog.value = true;
    }
    async function cargar() {
        loading.value = true;
        try {
            const d = await academico("/api/notas");
            registros.value = normalizar(Array.isArray(d) ? d : []);
        } catch (e: any) {
            errorMsg.value = e.message;
        } finally {
            loading.value = false;
        }
    }
    async function guardar() {
        if (form.calificacion == null || form.calificacion < 0 || form.calificacion > 20) {
            errorMsg.value = "La calificación debe estar entre 0 y 20.";
            return;
        }
        saving.value = true;
        try {
            let payload: any;
            if (form.id) {
                payload = {
                    tipoEvaluacion: form.tipoEvaluacion,
                    descripcion: form.descripcion,
                    calificacion: form.calificacion,
                    fechaEvaluacion: form.fechaEvaluacion,
                };
            } else {
                payload = {
                    estudianteId: form.estudianteId,
                    cursoId: form.cursoId,
                    periodoAcademicoId: form.periodoAcademicoId,
                    docenteId: form.docenteId,
                    tipoEvaluacion: form.tipoEvaluacion,
                    descripcion: form.descripcion,
                    calificacion: form.calificacion,
                    fechaEvaluacion: form.fechaEvaluacion,
                };
            }
            await academico(form.id ? `/api/notas/${form.id}` : "/api/notas", {
                method: form.id ? "PUT" : "POST",
                body: JSON.stringify(payload),
            });
            dialog.value = false;
            successMsg.value = form.id ? "Nota actualizada." : "Nota registrada.";
            await cargar();
        } catch (e: any) {
            errorMsg.value = e.message;
        } finally {
            saving.value = false;
        }
    }
    async function anular(x: any) {
        if (!await confirmAction("¿Anular esta nota?")) return;
        try {
            await academico(`/api/notas/${x.id}/anular`, { method: "PATCH" });
            await cargar();
        } catch (e: any) {
            errorMsg.value = e.message;
        }
    }
    async function eliminar(x: any) {
        if (!await confirmAction("¿Eliminar definitivamente esta nota?")) return;
        try {
            await academico(`/api/notas/${x.id}`, { method: "DELETE" });
            await cargar();
        } catch (e: any) {
            errorMsg.value = e.message;
        }
    }
    onMounted(cargar);
</script>