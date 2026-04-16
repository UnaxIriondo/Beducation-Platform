<template>
  <div class="space-y-6">
    <!-- Header with Back Button -->
    <div class="flex flex-col md:flex-row justify-between items-start md:items-center p-6 bg-white rounded-2xl shadow-sm border border-slate-200">
      <div class="flex items-center gap-4">
        <button @click="$router.push('/company/dashboard')" class="p-2 hover:bg-slate-100 rounded-full transition-colors">
          <svg class="w-6 h-6 text-slate-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>
        </button>
        <div>
          <h1 class="text-2xl font-bold text-slate-800">{{ opportunity?.title || 'Cargando...' }}</h1>
          <p class="text-slate-500 text-sm flex items-center gap-2">
            <span class="bg-emerald-100 text-emerald-700 px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider">Publicada</span>
            <span>{{ opportunity?.city }}, {{ opportunity?.country }}</span>
          </p>
        </div>
      </div>
      <div class="mt-4 md:mt-0 flex items-center gap-4">
        <div class="text-right">
          <p class="text-[10px] text-slate-400 font-bold uppercase tracking-widest">Estado del Cupo</p>
          <p class="text-lg font-bold text-slate-700">{{ opportunity?.spotsAvailable }} / {{ opportunity?.spots }} Libres</p>
        </div>
        <div class="w-12 h-12 bg-sky-50 text-sky-600 rounded-xl flex items-center justify-center border border-sky-100">
          <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
        </div>
      </div>
    </div>

    <!-- Main Content: Candidates Grid -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Sidebar: Opportunity Brief -->
      <div class="lg:col-span-1 space-y-6">
        <div class="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
          <h3 class="font-bold text-slate-800 mb-4 flex items-center gap-2">
            <svg class="w-4 h-4 text-sky-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            Detalles del Puesto
          </h3>
          <p class="text-sm text-slate-600 leading-relaxed">{{ opportunity?.description }}</p>
          
          <div class="mt-6 space-y-4">
            <div class="flex items-center gap-3">
              <div class="p-2 bg-slate-50 text-slate-400 rounded-lg">
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
              </div>
              <div>
                <p class="text-[10px] text-slate-400 font-bold uppercase">Fechas Estimadas</p>
                <p class="text-xs font-medium text-slate-700">{{ formatDate(opportunity?.startDate) }} - {{ formatDate(opportunity?.endDate) }}</p>
              </div>
            </div>
            <div class="flex items-center gap-3">
              <div class="p-2 bg-slate-50 text-slate-400 rounded-lg">
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path></svg>
              </div>
              <div>
                <p class="text-[10px] text-slate-400 font-bold uppercase">Tipo Requerido</p>
                <p class="text-xs font-medium text-slate-700">{{ opportunity?.educationType?.name || 'Cualquier perfil' }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Matching Stats -->
        <div class="bg-slate-900 rounded-2xl shadow-xl p-6 text-white relative overflow-hidden">
          <div class="absolute -right-4 -bottom-4 opacity-10">
            <svg class="w-24 h-24" fill="currentColor" viewBox="0 0 24 24"><path d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
          </div>
          <h3 class="font-bold mb-4 flex items-center gap-2">Matching Power</h3>
          <div class="space-y-4">
            <div class="flex justify-between items-end">
              <span class="text-indigo-200 text-xs">Afinidad Media Alumnos</span>
              <span class="text-xl font-bold">84%</span>
            </div>
            <div class="w-full bg-white/10 rounded-full h-1.5">
              <div class="bg-indigo-400 h-1.5 rounded-full" style="width: 84%"></div>
            </div>
            <p class="text-[10px] text-indigo-300">Basado en el sistema de compatibilidad por keywords BeDucation.</p>
          </div>
        </div>
      </div>

      <!-- Main Column: Candidates List -->
      <div class="lg:col-span-2 space-y-4">
        <div class="flex justify-between items-center mb-2 px-2">
            <h3 class="font-bold text-slate-800">Candidatos Inscritos ({{ applications.length }})</h3>
            <div class="flex gap-2">
                <select class="text-xs bg-white border border-slate-200 rounded-lg px-3 py-1.5 font-medium text-slate-600">
                    <option>Todos los estados</option>
                    <option>Pendientes</option>
                    <option>En entrevista</option>
                </select>
            </div>
        </div>

        <div v-if="loading" class="flex justify-center py-20">
            <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-primary-600"></div>
        </div>

        <div v-else-if="applications.length === 0" class="bg-white p-16 rounded-3xl border border-dashed border-slate-200 text-center">
            <div class="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-4 text-slate-300">
                <svg class="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
            </div>
            <h4 class="text-slate-600 font-bold mb-1">Sin candidatos todavía</h4>
            <p class="text-slate-400 text-sm max-w-xs mx-auto">Esta oferta es reciente. Tan pronto como un estudiante aplique, aparecerá aquí su perfil para revisión.</p>
        </div>

        <div v-else v-for="app in applications" :key="app.id" class="bg-white rounded-3xl border border-slate-100 shadow-sm p-6 hover:shadow-md hover:border-sky-100 transition-all group overflow-hidden relative">
            <!-- Glassy background on hover -->
            <div class="absolute inset-0 bg-gradient-to-r from-sky-50/0 to-sky-50/0 group-hover:from-sky-50/20 group-hover:to-transparent transition-all pointer-events-none"></div>

            <div class="flex flex-col md:flex-row justify-between gap-6 relative z-10">
                <!-- Candidate Info -->
                <div class="flex gap-4">
                    <div class="relative">
                        <div class="w-16 h-16 rounded-2xl bg-slate-100 overflow-hidden border border-slate-200 shadow-inner">
                            <img v-if="app.student.photoS3Key" :src="app.student.photoS3Key" class="w-full h-full object-cover" />
                            <div v-else class="w-full h-full flex items-center justify-center text-slate-400 text-xl font-bold bg-slate-50">
                                {{ app.student.firstName?.charAt(0) }}{{ app.student.lastName?.charAt(0) }}
                            </div>
                        </div>
                        <div class="absolute -bottom-1 -right-1 w-7 h-7 bg-white rounded-full border-2 flex items-center justify-center text-[9px] font-black shadow-sm"
                             :class="app.compatibilityScore >= 70 ? 'text-emerald-500 border-emerald-100' : 'text-slate-400 border-slate-100'">
                            {{ app.compatibilityScore || 0 }}%
                        </div>
                    </div>
                    <div class="flex-1">
                        <h4 class="text-lg font-extrabold text-slate-800">{{ app.student.firstName }} {{ app.student.lastName }}</h4>
                        <p class="text-[10px] text-slate-400 font-bold uppercase tracking-tighter mb-2">{{ app.student.educationType?.name || 'Perfil por completar' }}</p>
                        
                        <!-- Matched Keywords Transparency -->
                        <div v-if="app.matchedKeywords" class="mb-3 p-2 bg-emerald-50/50 border border-emerald-100/50 rounded-lg">
                            <p class="text-[9px] text-emerald-700 font-black uppercase tracking-widest mb-1.5 flex items-center gap-1">
                                <svg class="w-2.5 h-2.5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M11.3 1.046A1 1 0 0112 2v5h4a1 1 0 01.82 1.573l-7 10A1 1 0 018 18v-5H4a1 1 0 01-.82-1.573l7-10a1 1 0 011.12-.38z" clip-rule="evenodd"></path></svg>
                                Matched on:
                            </p>
                            <div class="flex flex-wrap gap-1">
                                <span v-for="kw in app.matchedKeywords.split(',')" :key="kw" class="px-1.5 py-0.5 bg-white text-[9px] font-bold text-emerald-600 rounded shadow-sm border border-emerald-100">
                                    {{ kw.trim() }}
                                </span>
                            </div>
                        </div>

                        <!-- All Candidate Tags -->
                        <div class="flex flex-wrap gap-1">
                            <span v-for="kw in app.student.keywords?.slice(0,5)" :key="kw.id" class="px-2 py-0.5 bg-slate-50 text-[10px] font-bold text-slate-400 rounded">
                                {{ kw.name }}
                            </span>
                        </div>
                    </div>
                </div>

                <!-- Status & Primary Actions -->
                <div class="flex flex-col items-end gap-3 justify-center border-t md:border-t-0 md:border-l border-slate-100 pt-4 md:pt-0 md:pl-6">
                    <span class="text-[10px] font-black uppercase tracking-widest px-3 py-1 rounded-full border shadow-sm"
                          :class="getStatusClasses(app.status)">
                        {{ getStatusLabel(app.status) }}
                    </span>
                    
                    <div class="flex gap-2">
                        <button v-if="app.status === 'APPLIED'" @click="updateStatus(app.id, 'express-interest')" class="btn-primary text-xs px-4 py-2">
                            Mostrar Interés
                        </button>
                        <button v-if="app.status === 'INTERESTED'" @click="openInterviewModal(app)" class="bg-indigo-600 hover:bg-indigo-700 text-white text-xs px-4 py-2 rounded-xl font-bold transition-all shadow-lg shadow-indigo-100">
                            Agendar Entrevista
                        </button>
                        <button v-if="app.status === 'INTERVIEW_SCHEDULED' || app.status === 'INTERESTED'" @click="updateStatus(app.id, 'offer')" class="bg-emerald-500 hover:bg-emerald-600 text-white text-xs px-4 py-2 rounded-xl font-bold transition-all">
                            Enviar Oferta
                        </button>
                        <button @click="viewCv(app.student)" class="bg-slate-50 hover:bg-slate-100 text-slate-600 text-xs px-4 py-2 rounded-xl font-bold transition-all border border-slate-200">
                            Ver CV
                        </button>
                        <button v-if="!['CONFIRMED', 'REJECTED'].includes(app.status)" @click="rejectApp(app)" class="p-2 text-slate-300 hover:text-rose-500 transition-colors">
                            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                        </button>
                    </div>
                </div>
            </div>
        </div>
      </div>
    </div>

    <!-- Modal: Schedule Interview -->
    <div v-if="showInterviewModal" class="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
        <div class="bg-white rounded-3xl shadow-2xl w-full max-w-md overflow-hidden animate-in zoom-in duration-300">
            <div class="p-8">
                <h3 class="text-xl font-bold text-slate-800 mb-2">Agendar Entrevista</h3>
                <p class="text-sm text-slate-500 mb-6">Programe una reunión con <strong>{{ selectedApp?.student?.firstName }}</strong>.</p>
                
                <div class="space-y-4">
                    <div>
                        <label class="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-1.5">Fecha y Hora</label>
                        <input v-model="interviewForm.scheduledAt" type="datetime-local" class="input-field w-full" />
                    </div>
                    <div>
                        <label class="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-1.5">Enlace Videollamada</label>
                        <input v-model="interviewForm.videoCallLink" type="url" placeholder="Ej: meet.google.com/xyz" class="input-field w-full" />
                    </div>
                    <div>
                        <label class="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-1.5">Notas adicionales</label>
                        <textarea v-model="interviewForm.notes" rows="3" class="input-field w-full text-sm" placeholder="Opcional..."></textarea>
                    </div>
                </div>

                <div class="mt-8 flex gap-3">
                    <button @click="showInterviewModal = false" class="flex-1 px-4 py-2.5 text-sm font-bold text-slate-500 hover:bg-slate-50 rounded-xl transition-all">Cancelar</button>
                    <button @click="confirmInterview" :disabled="processing" class="flex-1 bg-indigo-600 text-white font-bold py-2.5 rounded-xl shadow-lg shadow-indigo-100 hover:bg-indigo-700 transition-all flex items-center justify-center gap-2">
                        <span v-if="processing" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                        Programar
                    </button>
                </div>
            </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '../../store/auth';
import api from '../../services/api';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const opportunityId = route.params.id;
const companyId = computed(() => authStore.user?.id);

const opportunity = ref(null);
const applications = ref([]);
const loading = ref(true);
const processing = ref(false);

const showInterviewModal = ref(false);
const selectedApp = ref(null);
const interviewForm = ref({
    scheduledAt: '',
    videoCallLink: 'https://meet.google.com/demo-becas',
    notes: ''
});

const fetchData = async () => {
    loading.value = true;
    try {
        const [oppRes, appsRes] = await Promise.all([
            api.get(`/opportunities/${opportunityId}`),
            api.get(`/applications/opportunity/${opportunityId}`)
        ]);
        opportunity.value = oppRes;
        applications.value = appsRes.content || appsRes || [];
    } catch (e) {
        console.error("Error fetching data:", e);
        if (e.status === 403 || e.status === 401) {
            alert("No tienes permiso para ver esta oferta.");
            router.push('/company/dashboard');
        }
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    if (authStore.user) {
        fetchData();
    }
});

const updateStatus = async (appId, stage) => {
    try {
        await api.patch(`/applications/${appId}/${stage}`, null, {
            params: { companyId: companyId.value }
        });
        await fetchData();
    } catch (e) {
        alert("Error: " + (e.message || "No se pudo actualizar el estado"));
    }
};

const openInterviewModal = (app) => {
    selectedApp.value = app;
    showInterviewModal.value = true;
};

const confirmInterview = async () => {
    if (!interviewForm.value.scheduledAt || !interviewForm.value.videoCallLink) {
        alert("Fecha y enlace obligatorios.");
        return;
    }
    processing.value = true;
    try {
        await api.post(`/applications/${selectedApp.value.id}/interview`, null, {
            params: {
                companyId: companyId.value,
                scheduledAt: interviewForm.value.scheduledAt,
                videoCallLink: interviewForm.value.videoCallLink,
                notes: interviewForm.value.notes
            }
        });
        showInterviewModal.value = false;
        await fetchData();
    } catch (e) {
        alert("Error al programar entrevista: " + e.message);
    } finally {
        processing.value = false;
    }
};

const rejectApp = async (app) => {
    const reason = prompt("Motivo del rechazo:");
    if (!reason) return;
    try {
        await api.patch(`/applications/${app.id}/reject`, null, {
            params: { companyId: companyId.value, reason }
        });
        await fetchData();
    } catch (e) {
        alert("Error al rechazar: " + e.message);
    }
};

const viewCv = (student) => {
    if (student.cvS3Key) {
        window.open(student.cvS3Key, '_blank');
    } else {
        alert("El estudiante no ha subido su CV aún.");
    }
};

const formatDate = (dateStr) => {
    if (!dateStr) return '---';
    return new Date(dateStr).toLocaleDateString();
};

const getStatusLabel = (status) => {
    const labels = {
        'APPLIED': 'Nueva Aplicación',
        'INTERESTED': 'Interesados',
        'INTERVIEW_SCHEDULED': 'Entrevista',
        'OFFERED': 'Oferta Enviada',
        'ADMIN_APPROVED': 'Pendiente Admin',
        'CONFIRMED': 'Confirmado',
        'REJECTED': 'Rechazado',
        'OFFER_REJECTED': 'Oferta Rehusada'
    };
    return labels[status] || status;
};

const getStatusClasses = (status) => {
    return {
        'bg-sky-50 text-sky-600 border-sky-100': status === 'APPLIED',
        'bg-indigo-50 text-indigo-600 border-indigo-100': status === 'INTERESTED',
        'bg-amber-50 text-amber-600 border-amber-100': status === 'INTERVIEW_SCHEDULED',
        'bg-emerald-50 text-emerald-600 border-emerald-100': status === 'CONFIRMED' || status === 'OFFERED',
        'bg-rose-50 text-rose-600 border-rose-100': status === 'REJECTED' || status === 'OFFER_REJECTED'
    };
};
</script>
