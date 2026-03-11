<template>
  <div class="space-y-6">
    <!-- Brand / Header -->
    <div class="flex justify-between items-center py-4 border-b border-slate-200">
        <div>
           <h1 class="text-2xl font-bold tracking-tight text-slate-800">Pipeline Comercial: Mis Ofertas Erasmus</h1>
           <p class="text-sm text-slate-500 mt-1">Busque y seleccione el talento que su startup o compañía necesita.</p>
        </div>
        <button class="btn-primary flex items-center gap-2 bg-slate-900 hover:bg-slate-800">
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
            Crear Oferta de Vacante (DRAFT)
        </button>
    </div>

    <!-- Stats & Filters -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
         <div class="glass-card p-4 rounded-xl border border-slate-100 flex flex-col justify-center">
             <span class="text-xs text-slate-400 font-bold uppercase tracking-wider mb-1">Candidatos Activos (Etapa 2)</span>
             <span class="text-3xl font-extrabold text-slate-800">12</span>
         </div>
         <div class="glass-card p-4 rounded-xl border border-slate-100 flex flex-col justify-center">
             <span class="text-xs text-slate-400 font-bold uppercase tracking-wider mb-1">Entrevistas Agendadas</span>
             <span class="text-3xl font-extrabold text-slate-800">3</span>
         </div>
    </div>

    <!-- Job Postings Listado Maestro -->
    <div class="mt-8 space-y-4">
        <h3 class="font-bold text-slate-800">Tus ofertas publicadas</h3>
        
        <div v-for="opp in opportunities" :key="opp.id" class="bg-white border text-left border-slate-200 rounded-2xl p-6 shadow-sm hover:shadow-md transition-shadow relative overflow-hidden group">
            <!-- Barra de estado lateral estetica -->
            <div :class="{'bg-green-500': opp.status === 'APPROVED', 'bg-slate-300': opp.status === 'DRAFT', 'bg-amber-400': opp.status === 'PENDING_REVIEW' }" 
                 class="absolute left-0 top-0 bottom-0 w-1.5 opacity-80 group-hover:w-2 transition-all"></div>
            
            <div class="flex justify-between items-start mb-4">
                <div class="pl-2">
                     <h4 class="text-xl font-bold text-slate-900">{{ opp.title }}</h4>
                     <p class="text-sm text-slate-500 font-medium">
                         {{ opp.city }}, {{ opp.country }} · {{ opp.spotsAvailable }}/{{ opp.spots }} Vacantes Libres
                     </p>
                </div>
                <div class="flex flex-col items-end gap-2">
                    <span class="font-bold text-xs uppercase tracking-wider px-2 py-1 rounded bg-slate-100 border border-slate-200"
                          :class="{'bg-emerald-50 text-emerald-700 border-emerald-200': opp.status === 'APPROVED'}">
                        {{ opp.status }}
                    </span>
                </div>
            </div>

            <!-- Dashboard de Acción Rápida (Estudiantes) -->
            <div class="pl-2 mt-4 pt-4 border-t border-slate-100 flex gap-4">
                <button v-if="opp.status === 'APPROVED'" class="text-sm text-primary-600 font-bold hover:text-primary-700 underline underline-offset-4">
                    👁 Evaluar y Revisar Candidatos 
                    <span class="ml-1 bg-primary-100 text-primary-800 rounded-full px-1.5 py-0.5 text-[10px]">{{ opp.applicationsCount || 5 }} App</span>
                </button>
                <button v-if="opp.status === 'DRAFT'" class="text-sm text-amber-600 font-bold hover:text-amber-700 underline underline-offset-4">
                    Enviar a Publicación (Requires Admin review)
                </button>
                <button class="text-sm text-slate-500 font-bold hover:text-slate-700 underline underline-offset-4">
                    Editar
                </button>
            </div>
        </div>
        
        <div v-if="opportunities.length === 0" class="p-12 text-center text-slate-400 border-2 border-dashed border-slate-200 rounded-2xl">
            Aún no has creado ofertas de prácticas. Clica en el botón superior derecho.
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '../../store/auth';

const authStore = useAuthStore();
const opportunities = ref([]);

onMounted(() => {
    // Simulación de respuesta GET /companies/{companyId}/opportunities 
    setTimeout(() => {
        opportunities.value = [
            { id: 1, title: 'Beca Full Stack Web Developer (Node/React)', country: 'Ireland', city: 'Dublin', spots: 2, spotsAvailable: 1, status: 'APPROVED', applicationsCount: 8 },
            { id: 2, title: 'Prácticas Marketing Digital Erasmus+', country: 'España', city: 'Madrid', spots: 1, spotsAvailable: 1, status: 'PENDING_REVIEW', applicationsCount: 0 }
        ];
    }, 500);
});
</script>
