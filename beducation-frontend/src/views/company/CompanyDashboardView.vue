<template>
  <div class="space-y-6">
    <!-- Brand / Header -->
    <div class="flex justify-between items-start md:items-center py-6 border-b border-slate-200">
        <div>
           <h1 class="text-2xl font-bold tracking-tight text-slate-800">Pipeline Comercial: Mis Ofertas Erasmus</h1>
           <p class="text-sm text-slate-500 mt-1">Gestione sus vacantes y seleccione el talento internacional para su empresa.</p>
        </div>
        <button @click="openCreateModal" class="btn-primary flex items-center gap-2 bg-slate-900 hover:bg-slate-800 shadow-sm">
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
            Crear Nueva Vacante (DRAFT)
        </button>
    </div>

    <!-- Stats & Filters -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
         <div class="glass-card p-6 md:col-span-1 flex flex-col justify-center border-l-4 border-l-sky-500">
             <span class="text-[10px] text-slate-400 font-black uppercase tracking-widest mb-1">Candidatos Activos</span>
             <span class="text-4xl font-black text-slate-800">{{ stats.totalApplications || totalCandidates }}</span>
             <p class="text-[10px] text-slate-400 mt-2 font-medium">En todos los procesos</p>
         </div>
         <div class="glass-card p-6 md:col-span-3">
            <div class="flex justify-between items-center mb-4">
                <span class="text-[10px] text-slate-400 font-black uppercase tracking-widest">Pipeline Global de Selección</span>
                <span class="text-[10px] bg-sky-100 text-sky-700 px-2 py-0.5 rounded font-bold uppercase">Real-time</span>
            </div>
            
            <div class="flex items-end gap-1 h-20">
                <div v-for="(count, status) in stats.funnel" :key="status" 
                     class="flex-1 group relative flex flex-col items-center justify-end h-full">
                    <!-- Tooltip -->
                    <div class="absolute -top-10 opacity-0 group-hover:opacity-100 transition-opacity bg-slate-800 text-white text-[10px] px-2 py-1 rounded whitespace-nowrap z-20">
                        {{ status }}: {{ count }}
                    </div>
                    <!-- Bar -->
                    <div class="w-full rounded-t-lg transition-all duration-700 bg-sky-500/10 group-hover:bg-sky-500/20 relative overflow-hidden" 
                         :style="{ height: (stats.totalApplications > 0 ? (count / stats.totalApplications * 100) : 0) + '%' }">
                         <div class="absolute inset-0 bg-sky-500 opacity-40"></div>
                    </div>
                    <!-- Label -->
                    <span class="text-[8px] mt-2 font-bold text-slate-400 truncate w-full text-center uppercase tracking-tighter">
                        {{ status.substring(0,6) }}..
                    </span>
                </div>
            </div>
         </div>
    </div>

    <!-- Job Postings Listado Maestro -->
    <div class="space-y-4">
        <h3 class="font-bold text-slate-800 flex items-center gap-2">
            <svg class="w-5 h-5 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path></svg>
            Tus ofertas de prácticas
        </h3>
        
        <div v-if="loading" class="flex justify-center py-12">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-slate-800"></div>
        </div>

        <div v-else v-for="opp in opportunities" :key="opp.id" class="bg-white border text-left border-slate-200 rounded-2xl p-6 shadow-sm hover:shadow-md transition-all relative overflow-hidden group">
            <!-- Barra de estado lateral estetica -->
            <div :class="{
                'bg-emerald-500': opp.status === 'APPROVED', 
                'bg-slate-300': opp.status === 'DRAFT', 
                'bg-amber-400': opp.status === 'PENDING_REVIEW',
                'bg-rose-500': opp.status === 'CLOSED' || opp.status === 'REJECTED'
            }" class="absolute left-0 top-0 bottom-0 w-1.5 opacity-80 group-hover:w-2 transition-all"></div>
            
            <div class="flex flex-col md:flex-row justify-between items-start gap-4 mb-4">
                <div class="pl-2">
                     <h4 class="text-xl font-bold text-slate-900 group-hover:text-sky-700 transition-colors">{{ opp.title }}</h4>
                     <p class="text-sm text-slate-500 font-medium flex items-center gap-2 mt-1">
                         <span class="flex items-center gap-1">
                             <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                             {{ opp.city }}, {{ opp.country }}
                         </span>
                         <span class="text-slate-200">|</span>
                         <span class="flex items-center gap-1">
                             <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
                             {{ opp.spotsAvailable }}/{{ opp.spots }} Vacantes Libres
                         </span>
                     </p>
                </div>
                <div class="flex flex-col items-end gap-2">
                    <span class="font-bold text-[10px] uppercase tracking-wider px-2 py-1 rounded-full border shadow-sm"
                          :class="{
                              'bg-emerald-50 text-emerald-700 border-emerald-200': opp.status === 'APPROVED',
                              'bg-amber-50 text-amber-700 border-amber-200': opp.status === 'PENDING_REVIEW',
                              'bg-slate-50 text-slate-500 border-slate-200': opp.status === 'DRAFT'
                          }">
                        {{ getStatusLabel(opp.status) }}
                    </span>
                    <span class="text-[10px] text-slate-400 font-mono">ID: #{{ opp.id }}</span>
                </div>
            </div>

            <!-- Dashboard de Acción Rápida -->
            <div class="pl-2 mt-4 pt-4 border-t border-slate-100 flex flex-wrap gap-4">
                <button v-if="opp.status === 'APPROVED'" @click="reviewCandidates(opp)" class="text-sm text-sky-600 font-bold hover:text-sky-700 flex items-center gap-1 bg-sky-50 px-3 py-1.5 rounded-lg transition-colors">
                    👁 Evaluar y Revisar Candidatos 
                    <span class="bg-sky-500 text-white rounded-full px-1.5 py-0.5 text-[10px] ml-1">{{ opp.applicationsCount || 0 }}</span>
                </button>
                <button v-if="opp.status === 'DRAFT'" @click="publishOpportunity(opp)" class="text-sm text-amber-600 font-bold hover:text-amber-700 flex items-center gap-1 bg-amber-50 px-3 py-1.5 rounded-lg transition-colors border border-amber-100">
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path></svg>
                    Enviar a Publicación
                </button>
                <button @click="editOpportunity(opp)" class="text-sm text-slate-500 font-bold hover:text-slate-700 flex items-center gap-1 px-3 py-1.5 rounded-lg hover:bg-slate-50 transition-colors">
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                    Editar
                </button>
            </div>
        </div>
        
        <div v-if="!loading && opportunities.length === 0" class="p-20 text-center text-slate-400 border-2 border-dashed border-slate-200 rounded-3xl bg-slate-50/50">
            <div class="h-16 w-16 bg-slate-100 text-slate-300 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg class="w-10 h-10" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path></svg>
            </div>
            <h4 class="text-lg font-bold text-slate-600 mb-1">Aún no has creado ofertas</h4>
            <p class="text-sm text-slate-400 mb-6 max-w-xs mx-auto">Comienza creando tu primera vacante en estado borrador para atraer talento internacional.</p>
            <button @click="openCreateModal" class="btn-primary-outline text-sm">Crear mi primera vacante</button>
        </div>
    </div>

    <!-- Modal: Crear/Editar Vacante -->
    <div v-if="showModal" class="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
        <div class="bg-white rounded-3xl shadow-2xl w-full max-w-2xl overflow-hidden animate-in fade-in zoom-in duration-300">
            <div class="px-8 py-6 border-b border-slate-100 flex justify-between items-center bg-slate-50">
                <div>
                    <h3 class="text-xl font-bold text-slate-800">{{ editingId ? 'Editar Vacante' : 'Nueva Vacante' }}</h3>
                    <p class="text-xs text-slate-500 font-medium">Complete los detalles de la oferta de prácticas.</p>
                </div>
                <button @click="closeModal" class="text-slate-400 hover:text-slate-600 bg-white p-2 rounded-full shadow-sm border border-slate-100 transition-all">
                    <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                </button>
            </div>
            
            <form @submit.prevent="saveOpportunity" class="p-8 space-y-5">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
                    <div class="md:col-span-2">
                        <label class="block text-sm font-bold text-slate-700 mb-1">Título de la vacante</label>
                        <input v-model="form.title" type="text" required class="input-field w-full" placeholder="Ej: Junior Frontend Developer Erasmus+" />
                    </div>
                    
                    <div>
                        <label class="block text-sm font-bold text-slate-700 mb-1">País</label>
                        <input v-model="form.country" type="text" required class="input-field w-full" placeholder="Ej: Ireland" />
                    </div>
                    <div>
                        <label class="block text-sm font-bold text-slate-700 mb-1">Ciudad</label>
                        <input v-model="form.city" type="text" required class="input-field w-full" placeholder="Ej: Dublin" />
                    </div>

                    <div>
                        <label class="block text-sm font-bold text-slate-700 mb-1">Fecha Inicio</label>
                        <input v-model="form.startDate" type="date" required class="input-field w-full" />
                    </div>
                    <div>
                        <label class="block text-sm font-bold text-slate-700 mb-1">Fecha Fin</label>
                        <input v-model="form.endDate" type="date" required class="input-field w-full" />
                    </div>

                    <div>
                        <label class="block text-sm font-bold text-slate-700 mb-1">Puestos totales</label>
                        <input v-model.number="form.spots" type="number" required min="1" class="input-field w-full" />
                    </div>
                    <div>
                        <label class="block text-sm font-bold text-slate-700 mb-1">Tipo de Estudiante</label>
                        <select v-model="form.educationTypeId" required class="input-field w-full">
                            <option value="">Seleccione...</option>
                            <option v-for="type in educationTypes" :key="type.id" :value="type.id">{{ type.name }}</option>
                        </select>
                    </div>
                </div>

                <div>
                    <label class="block text-sm font-bold text-slate-700 mb-1">Descripción de la vacante</label>
                    <textarea v-model="form.description" rows="4" required class="input-field w-full text-sm" placeholder="Detalle las tareas, requisitos y beneficios..."></textarea>
                </div>

                <div class="pt-6 flex justify-end gap-3 border-t border-slate-100">
                    <button type="button" @click="closeModal" class="px-6 py-2.5 text-sm font-bold text-slate-600 hover:bg-slate-50 rounded-xl transition-all border border-slate-200">Cancelar</button>
                    <button type="submit" :disabled="saving" class="btn-primary text-sm px-8 py-2.5 flex items-center gap-2 shadow-lg shadow-sky-100">
                        <span v-if="saving" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                        {{ saving ? 'Guardando...' : (editingId ? 'Actualizar Vacante' : 'Crear en Borrador') }}
                    </button>
                </div>
            </form>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../store/auth';
