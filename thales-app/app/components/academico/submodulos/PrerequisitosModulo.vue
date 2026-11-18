<template>
    <div>
        <div class="d-flex justify-space-between align-center mb-4 flex-wrap ga-3">
            <div class="d-flex ga-2">
                <v-btn variant="tonal" color="primary" :loading="loading" @click="cargarTodo"
                    ><v-icon start>mdi-refresh</v-icon>Actualizar</v-btn
                ><v-btn color="primary" @click="nuevo"><v-icon start>mdi-plus</v-icon>Nuevo prerrequisito</v-btn>
            </div>
        </div>
        <v-alert v-if="errorMsg" type="error" variant="tonal" closable class="mb-4" @click:close="errorMsg=''"
            >{{errorMsg}}</v-alert
        ><v-alert v-if="successMsg" type="success" variant="tonal" closable class="mb-4" @click:close="successMsg=''"
            >{{successMsg}}</v-alert
        ><v-card class="main-card pa-4 mb-4"
            ><v-text-field
                v-model="buscar"
                label="Buscar curso, prerrequisito, tipo o estado"
                prepend-inner-icon="mdi-magnify"
                variant="outlined"
                density="compact"
                clearable
                hide-details /></v-card
        ><v-card class="main-card"
            ><v-data-table :headers="headers" :items="filtrados" :loading="loading"
                ><template #item.cursoId="{item}">{{cursoNombre(item.cursoId)}}</template
                ><template #item.cursoPrerequisitoId="{item}">{{cursoNombre(item.cursoPrerequisitoId)}}</template
                ><template #item.actions="{item}"
                    ><div class="d-flex justify-end ga-1">
                        <v-btn icon="mdi-pencil" size="small" variant="text" @click="editar(item)" /><v-btn
                            icon="mdi-delete"
                            size="small"
                            variant="text"
                            color="error"
                            @click="eliminar(item)"
                        /></div></template></v-data-table></v-card
        ><v-dialog v-model="dialog" max-width="700"
            ><v-card class="pa-5"
                ><v-card-title>{{form.id?'Editar prerrequisito':'Nuevo prerrequisito'}}</v-card-title
                ><v-card-text
                    ><v-row
                        ><v-col cols="12" md="6"
                            ><v-select
                                v-model="form.cursoId"
                                :items="cursoItems"
                                label="Curso *"
                                variant="outlined" /></v-col
                        ><v-col cols="12" md="6"
                            ><v-select
                                v-model="form.cursoPrerequisitoId"
                                :items="cursoItems"
                                label="Curso prerrequisito *"
                                variant="outlined" /></v-col
                        ><v-col cols="12" md="6"
                            ><v-select
                                v-model="form.tipo"
                                :items="['OBLIGATORIO','RECOMENDADO']"
                                label="Tipo"
                                variant="outlined" /></v-col
                        ><v-col cols="12" md="6"
                            ><v-select
                                v-model="form.estado"
                                :items="['ACTIVO','INACTIVO']"
                                label="Estado"
                                variant="outlined" /></v-col></v-row></v-card-text
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
    const { academico, matricula } = useAcademicoHttp();
    const loading = ref(false),
        saving = ref(false),
        dialog = ref(false),
        errorMsg = ref(""),
        successMsg = ref(""),
        buscar = ref(""),
        registros = ref<any[]>([]),
        cursos = ref<any[]>([]);
    const form = reactive<any>({
        id: null,
        cursoId: "",
        cursoPrerequisitoId: "",
        tipo: "OBLIGATORIO",
        estado: "ACTIVO",
    });
    const headers = [
        { title: "Curso", key: "cursoId" },
        { title: "Prerrequisito", key: "cursoPrerequisitoId" },
        { title: "Tipo", key: "tipo" },
        { title: "Estado", key: "estado" },
        { title: "Acciones", key: "actions", sortable: false, align: "end" as const },
    ];
    function arr(d: any) {
        return Array.isArray(d) ? d : Array.isArray(d?.cursos) ? d.cursos : [];
    }
    function cursoNombre(id: string) {
        const x = cursos.value.find((c) => c.id === id);
        return x ? `${x.nombre || x.codigo || x.id}${x.codigo && x.nombre ? " (" + x.codigo + ")" : ""}` : id;
    }
    const cursoItems = computed(() => cursos.value.map((x) => ({ title: cursoNombre(x.id), value: x.id })));
    const filtrados = computed(() => {
        const q = buscar.value.toLowerCase().trim();
        return registros.value.filter(
            (x) =>
                !q ||
                [cursoNombre(x.cursoId), cursoNombre(x.cursoPrerequisitoId), x.tipo, x.estado].some((v) =>
                    String(v || "")
                        .toLowerCase()
                        .includes(q)
                )
        );
    });
    function reset() {
        Object.assign(form, { id: null, cursoId: "", cursoPrerequisitoId: "", tipo: "OBLIGATORIO", estado: "ACTIVO" });
    }
    function nuevo() {
        reset();
        dialog.value = true;
    }
    function editar(x: any) {
        Object.assign(form, x);
        dialog.value = true;
    }
    async function cargarTodo() {
        loading.value = true;
        try {
            const [r, c] = await Promise.all([academico("/api/prerequisitos"), matricula("/api/cursos")]);
            registros.value = arr(r);
            cursos.value = arr(c);
        } catch (e: any) {
            errorMsg.value = e.message;
        } finally {
            loading.value = false;
        }
    }
    async function guardar() {
        if (form.cursoId === form.cursoPrerequisitoId) {
            errorMsg.value = "Un curso no puede ser prerrequisito de sí mismo.";
            return;
        }
        saving.value = true;
        try {
            const body = JSON.stringify({
                cursoId: form.cursoId,
                cursoPrerequisitoId: form.cursoPrerequisitoId,
                tipo: form.tipo,
                estado: form.estado,
            });
            await academico(form.id ? `/api/prerequisitos/${form.id}` : "/api/prerequisitos", {
                method: form.id ? "PUT" : "POST",
                body,
            });
            dialog.value = false;
            successMsg.value = form.id ? "Prerrequisito actualizado." : "Prerrequisito creado.";
            await cargarTodo();
        } catch (e: any) {
            errorMsg.value = e.message;
        } finally {
            saving.value = false;
        }
    }
    async function eliminar(x: any) {
        if (!await confirmAction("¿Eliminar este prerrequisito?")) return;
        try {
            await academico(`/api/prerequisitos/${x.id}`, { method: "DELETE" });
            await cargarTodo();
        } catch (e: any) {
            errorMsg.value = e.message;
        }
    }
    onMounted(cargarTodo);
</script>
