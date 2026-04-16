<template>
  <div class="space-y-6">
    <!-- Header Alumno Simplificado -->
    <div class="section-card flex flex-col md:flex-row items-center justify-between gap-6 border-l-4 border-l-slate-900">
      <div>
        <h2 class="text-2xl font-bold text-slate-900 tracking-tight">
          Hola, {{ authStore.user?.firstName || authStore.user?.name || 'Estudiante' }} 
        </h2>
        <p class="text-slate-500 mt-1 text-sm font-medium">
          Dashboard Universitario · <span class="text-slate-900">Buscando Prácticas</span>
        </p>
      </div>

      <div class="flex gap-3">
        <button @click="$router.push('/gallery')" class="btn-secondary text-sm flex items-center gap-2">
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
            Fotos Eventos
        </button>
        <button @click="$router.push('/student/onboarding')" class="btn-secondary text-sm">
          Mi Perfil / CV
        </button>
        <button @click="$router.push('/student/search')" class="btn-primary text-sm flex items-center gap-2">
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
          Explorar Ofertas
        </button>
      </div>
    </div>

    <!-- Progreso Perfil Simplificado -->
    <div v-if="profileProgress < 100" class="section-card border-slate-200">
        <div class="flex justify-between items-center mb-2">
            <span class="label-caps">Completado de Perfil</span>
            <span class="text-xs font-bold text-slate-900">{{ profileProgress }}%</span>
        </div>
        <div class="w-full bg-slate-100 rounded-full h-1.5 overflow-hidden">
            <div class="bg-slate-900 h-full transition-all duration-1000" :style="{ width: profileProgress + '%' }"></div>
        </div>
    </div>
    
    <!-- MatchMaker Simplificado -->
    <div v-if="aiMatches.length > 0" class="bg-slate-50 border border-slate-200 p-6 rounded-2xl relative overflow-hidden">
        <h3 class="text-sm font-extrabold text-slate-900 uppercase tracking-widest flex items-center gap-2 mb-4">
            <span class="w-2 h-2 rounded-full bg-emerald-500"></span>
            Recomendaciones Inteligentes
        </h3>
        <p class="text-slate-600 mt-1 mb-4">Basado en tus skills y destino (70-20-10 match), tenemos ofertas altamente compatibles para ti.</p>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div v-for="match in aiMatches.slice(0,3)" :key="match.opportunity.id" 
                 @click="$router.push('/student/offers/' + match.opportunity.id)"
                 class="bg-white p-4 rounded-xl border border-slate-200 hover-scale cursor-pointer group hover:border-emerald-300 hover:shadow-xl hover:shadow-emerald-900/5 transition-all">
                <div class="flex justify-between items-start mb-2">
                    <span class="text-xs font-bold px-2 py-1 bg-emerald-100 text-emerald-800 rounded-full">{{ Math.round(match.score * 100) }}% Comaptibilidad</span>
                    <span class="text-xs text-slate-400 border border-slate-200 px-2 rounded">{{ match.opportunity.country }}</span>
                </div>
                <h4 class="font-bold text-slate-800 group-hover:text-primary-600 transition-colors">{{ match.opportunity.title }}</h4>
                <!-- Placeholder Empresa -->
                <p class="text-sm text-slate-500 mt-1">Empresa Tech SL</p>
                <div class="mt-4 flex gap-1 flex-wrap">
                     <!-- Etiquetas Reales del Match -->
                     <span v-for="kw in match.matchedKeywords" :key="kw" 
                           class="text-[10px] bg-emerald-50 text-emerald-700 px-2 py-0.5 rounded-md font-bold border border-emerald-100">
                        {{ kw }}
                     </span>
                     <span v-if="match.matchedKeywords.length === 0" class="text-[10px] text-slate-400 italic">
                        Match por ubicación/estudios
                     </span>
                </div>
            </div>
        </div>
    </div>


    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Estadísticas de Solicitudes (Fila 2) -->
      <div class="lg:col-span-2 glass-card rounded-2xl p-6">
        <h3 class="text-lg font-bold text-slate-800 mb-4 border-b border-slate-100 pb-3">Mis Procesos de Selección</h3>
        
        <div v-if="loadingApps" class="animate-pulse space-y-4">
            <div v-for="i in 2" :key="i" class="bg-slate-100 h-24 rounded-xl"></div>
        </div>
        <div v-else-if="applications.length === 0" class="text-center py-10 bg-slate-50 rounded-xl border border-slate-200 border-dashed">
            <svg class="w-12 h-12 text-slate-300 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path></svg>
            <p class="text-slate-500 font-medium">No has postulado a ninguna oferta todavía.</p>
            <button @click="$router.push('/student/search')" class="mt-4 text-primary-600 hover:text-primary-700 font-medium underline underline-offset-4">Explorar catálogo</button>
        </div>
        <div v-else class="space-y-6">
            <!-- Iterate over applications -->
            <div v-for="app in applications" :key="app.id" class="border border-slate-200 rounded-2xl p-5 bg-white hover:border-sky-200 hover:shadow-lg hover:shadow-sky-900/5 transition-all">
                <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
                    <div>
                        <div class="flex items-center gap-2">
                            <h4 class="font-bold text-lg text-slate-800">{{ app.opportunity.title }}</h4>
                            <span :class="getStatusClasses(app.status)" class="px-2 py-0.5 text-[9px] font-black rounded-md uppercase tracking-wider">
                                {{ app.status }}
                            </span>
                        </div>
                        <p class="text-xs text-slate-500 mt-1">
                            {{ app.opportunity.country }} · Aplicado: {{ new Date(app.createdAt).toLocaleDateString() }}
                            <span v-if="app.compatibilityScore" class="ml-2 font-black text-emerald-600">({{ app.compatibilityScore }}% Match)</span>
                        </p>
                        <div v-if="app.matchedKeywords" class="mt-2 flex flex-wrap gap-1">
                             <span v-for="kw in app.matchedKeywords.split(',')" :key="kw" class="text-[8px] bg-slate-50 text-slate-400 px-1.5 py-0.5 rounded border border-slate-100 font-bold uppercase">
                                {{ kw.trim() }}
                             </span>
                        </div>
                    </div>
                    
                    <div class="flex gap-2">
                        <template v-if="app.status === 'OFFERED'">
                            <button @click="rejectOffer(app.id)" class="px-3 py-2 text-xs font-bold text-rose-600 border border-rose-200 rounded-lg hover:bg-rose-50">Rechazar</button>
                            <button @click="acceptOffer(app.id)" class="px-4 py-2 text-xs font-bold text-white bg-emerald-500 rounded-lg hover:bg-emerald-600 shadow-md shadow-emerald-200">Aceptar Oferta</button>
                        </template>
                        <button v-if="['APPLIED', 'INTERESTED'].includes(app.status)" 
                                @click="withdrawApplication(app.id)" 
                                class="px-3 py-2 text-xs font-bold text-slate-400 hover:text-rose-500 transition-colors uppercase">
                            Retirar
                        </button>
                    </div>
                </div>

                <!-- Visual Timeline -->
                <div class="relative pt-2 pb-6 px-2">
                    <div class="absolute top-5 left-8 right-8 h-0.5 bg-slate-100"></div>
                    <div class="relative z-10 flex justify-between">
                        <div v-for="s in [1,2,3,4,5]" :key="s" class="flex flex-col items-center">
                            <div class="w-6 h-6 rounded-full flex items-center justify-center text-[10px] font-bold transition-all duration-500"
                                 :class="app.stage >= s ? 'bg-sky-600 text-white ring-4 ring-sky-50' : 'bg-slate-200 text-slate-400'">
                                <svg v-if="app.stage > s" class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"></path></svg>
                                <span v-else>{{ s }}</span>
                            </div>
                            <span class="text-[9px] mt-2 font-bold uppercase tracking-tighter" :class="app.stage >= s ? 'text-sky-700' : 'text-slate-400'">
                                {{ ['Applied','Int','Offer','Val','Conf'][s-1] }}
                            </span>
                        </div>
                    </div>
                </div>

                <!-- Entrevista Alert inside Card if exists -->
                <div v-if="app.interview" class="mt-4 p-3 bg-indigo-50 rounded-xl border border-indigo-100 flex items-center gap-3">
                    <div class="p-2 bg-indigo-100 rounded-lg text-indigo-600">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"></path></svg>
                    </div>
                    <div class="flex-1">
                        <p class="text-xs font-bold text-indigo-900">Entrevista Programada: {{ new Date(app.interview.scheduledAt).toLocaleString() }}</p>
                        <a :href="app.interview.videoCallLink" target="_blank" class="text-[10px] text-indigo-600 font-bold underline">Unirse a la llamada</a>
                    </div>
                </div>
            </div>
        </div>
      </div>

      <!-- Tareas Pendientes o Sidebar Info -->
      <div class="glass-card rounded-2xl p-0 overflow-hidden bg-slate-800 text-white shadow-xl shadow-slate-900/10 border-0 flex flex-col">
        <div class="p-6 bg-slate-700/50">
            <h3 class="text-lg font-bold flex items-center gap-2">
                <span class="w-2 h-2 bg-sky-400 rounded-full animate-ping"></span>
                Avisos Críticos
            </h3>
        </div>
        <div class="p-6 flex-1">
            <ul class="space-y-6 relative">
              <!-- Line background -->
              <div class="absolute left-1.5 top-2 bottom-2 w-px bg-slate-700"></div>

              <li v-if="profileProgress < 100" class="relative pl-6">
                <span class="absolute left-0 top-1.5 w-3 h-3 bg-amber-500 rounded-full ring-4 ring-slate-800"></span>
                <p class="text-sm font-bold">Perfil Incompleto</p>
                <p class="text-[10px] text-slate-400 mt-1">Tu perfil está al {{ Math.round(profileProgress) }}%. Alguna ofertas requieren el 100% para postular.</p>
                <button @click="$router.push('/student/onboarding')" class="mt-2 text-[10px] bg-slate-700 px-2 py-1 rounded hover:bg-slate-600 transition-colors">Completar ahora</button>
              </li>

              <li v-if="applications.some(a => a.status === 'OFFERED')" class="relative pl-6">
                <span class="absolute left-0 top-1.5 w-3 h-3 bg-emerald-500 rounded-full ring-4 ring-slate-800"></span>
                <p class="text-sm font-bold">Oferta Pendiente</p>
                <p class="text-[10px] text-slate-400 mt-1">¡Enhorabuena! Has recibido una oferta formal. Revísala pronto.</p>
              </li>

              <li v-if="applications.some(a => a.interview)" class="relative pl-6">
                <span class="absolute left-0 top-1.5 w-3 h-3 bg-sky-500 rounded-full ring-4 ring-slate-800"></span>
                <p class="text-sm font-bold">Cita de Entrevista</p>
                <p class="text-[10px] text-slate-400 mt-1">Tienes una entrevista programada. Revisa los detalles en tu pipeline.</p>
              </li>

              <li v-if="profileProgress >= 80" class="relative pl-6">
                <span class="absolute left-0 top-1.5 w-3 h-3 bg-slate-500 rounded-full ring-4 ring-slate-800"></span>
                <p class="text-sm font-bold">T-Matching Activado</p>
                <p class="text-[10px] text-slate-400 mt-1">Estamos analizando ofertas compatibles con tu perfil.</p>
              </li>
            </ul>
        </div>
        <div class="p-6 bg-slate-900/50 mt-auto">
            <p class="text-[10px] text-slate-500 font-medium">Beducation Platform v2.1.0-dev</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '../../store/auth';