import api from '../../services/api';

const router = useRouter();
const authStore = useAuthStore();
const opportunities = ref([]);
const educationTypes = ref([]);
const loading = ref(true);
const saving = ref(false);

const stats = ref({
    funnel: {},
    totalApplications: 0,
    activeVacancies: 0
});

const showModal = ref(false);
const editingId = ref(null);

const form = ref({
    title: '',
    description: '',
    country: '',
    city: '',
    startDate: '',
    endDate: '',
    spots: 1,
    educationTypeId: '',
    status: 'DRAFT'
});

const companyId = computed(() => authStore.user?.id);
const totalCandidates = computed(() => {
    return opportunities.value.reduce((acc, curr) => acc + (curr.applicationsCount || 0), 0);
});

const fetchData = async () => {
    if (!companyId.value) return;
    loading.value = true;
    try {
        const [oppRes, statsRes] = await Promise.all([
            api.get(`/companies/${companyId.value}/opportunities`),
            api.get(`/companies/${companyId.value}/stats`)
        ]);
        opportunities.value = oppRes.content || oppRes || [];
        stats.value = statsRes;
    } catch (e) {
        console.error("Error fetching data:", e);
    } finally {
        loading.value = false;
    }
};

const fetchOptions = async () => {
    try {
        const res = await api.get('/education-types');
        educationTypes.value = res || [];
    } catch (e) {
        console.error("Error fetching education types:", e);
    }
};

