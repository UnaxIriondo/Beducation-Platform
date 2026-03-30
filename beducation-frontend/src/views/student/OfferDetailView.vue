<template>
  <div class="max-w-5xl mx-auto space-y-6 animate-fade-in pb-12">
    <!-- Back Button -->
    <button @click="$router.back()" class="flex items-center gap-2 text-slate-500 hover:text-primary-600 transition-colors font-medium group">
      <svg class="w-5 h-5 transition-transform group-hover:-translate-x-1" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
      Volver al listado
    </button>

    <div v-if="loading" class="space-y-6">
      <div class="h-64 bg-slate-200 animate-pulse rounded-3xl"></div>
      <div class="h-40 bg-slate-200 animate-pulse rounded-3xl"></div>
    </div>

    <div v-else-if="offer" class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- Main Content -->
      <div class="lg:col-span-2 space-y-6">
        <!-- Header Card -->
        <div class="glass-card p-8 rounded-3xl border-l-8 border-primary-500 shadow-xl shadow-primary-900/5">
          <div class="flex justify-between items-start mb-6">
            <div>
              <span class="inline-block px-3 py-1 bg-primary-50 text-primary-700 text-xs font-bold rounded-full mb-3 uppercase tracking-wider">
                {{ offer.country }} · {{ offer.city }}
              </span>
              <h1 class="text-4xl font-extrabold text-slate-900 tracking-tight leading-tight">
                {{ offer.title }}
              </h1>
              <p class="text-xl text-slate-500 mt-2 font-medium">
                {{ offer.company?.name || 'Empresa Colaboradora' }}
              </p>
            </div>
            <div class="w-20 h-20 bg-slate-50 rounded-2xl border border-slate-100 flex items-center justify-center overflow-hidden shrink-0 shadow-inner">
               <img v-if="offer.company?.logoS3Key" :src="offer.company.logoS3Key" class="w-full h-full object-cover">
               <span v-else class="text-2xl font-bold text-slate-300">{{ offer.company?.name?.charAt(0) || 'B' }}</span>
            </div>
          </div>

          <div class="flex flex-wrap gap-4 text-sm font-medium text-slate-600 border-t border-slate-100 pt-6">
            <div class="flex items-center gap-2">
              <svg class="w-5 h-5 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 00-2 2z" /></svg>
              <span>Inicio: {{ offer.startDate ? new Date(offer.startDate).toLocaleDateString() : 'A convenir' }}</span>
            </div>
            <div class="flex items-center gap-2">
              <svg class="w-5 h-5 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" /></svg>
              <span>{{ offer.spotsAvailable }} plazas restantes</span>
            </div>
            <div class="flex items-center gap-2">
               <span class="w-2 h-2 bg-emerald-500 rounded-full animate-pulse"></span>
               <span class="text-emerald-600 font-bold uppercase tracking-widest text-[10px]">Active</span>
            </div>
          </div>
        </div>

        <!-- Description -->
        <div class="glass-card p-8 rounded-3xl space-y-6">
          <h2 class="text-2xl font-bold text-slate-800 flex items-center gap-3">
             <span class="w-1.5 h-8 bg-primary-500 rounded-full"></span>
             Descripción de la vacante
          </h2>
          <div class="prose prose-slate max-w-none text-slate-600 leading-relaxed text-lg whitespace-pre-wrap">
            {{ offer.description }}
          </div>

          <h2 class="text-2xl font-bold text-slate-800 pt-10 flex items-center gap-3 border-t border-slate-100">
             <span class="w-1.5 h-8 bg-emerald-500 rounded-full"></span>
             Sobre la Empresa
          </h2>
          <p class="text-slate-600 leading-relaxed">
            {{ offer.company?.description || 'Esta empresa colabora con BeDucation ofreciendo programas internacionales de alta calidad.' }}
          </p>
        </div>
      </div>

      <!-- Sidebar Actions -->
      <div class="space-y-6">
        <div class="glass-card p-6 rounded-3xl sticky top-8 bg-white/80 backdrop-blur-xl border border-white shadow-2xl shadow-slate-200">
          <h3 class="text-lg font-bold text-slate-800 mb-4">¿Te interesa esta posición?</h3>
          <p class="text-sm text-slate-500 mb-6">Al aplicar, tu perfil completo y CV serán enviados al manager de talento de la empresa.</p>
          
          <div v-if="hasApplied" class="bg-emerald-50 border border-emerald-100 p-4 rounded-2xl text-center">
            <svg class="w-12 h-12 text-emerald-500 mx-auto mb-2" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            <p class="text-emerald-800 font-bold">¡Solicitud Enviada!</p>
            <p class="text-xs text-emerald-600 mt-1">Sigue el progreso en tu dashboard.</p>
          </div>

          <button 
            v-else
            @click="applyToOffer"
            :disabled="applying || !authStore.user?.profileComplete"
            class="w-full btn-primary py-4 rounded-2xl text-lg font-extrabold shadow-lg shadow-primary-500/30 flex items-center justify-center gap-2 disabled:bg-slate-300 disabled:shadow-none transition-all hover:scale-[1.02]"
          >
            <span v-if="applying" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
            {{ applying ? 'Enviando...' : 'Aplicar ahora' }}
          </button>

          <p v-if="!authStore.user?.profileComplete" class="text-xs text-rose-500 mt-4 text-center font-medium">
             Debes completar tu perfil y CV primero para poder aplicar.
          </p>
        </div>

        <div class="glass-card p-6 rounded-3xl bg-slate-900 shadow-xl">
           <h4 class="text-white font-bold mb-3 flex items-center gap-2">
              <svg class="w-5 h-5 text-primary-400" fill="currentColor" viewBox="0 0 20 20"><path d="M10 2a6 6 0 00-6 6v3.586l-.707.707A1 1 0 004 14h12a1 1 0 00.707-1.707L16 11.586V8a6 6 0 00-6-6zM10 18a3 3 0 01-3-3h6a3 3 0 01-3 3z"/></svg>
              Consejo de BeDucation
           </h4>
           <p class="text-slate-400 text-sm leading-relaxed">
             "Asegúrate de tener tus preferencias de destino alineadas con el país de la oferta para obtener un Match Score superior."
           </p>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-20">
      <h2 class="text-2xl font-bold text-slate-400">Oferta no encontrada</h2>
      <button @click="$router.push('/student/dashboard')" class="text-primary-600 mt-4 underline">Volver al Dashboard</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '../../store/auth';
