<template>
  <div class="space-y-6">
    <div class="flex flex-col md:flex-row justify-between items-start md:items-center p-6 bg-white rounded-2xl shadow-sm border border-slate-200">
      <div>
        <h1 class="text-2xl font-bold text-slate-800">Mi Panel Institucional</h1>
        <p class="text-slate-500">Gestione alumnos, invitaciones y aprobaciones de prácticas en el extranjero.</p>
      </div>
      <div class="mt-4 md:mt-0 space-x-3">
        <button @click="showInviteModal = true" class="btn-secondary text-sm px-4 py-2">
          + Nuevo Alumno (1 a 1)
        </button>
        <button @click="fileInput.click()" class="btn-primary text-sm px-4 py-2 flex items-center gap-2 inline-flex">
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"></path></svg>
          Importar estudiantes (.csv)
        </button>
        <!-- Oculto para disparar click via button -->
        <input type="file" ref="fileInput" @change="uploadCsv" accept=".csv" class="hidden" />
      </div>
    </div>

    <!-- Pestañas: Alumnos (Perfil | Track App) & Validation (Etapa 4->5) -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <div class="bg-white p-4 rounded-xl border border-rose-100 flex items-center justify-between shadow-sm">
            <div>
                 <p class="text-sm text-slate-500 font-medium">Validación Final</p>
                 <p class="text-2xl font-bold text-slate-800">1</p>
            </div>
            <div class="bg-rose-50 text-rose-500 p-2 rounded-lg">
                <svg class="h-6 w-6" fill="currentColor" viewBox="0 0 24 24"><path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            </div>
        </div>
        <div class="bg-white p-4 rounded-xl border border-sky-100 flex items-center justify-between shadow-sm">
             <div>
                 <p class="text-sm text-slate-500 font-medium">Estudiantes Registrados (Totales)</p>
                 <p class="text-2xl font-bold text-slate-800">34</p>
            </div>
        </div>
    </div>

    <!-- Lista de Estudiantes -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
        <div class="p-6 border-b border-slate-200 bg-slate-50/50 flex justify-between items-center">
            <h3 class="text-lg font-bold text-slate-800">Directorio de Alumnos</h3>
        </div>
        <div class="overflow-x-auto">
            <table class="w-full text-sm text-left text-slate-600">
                <thead class="bg-slate-50 text-slate-800 uppercase text-[10px] sm:text-xs">
                    <tr>
                        <th class="px-6 py-4 font-bold border-b border-slate-200">Nombre / Email</th>
                        <th class="px-6 py-4 font-bold border-b border-slate-200">Tipo Edu.</th>
                        <th class="px-6 py-4 font-bold border-b border-slate-200 text-center">Perfil Completo</th>
                        <th class="px-6 py-4 font-bold text-right border-b border-slate-200">Acciones</th>
                    </tr>
                </thead>
                <tbody>
                     <tr v-for="std in students" :key="std.id" class="border-b border-slate-100 hover:bg-sky-50 transition-colors">
                         <td class="px-6 py-4 flex flex-col">
                             <span class="font-bold text-slate-800">{{ std.firstName }} {{ std.lastName }}</span>
                             <span class="text-xs text-slate-400 font-medium">{{ std.invitationEmail }}</span>
                         </td>
                         <td class="px-6 py-4">
                             {{ std.educationType?.code || 'Pte. Onboarding' }}
                         </td>
                         <td class="px-6 py-4 text-center">
                              <span v-if="std.profileComplete" class="inline-flex items-center gap-1 text-emerald-600 bg-emerald-50 px-2 py-1 rounded text-xs font-bold border border-emerald-200">
                                  <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"></path></svg> Sí
                              </span>
                              <span v-else class="text-slate-400 bg-slate-100 px-2 py-1 rounded text-xs">Falta CV/Datos</span>
                         </td>
                         <td class="px-6 py-4 text-right">
                             <button class="text-sky-600 font-medium hover:underline text-xs mr-3">Ver Progreso (Funnel)</button>
                             <button class="text-slate-400 hover:text-rose-500 font-medium text-xs">Eliminar</button>
                         </td>
                     </tr>
                </tbody>
            </table>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '../../store/auth';
import api from '../../services/api';

const authStore = useAuthStore();
const students = ref([]);
const fileInput = ref(null);
const showInviteModal = ref(false);

const mockSchoolId = 20;

onMounted(() => {
    // Simulamos la respuesta de GET /schools/{id}/students paginada
    students.value = [
        { id: 1, firstName: 'Lucía', lastName: 'García', invitationEmail: 'alu.1@ies.es', profileComplete: true, educationType: { code: 'FP_HIGH' } },
        { id: 2, firstName: 'Carlos', lastName: 'López', invitationEmail: 'alu.2@ies.es', profileComplete: false, educationType: null }
    ];
});

const uploadCsv = async (event) => {
    const file = event.target.files[0];
    if (!file) return;
    
    // Validar tipo mimetype CSV básico
    if (file.type !== "text/csv" && file.name.split('.').pop() !== 'csv') {
        alert("El archivo debe ser un .csv válido.");
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    try {
        const res = await api.post(`/schools/${mockSchoolId}/import-students`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
        alert(`Éxito. Se importaron e invitaron ${res.length} alumnos.`);
        window.location.reload();
    } catch (e) {
        alert(`Error procesando CSV: ${e.message}`);
    } finally {
        // Limpiamos la caché del file input para permitir re subidas
        event.target.value = null; 
    }
};
</script>