import api from '../../services/api';
const authStore = useAuthStore();
const applications = ref([]);
const loadingApps = ref(true);
const aiMatches = ref([]);

const profileProgress = computed(() => {
    if (!authStore.user) return 0;
    const user = authStore.user;
    let score = 0;
    if (user.firstName && user.lastName) score += 20;
    if (user.phone) score += 10;
    if (user.educationType) score += 10;
    if (user.motivation && user.motivation.length >= 100) score += 20;
    if (user.keywords && user.keywords.length > 0) score += 20;
    if (user.countryPreferences && user.countryPreferences.length > 0) score += 10;
    if (user.documents && user.documents.some(d => d.documentType === 'CV')) score += 10;
    return score;
});

onMounted(async () => {
    if (!authStore.user) {
        await authStore.fetchLocalUserProfile();
    }
    
    if (authStore.user && authStore.user.id) {
        fetchAppData();
    }
});

const fetchAppData = async () => {
    loadingApps.value = true;
    try {
        const studentId = authStore.user.id;
        
        // 1. Fetch matches
        try {
            const matchResponse = await api.get(`/opportunities/matching/student/${studentId}`);
            aiMatches.value = matchResponse; // Axios interceptor returns .data
        } catch (e) {
            console.warn("Could not fetch matches:", e);
        }

        // 2. Fetch applications
        try {
            const appResponse = await api.get(`/applications/student/${studentId}`);
            // applicationController returns a Page<Application>
            applications.value = appResponse.content || [];
        } catch (e) {
            console.warn("Could not fetch applications:", e);
        }
        
    } catch(e) {
        console.error("Error loading student dashboard data:", e);
    } finally {
        loadingApps.value = false;
    }
};

