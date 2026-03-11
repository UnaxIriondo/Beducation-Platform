<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Student Header Welcome -->
    <div class="glass-card p-6 md:p-8 rounded-2xl flex flex-col md:flex-row items-center justify-between gap-6 border-l-4 border-l-sky-500">
      <div>
        <h2 class="text-3xl font-extrabold text-slate-800 tracking-tight">
          Hola, {{ userProfile?.firstName || authStore.user?.name || 'Estudiante' }} 👋
        </h2>
        <p class="text-slate-600 mt-2 font-medium">
          Dashboard Universitario · Fase: <span class="font-bold text-sky-600 px-2 bg-sky-50 rounded-md">Buscando Prácticas</span>
        </p>
      </div>

      <div class="flex flex-col sm:flex-row gap-3 w-full md:w-auto">
        <button @click="$router.push('/student/onboarding')" class="btn-secondary whitespace-nowrap">
          Editar Perfil y CV
        </button>
        <button class="btn-primary flex items-center gap-2 justify-center whitespace-nowrap bg-sky-600 hover:bg-sky-700">
          <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
          Buscar Ofertas
        </button>
      </div>
    </div>
    
    <!-- AI Suggestion & MatchMaker Alert -->
    <div v-if="aiMatches.length > 0" class="bg-gradient-to-r from-emerald-50 to-primary-50 border border-emerald-100 p-6 rounded-2xl shadow-sm relative overflow-hidden">
        <div class="absolute -right-10 -bottom-10 opacity-10">
            <svg class="w-48 h-48 text-emerald-600" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/></svg>
        </div>
        <h3 class="text-xl font-bold text-slate-800 flex items-center gap-2">
            <span class="bg-emerald-500 rounded-full w-3 h-3 animate-pulse"></span>
            Algoritmo T-Matching BeDucation
        </h3>
        <p class="text-slate-600 mt-1 mb-4">Basado en tus skills y destino (70-20-10 match), tenemos ofertas altamente compatibles para ti.</p>
        
        <!-- Carrusel/Grilla matches -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div v-for="match in aiMatches.slice(0,3)" :key="match.opportunity.id" class="bg-white p-4 rounded-xl border border-slate-200 hover-scale cursor-pointer group">
                <div class="flex justify-between items-start mb-2">
                    <span class="text-xs font-bold px-2 py-1 bg-emerald-100 text-emerald-800 rounded-full">{{ Math.round(match.score * 100) }}% Comaptibilidad</span>
                    <span class="text-xs text-slate-400 border border-slate-200 px-2 rounded">{{ match.opportunity.country }}</span>
                </div>
                <h4 class="font-bold text-slate-800 group-hover:text-primary-600 transition-colors">{{ match.opportunity.title }}</h4>
                <!-- Placeholder Empresa -->
                <p class="text-sm text-slate-500 mt-1">Empresa Tech SL</p>
                <div class="mt-4 flex gap-1 flex-wrap">
                     <!-- Etiquetas random -->
                     <span class="text-xs bg-slate-100 text-slate-600 px-2 py-1 rounded">Java</span>
                     <span class="text-xs bg-slate-100 text-slate-600 px-2 py-1 rounded">Inglés</span>
                </div>
            </div>
        </div>
    </div>


    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Estadísticas de Solicitudes (Fila 2) -->
      <div class="lg:col-span-2 glass-card rounded-2xl p-6">
        <h3 class="text-lg font-bold text-slate-800 mb-4 border-b border-slate-100 pb-3">Estado de mis candidaturas (Funnel)</h3>
        
        <!-- Falsa tabla/estado -->
        <div v-if="loadingApps" class="animate-pulse bg-slate-100 h-20 rounded-xl mb-3"></div>
        <div v-else-if="applications.length === 0" class="text-center py-10 bg-slate-50 rounded-xl border border-slate-200 border-dashed">
            <svg class="w-12 h-12 text-slate-300 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path></svg>
            <p class="text-slate-500 font-medium">No has postulado a ninguna oferta todavía.</p>
            <button @click="$router.push('/student/search')" class="mt-4 text-primary-600 hover:text-primary-700 font-medium underline underline-offset-4">Explorar catálogo</button>
        </div>
        <div v-else class="space-y-4">
            <!-- Iterate over applications -->
            <div v-for="app in applications" :key="app.id" class="border border-slate-200 rounded-xl p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white hover:border-slate-300 transition-colors">
                <div>
                    <h4 class="font-bold text-slate-800">{{ app.opportunity.title }}</h4>
                    <p class="text-sm text-slate-500">{{ app.opportunity.country }} · Aplicado: {{ new Date(app.createdAt).toLocaleDateString() }}</p>
                </div>
                <!-- Status Badge -->
                <div class="shrink-0 flex items-center gap-3">
                    <span :class="getStatusClasses(app.status)" class="px-3 py-1 text-xs font-bold rounded-full">
                        {{ app.status }}
                    </span>
                    <!-- Action Buttons basadas en workflow -->
                    <button v-if="app.status === 'OFFERED'" @click="acceptOffer(app.id)" class="bg-emerald-100 text-emerald-700 hover:bg-emerald-200 px-3 py-1 font-bold text-xs rounded transition-colors">
                        ACEPTAR OFERTA
                    </button>
                </div>
            </div>
        </div>
      </div>

      <!-- Tareas Pendientes o Sidebar Info -->
      <div class="glass-card rounded-2xl p-6 bg-slate-800 text-white shadow-xl shadow-slate-900/10">
        <h3 class="text-lg font-bold mb-4 flex items-center gap-2">
           <svg class="h-5 w-5 text-amber-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
           Avisos del Sistema
        </h3>
        <ul class="space-y-4 relative">
          <!-- Line background -->
          <div class="absolute left-1.5 top-2 bottom-2 w-0.5 bg-slate-700"></div>

          <li class="relative pl-6">
            <span class="absolute left-0 top-1.5 w-3 h-3 bg-red-500 rounded-full ring-4 ring-slate-800"></span>
            <p class="text-sm font-medium">Sube tu CV Europass</p>
            <p class="text-xs text-slate-400 mt-1">Obligatorio para que los managers estudien tu caso.</p>
          </li>
          <li class="relative pl-6">
            <span class="absolute left-0 top-1.5 w-3 h-3 bg-emerald-500 rounded-full ring-4 ring-slate-800"></span>
            <p class="text-sm font-medium">Completar 5 variables Onboarding</p>
            <p class="text-xs text-slate-400 mt-1">Realizado con éxito. Ya puedes aplicar.</p>
          </li>
           <li class="relative pl-6">
            <span class="absolute left-0 top-1.5 w-3 h-3 bg-amber-500 rounded-full ring-4 ring-slate-800"></span>
            <p class="text-sm font-medium">Entrevista de Prueba SL</p>
            <p class="text-xs text-slate-400 mt-1">Hoy a las 16:00 h vía Zoom (Stage 2).</p>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '../../store/auth';
import { ref, onMounted } from 'vue';

const authStore = useAuthStore();
const applications = ref([]);
const loadingApps = ref(true);
const aiMatches = ref([]);

// Simulation objects para previsualizar render
onMounted(() => {
    
    // Simulate API fetch delay
    setTimeout(() => {
        aiMatches.value = [
            { score: 0.94, opportunity: { id: 10, title: 'Junior Data Analyst Intern', country: 'Germany' } },
            { score: 0.81, opportunity: { id: 11, title: 'Software Developer Trainee', country: 'France' } }
        ];

        applications.value = [
            { id: 1, opportunity: { title: 'Marketing Executive', country: 'Italy' }, status: 'APPLIED', createdAt: '2026-03-01T10:00:00Z' },
            { id: 2, opportunity: { title: 'Backend Node.js Dev', country: 'Spain' }, status: 'OFFERED', createdAt: '2026-02-15T10:00:00Z' },
            { id: 3, opportunity: { title: 'Admin Specialist', country: 'Germany' }, status: 'REJECTED', createdAt: '2026-01-20T10:00:00Z' }
        ];
        loadingApps.value = false;
    }, 1500);
});

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