import api from '../../services/api';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const offer = ref(null);
const loading = ref(true);
const applying = ref(false);
const hasApplied = ref(false);

const checkExistingApplication = async () => {
    try {
        const appsRes = await api.get(`/applications/student/${authStore.user.id}`);
        const apps = appsRes.content || [];
        hasApplied.value = apps.some(a => a.opportunity.id === parseInt(route.params.id));
    } catch (e) {
        console.warn("Could not check existing applications", e);
    }
}

onMounted(async () => {
    const offerId = route.params.id;
    try {
        const res = await api.get(`/opportunities/${offerId}`);
        offer.value = res;
        await checkExistingApplication();
    } catch (e) {
        console.error("Error loading offer:", e);
    } finally {
        loading.value = false;
    }
});

const applyToOffer = async () => {
    if (!authStore.user?.id) return;
    
    applying.value = true;
    try {
        await api.post('/applications', null, {
            params: {
                studentId: authStore.user.id,
                opportunityId: offer.value.id
            }
        });
        hasApplied.value = true;
        alert("¡Tu solicitud se ha enviado con éxito! Puedes seguir el estado en tu Dashboard.");
    } catch (e) {
        alert("Error al aplicar: " + (e.message || "Error desconocido"));
    } finally {
        applying.value = false;
    }
};
</script>