const acceptOffer = async (appId) => {
    try {
        if (confirm("¿Estás seguro de que quieres aceptar esta oferta? Esto te llevará a la fase de validación por parte de tu escuela.")) {
            await api.patch(`/applications/${appId}/accept`, null, {
                params: { studentId: authStore.user.id }
            });
            await fetchAppData(); 
            alert("¡Oferta aceptada! Tu escuela revisará ahora el convenio.");
        }
    } catch (e) {
        alert("Error al aceptar la oferta: " + (e.message || "Error desconocido"));
    }
};

const rejectOffer = async (appId) => {
    try {
        if (confirm("¿Seguro que deseas rechazar esta oferta? Esta acción es definitiva.")) {
            await api.patch(`/applications/${appId}/reject-offer`, null, {
                params: { studentId: authStore.user.id }
            });
            await fetchAppData();
            alert("Oferta rechazada.");
        }
    } catch (e) {
        alert("Error al rechazar oferta: " + (e.message || "Error desconocido"));
    }
};

const withdrawApplication = async (appId) => {
    try {
        if (confirm("¿Deseas retirar tu candidatura para esta oferta?")) {
            await api.delete(`/applications/${appId}/withdraw`, {
                params: { studentId: authStore.user.id }
            });
            await fetchAppData();
            alert("Candidatura retirada.");
        }
    } catch (e) {
        alert("Error al retirar candidatura: " + (e.message || "Error desconocido"));
    }
};

// Utility classes format
const getStatusClasses = (status) => {
    const map = {
        'APPLIED': 'bg-blue-100 text-blue-800',
        'INTERESTED': 'bg-violet-100 text-violet-800 border border-violet-200',
        'OFFERED': 'bg-amber-100 text-amber-800 shadow-sm animate-pulse',
        'ADMIN_APPROVED': 'bg-teal-100 text-teal-800',
        'CONFIRMED': 'bg-gradient-to-r from-emerald-400 to-emerald-500 text-white shadow-md',
        'REJECTED': 'bg-rose-100 text-rose-800 line-through decoration-white',
        'EXPIRED': 'bg-slate-200 text-slate-600'
    };
    return map[status] || 'bg-slate-100 text-slate-800';
};
</script>
