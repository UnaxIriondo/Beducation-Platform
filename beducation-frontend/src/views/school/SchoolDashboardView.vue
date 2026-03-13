<template>
  <div class="space-y-6">
    <div class="flex flex-col md:flex-row justify-between items-start md:items-center p-6 bg-white rounded-2xl shadow-sm border border-slate-200">
      <div>
        <h1 class="text-2xl font-bold text-slate-800">Mi Panel Institucional</h1>
        <p class="text-slate-500">Gestione alumnos, invitaciones y aprobaciones de prácticas en el extranjero.</p>
      </div>
      <div class="mt-4 md:mt-0 space-x-3 flex items-center">
        <button @click="openInviteModal" class="btn-secondary text-sm px-4 py-2 flex items-center gap-2">
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path></svg>
          + Nuevo Alumno (1 a 1)
        </button>
        <button @click="fileInput.click()" class="btn-primary text-sm px-4 py-2 flex items-center gap-2">
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
                             <button @click="openFunnelModal(std)" class="text-sky-600 font-medium hover:underline text-xs mr-3">Ver Progreso (Funnel)</button>
                             <button @click="deleteStudent(std.id)" class="text-slate-400 hover:text-rose-500 font-medium text-xs">Eliminar</button>
                         </td>
                     </tr>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Modal Invitación 1 a 1 -->
    <div v-if="showInviteModal" class="fixed inset-0 z-50 bg-slate-900/50 flex items-center justify-center p-4">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden">
        <div class="px-6 py-4 border-b border-slate-100 flex justify-between items-center">
          <h3 class="text-lg font-bold text-slate-800">Invitar Alumno (1 a 1)</h3>
          <button @click="closeInviteModal" class="text-slate-400 hover:text-slate-600 transition-colors">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
          </button>
        </div>
        <form @submit.prevent="submitInvite" class="p-6 space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Nombre</label>
              <input v-model="newStudent.firstName" type="text" required class="input-field" placeholder="Ej: Laura" />
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Apellidos</label>
              <input v-model="newStudent.lastName" type="text" required class="input-field" placeholder="Ej: Gómez" />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Correo Electrónico</label>
            <input v-model="newStudent.email" type="email" required class="input-field" placeholder="alumno@institucion.es" />
          </div>
          <div>
             <label class="block text-sm font-medium text-slate-700 mb-1">Tipo de Educación (Opcional)</label>
             <select v-model="newStudent.educationType" class="input-field">
                 <option value="">Seleccionar (Se define en onboarding)</option>
                 <option value="FP_HIGH">FP Grado Superior</option>
                 <option value="UNIVERSITY">Grado Universitario</option>
                 <option value="BOOTCAMP">Bootcamp</option>
             </select>
          </div>
          <div class="pt-4 flex justify-end gap-3">
            <button type="button" @click="closeInviteModal" class="px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50 rounded-lg transition-colors border border-slate-200">Cancelar</button>
            <button type="submit" :disabled="isInviting" class="btn-primary text-sm px-6 py-2 flex items-center gap-2">
              <span v-if="isInviting" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
              {{ isInviting ? 'Enviando...' : 'Enviar Invitación' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Progreso (Funnel) -->
    <div v-if="showFunnelModal" class="fixed inset-0 z-50 bg-slate-900/50 flex items-center justify-center p-4">
       <div class="bg-white rounded-2xl shadow-xl w-full max-w-2xl overflow-hidden">
         <div class="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-slate-50">
            <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full bg-sky-100 flex items-center justify-center text-sky-600 font-bold text-lg">
                    {{ (selectedStudent?.firstName?.charAt(0) || '') + (selectedStudent?.lastName?.charAt(0) || '') }}
                </div>
                <div>
                   <h3 class="text-lg font-bold text-slate-800">{{ selectedStudent?.firstName }} {{ selectedStudent?.lastName }}</h3>
                   <p class="text-xs text-slate-500">{{ selectedStudent?.invitationEmail }} · {{ selectedStudent?.educationType?.code || 'Pte. Onboarding' }}</p>
                </div>
            </div>
            <button @click="showFunnelModal = false" class="text-slate-400 hover:text-slate-600 bg-white shadow-sm border border-slate-200 rounded-full p-1 transition-colors">
                 <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
            </button>
         </div>
         <div class="p-8">
             <h4 class="text-sm font-bold text-slate-700 uppercase mb-6 tracking-wide">Funnel del Estudiante</h4>
             <div class="relative">
                 <!-- Tracking Line -->
                 <div class="absolute left-[19px] top-6 bottom-4 w-0.5 bg-slate-100"></div>
                 
                 <div class="space-y-6 relative">
                     <!-- Step 1 -->
                     <div class="flex items-start gap-4">
                         <div class="flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center relative z-10" :class="selectedStudent?.profileComplete ? 'bg-emerald-500 text-white' : 'bg-emerald-100 text-emerald-600'">
                             <svg v-if="selectedStudent?.profileComplete" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                             <span v-else class="text-sm font-bold">1</span>
                         </div>
                         <div>
                             <h4 class="text-md font-bold text-slate-800">Registro & Onboarding</h4>
                             <p class="text-sm text-slate-500">{{ selectedStudent?.profileComplete ? 'Completado. CV y perfil listos.' : 'Pendiente o incompleto.' }}</p>
                         </div>
                     </div>
                     <!-- Step 2 -->
                     <div class="flex items-start gap-4">
                         <div class="flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center relative z-10" :class="selectedStudent?.profileComplete ? 'bg-sky-500 text-white' : 'bg-slate-100 text-slate-400'">
                             <span class="text-sm font-bold">2</span>
                         </div>
                         <div>
                             <h4 class="text-md font-bold" :class="selectedStudent?.profileComplete ? 'text-slate-800' : 'text-slate-400'">Test de Idiomas / Habilidades</h4>
                             <p class="text-sm" :class="selectedStudent?.profileComplete ? 'text-slate-500' : 'text-slate-400'">Resultados: B2 Inglés validado.</p>
                         </div>
                     </div>
                     <!-- Step 3 -->
                     <div class="flex items-start gap-4">
                         <div class="flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center relative z-10 bg-amber-400 text-white shadow-md ring-4 ring-amber-50">
                             <span class="text-sm font-bold">3</span>
                         </div>
                         <div>
                             <h4 class="text-md font-bold text-slate-800">Candidaturas / Entrevistas</h4>
                             <p class="text-sm text-slate-500">2 Entrevistas agendadas con empresas en Irlanda y UK.</p>
                             <span class="mt-2 inline-block px-2 py-1 bg-amber-100 text-amber-700 text-xs font-bold rounded-md border border-amber-200">En proceso</span>
                         </div>
                     </div>
                     <!-- Step 4 -->
                     <div class="flex items-start gap-4">
                         <div class="flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center relative z-10 bg-slate-100 text-slate-400">
                             <span class="text-sm font-bold">4</span>
                         </div>
                         <div>
                             <h4 class="text-md font-bold text-slate-400">Acuerdo Creado / Aceptado</h4>
                             <p class="text-sm text-slate-400">Firma del Learning Agreement pendiente.</p>
                         </div>
                     </div>
                 </div>
             </div>
         </div>
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
const isInviting = ref(false);

const newStudent = ref({
    firstName: '',
    lastName: '',
    email: '',
    educationType: ''
});

// Modal Funnel
const showFunnelModal = ref(false);
const selectedStudent = ref(null);

const mockSchoolId = 20;

onMounted(() => {
    // Simulamos la respuesta de GET /schools/{id}/students paginada
    students.value = [
        { id: 1, firstName: 'Lucía', lastName: 'García', invitationEmail: 'alu.1@ies.es', profileComplete: true, educationType: { code: 'FP_HIGH' } },
        { id: 2, firstName: 'Carlos', lastName: 'López', invitationEmail: 'alu.2@ies.es', profileComplete: false, educationType: null }
    ];
});

const openInviteModal = () => {
    showInviteModal.value = true;
};

const closeInviteModal = () => {
    showInviteModal.value = false;
    newStudent.value = { firstName: '', lastName: '', email: '', educationType: '' };
};

const submitInvite = async () => {
    isInviting.value = true;
    try {
        // Simulación API si es mock o real
        // const res = await api.post(`/schools/${mockSchoolId}/invite-student`, newStudent.value);
        await new Promise(r => setTimeout(r, 800)); // Simulando carga
        
        // Agregar estudiante a la lista local para ver el efecto inmediatamente
        students.value.push({
            id: Date.now(),
            firstName: newStudent.value.firstName,
            lastName: newStudent.value.lastName,
            invitationEmail: newStudent.value.email,
            profileComplete: false,
            educationType: newStudent.value.educationType ? { code: newStudent.value.educationType } : null
        });
        
        closeInviteModal();
    } catch (e) {
        alert(`Error al invitar alumno: ${e.message}`);
    } finally {
        isInviting.value = false;
    }
};

const deleteStudent = async (studentId) => {
    if (!confirm('¿Estás seguro de que deseas eliminar a este alumno del directorio? Esta acción no se puede deshacer.')) return;
    try {
        // Simulación de borrado backend
        // await api.delete(`/schools/${mockSchoolId}/students/${studentId}`);
        await new Promise(r => setTimeout(r, 400));
        students.value = students.value.filter(s => s.id !== studentId);
    } catch (e) {
        alert(`Error al eliminar alumno: ${e.message}`);
    }
};

const openFunnelModal = (student) => {
    selectedStudent.value = student;
    showFunnelModal.value = true;
};

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
