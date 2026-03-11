<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header Admin -->
    <div class="bg-indigo-900 text-indigo-50 p-6 md:p-8 rounded-2xl flex flex-col sm:flex-row items-center justify-between gap-6 shadow-xl relative overflow-hidden">
      <!-- Glow effect -->
      <div class="absolute -right-20 -top-20 w-64 h-64 bg-indigo-500 rounded-full blur-3xl opacity-30 pointer-events-none"></div>

      <div class="z-10">
        <h2 class="text-3xl font-extrabold tracking-tight">Panel Administrativo Global</h2>
        <p class="text-indigo-200 mt-2 font-medium flex items-center gap-2">
            <svg class="w-4 h-4 text-emerald-400" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path></svg>
            Sistemas BeDucation en línea
        </p>
      </div>

      <div class="z-10 bg-indigo-800 p-3 rounded-lg flex items-center gap-4">
        <div class="text-center px-4">
            <span class="block text-2xl font-bold text-white">{{ stats.activeSchools }}</span>
            <span class="text-xs text-indigo-300 uppercase">Escuelas</span>
        </div>
        <div class="w-px h-8 bg-indigo-600"></div>
        <div class="text-center px-4">
            <span class="block text-2xl font-bold text-white">{{ stats.activeStudents }}</span>
            <span class="text-xs text-indigo-300 uppercase">Alumnos</span>
        </div>
      </div>
    </div>

    <!-- Tabs Content -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 p-4">
      <!-- Custom simple tabs -->
      <div class="flex border-b border-slate-200 overflow-x-auto">
        <button v-for="tab in tabs" :key="tab.id" @click="currentTab = tab.id"
                class="px-6 py-4 font-medium text-sm transition-colors whitespace-nowrap border-b-2"
                :class="currentTab === tab.id ? 'border-primary-600 text-primary-600' : 'border-transparent text-slate-500 hover:text-slate-800 hover:border-slate-300'">
          {{ tab.label }}
          <span v-if="tab.badge > 0" class="ml-2 bg-rose-500 text-white text-xs px-2 py-0.5 rounded-full font-bold">
            {{ tab.badge }}
          </span>
        </button>
      </div>

      <!-- Control PENDING_SCHOOLS -->
      <div v-show="currentTab === 'escuelas'" class="p-6">
        <h3 class="text-lg font-bold text-slate-800 mb-4">Aprobación de Nuevas Instituciones Educativas</h3>
        
        <div class="overflow-x-auto rounded-xl border border-slate-200">
            <table class="w-full text-sm text-left text-slate-600">
                <thead class="bg-slate-50 text-slate-800 uppercase text-xs border-b border-slate-200">
                    <tr>
                        <th class="px-6 py-4">Institución</th>
                        <th class="px-6 py-4">Contacto</th>
                        <th class="px-6 py-4">Ubicación</th>
                        <th class="px-6 py-4 text-right">Acciones (Admin)</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-if="pendingSchools.length === 0">
                        <td colspan="4" class="px-6 py-8 text-center text-slate-500">No hay colegios pendientes de aprobación (Stage 1).</td>
                    </tr>
                    <tr v-for="school in pendingSchools" :key="school.id" class="border-b border-slate-100 hover:bg-slate-50">
                        <td class="px-6 py-4 font-medium text-slate-900">{{ school.name }}</td>
                        <td class="px-6 py-4">{{ school.contactPerson }}<br><span class="text-xs text-slate-400">{{ school.email }}</span></td>
                        <td class="px-6 py-4">{{ school.city }}, {{ school.country }}</td>
                        <td class="px-6 py-4 text-right flex justify-end gap-2">
                            <button @click="processEntity('school', school.id, true)" class="bg-emerald-100 hover:bg-emerald-200 text-emerald-700 px-3 py-1.5 rounded-md font-medium text-xs transition-colors">Aprobar Inst.</button>
                            <button @click="processEntity('school', school.id, false)" class="bg-rose-100 hover:bg-rose-200 text-rose-700 px-3 py-1.5 rounded-md font-medium text-xs transition-colors">Rechazar</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
      </div>
      
      <!-- Panel de Gestión de Contenido Restante Placeholder -->
      <div v-show="currentTab !== 'escuelas'" class="p-6 text-center text-slate-500">
        <p>Sistema modular activo: {{ tabs.find(t=>t.id === currentTab).label }} en construcción.</p>
        <p class="text-xs mt-2 bg-slate-100 inline-block px-3 py-1 rounded">Los endpoints del Backend Spring Boot ya están listos en AdminController.java</p>
      </div>
      
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '../../services/api';

const currentTab = ref('escuelas');
const tabs = ref([
    { id: 'escuelas', label: 'Verificar Escuelas', badge: 2 },
    { id: 'empresas', label: 'Verificar Empresas', badge: 0 },
    { id: 'ofertas', label: 'Control Calidad Ofertas', badge: 5 },
    { id: 'placements', label: 'Validación Convenio Stage 4', badge: 1 },
    { id: 'diccionario', label: 'Mantenimiento Keywords DB', badge: 0 }
]);

const stats = ref({ activeSchools: 145, activeStudents: 3042 });
const pendingSchools = ref([]);

onMounted(async () => {
   // Simulación de carga inicial.
   // En código real llamaríamos a api.get('/admin/pending-schools') 
   setTimeout(() => {
       pendingSchools.value = [
           { id: 101, name: 'Universidad Loyola', contactPerson: 'Dirección RRII', email: 'internacional@uloyola.es', city: 'Sevilla', country: 'España' },
           { id: 102, name: 'TUM - Technical University Munich', contactPerson: 'Hans Bach', email: 'international@tum.de', city: 'Munich', country: 'Germany' }
       ];
   }, 800);
});

const processEntity = async (type, id, approve) => {
    // Ejemplo de interaccion REST de Rechazo/Aprobación administrativa
    alert(`Enviando POST /admin/${type}s/${id}/${approve ? 'approve' : 'reject'} a SpringBoot API...`);
    // Tras la respuesta de API, sacar del array visual.
    if(type === 'school') pendingSchools.value = pendingSchools.value.filter(s => s.id !== id);
};
</script>