onMounted(async () => {
    if (authStore.user) {
        await Promise.all([fetchData(), fetchOptions()]);
    }
});

const openCreateModal = () => {
    editingId.value = null;
    form.value = {
        title: '',
        description: '',
        country: '',
        city: '',
        startDate: '',
        endDate: '',
        spots: 1,
        educationTypeId: '',
        status: 'DRAFT'
    };
    showModal.value = true;
};

const editOpportunity = (opp) => {
    editingId.value = opp.id;
    form.value = {
        title: opp.title,
        description: opp.description,
        country: opp.country,
        city: opp.city,
        startDate: opp.startDate ? opp.startDate.split('T')[0] : '',
        endDate: opp.endDate ? opp.endDate.split('T')[0] : '',
        spots: opp.spots,
        educationTypeId: opp.educationType?.id || '',
        status: opp.status
    };
    showModal.value = true;
};

const closeModal = () => {
    showModal.value = false;
};

const saveOpportunity = async () => {
    if (!companyId.value) return;
    saving.value = true;
    try {
        if (editingId.value) {
            await api.put(`/companies/${companyId.value}/opportunities/${editingId.value}`, form.value);
        } else {
            await api.post(`/companies/${companyId.value}/opportunities`, form.value);
        }
        await fetchData();
        closeModal();
    } catch (e) {
        alert("Error al guardar la vacante: " + (e.message || 'Error desconocido'));
    } finally {
        saving.value = false;
    }
};

const publishOpportunity = async (opp) => {
    if (!confirm('¿Deseas enviar esta oferta a revisión para su publicación?')) return;
    try {
        const updateData = { ...opp, status: 'PENDING_REVIEW' };
        // We only send the fields needed by the DTO or the full object
        await api.put(`/companies/${companyId.value}/opportunities/${opp.id}`, { status: 'PENDING_REVIEW' });
        await fetchData();
    } catch (e) {
        alert("Error al publicar: " + (e.message || 'Error desconocido'));
    }
};

const getStatusLabel = (status) => {
    const labels = {
        'DRAFT': 'Borrador',
        'PENDING_REVIEW': 'En Revisión',
        'APPROVED': 'Publicado',
        'REJECTED': 'Rechazado',
        'CLOSED': 'Cerrado'
    };
    return labels[status] || status;
};

const reviewCandidates = (opp) => {
    router.push(`/company/opportunities/${opp.id}/candidates`);
};
</script>
