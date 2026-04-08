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

      <!-- ANALYTICS FUNNEL VIEW -->
      <div v-show="currentTab === 'analiticas'" class="p-6">
        <h3 class="text-xl font-bold text-slate-800 mb-6">Pipeline Global de Selección</h3>
        
        <div class="max-w-4xl mx-auto space-y-4">
            <!-- Funnel Stages -->
            <div v-for="(stage, index) in funnelStages" :key="stage.key" 
                 class="relative flex items-center group">
                <!-- Label & Bar -->
                <div class="w-40 text-right pr-4 text-xs font-bold text-slate-400 uppercase tracking-widest">{{ stage.label }}</div>
                <div class="flex-1 bg-slate-50 rounded-xl h-14 border border-slate-100 relative overflow-hidden flex items-center px-4">
                    <div class="absolute inset-y-0 left-0 transition-all duration-1000 ease-out"
                         :style="{ width: getPercentage(stage.key) + '%', backgroundColor: stage.color }"></div>
                    <span class="relative z-10 font-black text-slate-800">{{ funnelData[stage.key] || 0 }}</span>
                    <span class="relative z-10 ml-auto font-bold text-[10px] text-slate-400 opacity-0 group-hover:opacity-100 transition-opacity">
                        {{ getPercentage(stage.key) }}% del total
                    </span>
                </div>
                <!-- Connector arrow (except last) -->
                <div v-if="index < funnelStages.length - 1" class="absolute left-60 -bottom-3 z-20 text-slate-200">
                    <svg class="h-4 w-4" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M14.707 12.707a1 1 0 01-1.414 0L10 9.414l-3.293 3.293a1 1 0 01-1.414-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 010 1.414z" clip-rule="evenodd" transform="rotate(180 10 10)"></path></svg>
                </div>
            </div>
        </div>

        <div class="mt-12 grid grid-cols-1 md:grid-cols-3 gap-6">
            <div class="p-6 bg-emerald-50 rounded-2xl border border-emerald-100">
                <p class="text-[10px] font-black text-emerald-600 uppercase mb-1">Confirmaciones Totales</p>
                <p class="text-3xl font-black text-emerald-800">{{ funnelData['CONFIRMED'] || 0 }}</p>
                <p class="text-xs text-emerald-600 mt-1">Acuerdos finalizados con éxito.</p>
            </div>
            <div class="p-6 bg-rose-50 rounded-2xl border border-rose-100">
                <p class="text-[10px] font-black text-rose-600 uppercase mb-1">Candidaturas Rechazadas</p>
                <p class="text-3xl font-black text-rose-800">{{ funnelData['REJECTED'] || 0 }}</p>
                <p class="text-xs text-rose-600 mt-1">Incluye descartes de empresas y admin.</p>
            </div>
        </div>
      </div>

      <!-- Panel VERIFICAR (Combinado) -->
      <div v-show="currentTab === 'escuelas'" class="p-6">
        <h3 class="text-lg font-bold text-slate-800 mb-4">Nuevas Solicitudes de Registro (Stage 1)</h3>
        
        <!-- Escuelas Pendientes -->
        <div v-if="pendingSchools.length > 0" class="mb-8">
            <h4 class="text-sm font-semibold text-slate-500 uppercase mb-3 px-2">Instituciones Educativas</h4>
            <div class="overflow-x-auto rounded-xl border border-slate-200">
                <table class="w-full text-sm text-left text-slate-600">
                    <tbody class="bg-white">
                        <tr v-for="school in pendingSchools" :key="school.id" class="border-b border-slate-100 hover:bg-slate-50">
                            <td class="px-6 py-4">
                                <div class="font-medium text-slate-900">{{ school.name }}</div>
                                <div class="text-xs text-slate-400">{{ school.user?.email }}</div>
                            </td>
                            <td class="px-6 py-4">{{ school.city }}, {{ school.country }}</td>
                            <td class="px-6 py-4 text-right flex justify-end gap-2">
                                <button @click="processEntity('school', school.id, true)" class="bg-emerald-100 hover:bg-emerald-200 text-emerald-700 px-3 py-1.5 rounded-md font-medium text-xs transition-colors">Aprobar</button>
                                <button @click="processEntity('school', school.id, false)" class="bg-rose-100 hover:bg-rose-200 text-rose-700 px-3 py-1.5 rounded-md font-medium text-xs transition-colors">Rechazar</button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Empresas Pendientes -->
        <div v-if="pendingCompanies.length > 0">
            <h4 class="text-sm font-semibold text-slate-500 uppercase mb-3 px-2">Empresas Receptoras</h4>
            <div class="overflow-x-auto rounded-xl border border-slate-200">
                <table class="w-full text-sm text-left text-slate-600">
                    <tbody class="bg-white">
                        <tr v-for="company in pendingCompanies" :key="company.id" class="border-b border-slate-100 hover:bg-slate-50">
                            <td class="px-6 py-4">
                                <div class="font-medium text-slate-900">{{ company.name }}</div>
                                <div class="text-xs text-slate-400">{{ company.user?.email }}</div>
                            </td>
                            <td class="px-6 py-4">{{ company.sector }}</td>
                            <td class="px-6 py-4 text-right flex justify-end gap-2">
                                <button @click="processEntity('company', company.id, true)" class="bg-emerald-100 hover:bg-emerald-200 text-emerald-700 px-3 py-1.5 rounded-md font-medium text-xs transition-colors">Aprobar</button>
                                <button @click="processEntity('company', company.id, false)" class="bg-rose-100 hover:bg-rose-200 text-rose-700 px-3 py-1.5 rounded-md font-medium text-xs transition-colors">Rechazar</button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <div v-if="pendingSchools.length === 0 && pendingCompanies.length === 0" class="text-center py-12 text-slate-400">
            No hay solicitudes urgentes pendientes de validación.
        </div>
      </div>

      <!-- GESTIÓN GLOBAL: ESTUDIANTES -->
      <div v-show="currentTab === 'control_estudiantes'" class="p-6">
        <div class="flex justify-between items-center mb-6">
            <h3 class="text-lg font-bold text-slate-800">Base de Datos de Estudiantes</h3>
            <button @click="showInviteForm = !showInviteForm" class="bg-primary-600 hover:bg-primary-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-all flex items-center gap-2">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" /></svg>
                Invitar Alumno (Admin)
            </button>
        </div>

        <!-- Formulario de Invitación -->
        <div v-if="showInviteForm" class="bg-slate-50 p-6 rounded-xl border border-slate-200 mb-8 animate-in fade-in slide-in-from-top-4 duration-300">
            <h4 class="text-sm font-bold text-slate-700 mb-4">Nueva Invitación Administrativa</h4>
            <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
                <input v-model="inviteForm.firstName" type="text" placeholder="Nombre" class="bg-white border text-sm rounded-lg p-2.5 focus:ring-primary-500">
                <input v-model="inviteForm.lastName" type="text" placeholder="Apellidos" class="bg-white border text-sm rounded-lg p-2.5 focus:ring-primary-500">
                <input v-model="inviteForm.email" type="email" placeholder="Email Invitación" class="bg-white border text-sm rounded-lg p-2.5 focus:ring-primary-500">
                <select v-model="inviteForm.schoolId" class="bg-white border text-sm rounded-lg p-2.5">
                    <option value="">-- Seleccionar Escuela --</option>
                    <option v-for="s in allSchoolsList" :key="s.id" :value="s.id">{{ s.name }}</option>
                </select>
            </div>
            <div class="mt-4 flex justify-end gap-3">
                <button @click="showInviteForm = false" class="text-slate-500 text-sm font-medium">Cancelar</button>
                <button @click="submitInvitation" class="bg-primary-600 text-white px-6 py-2 rounded-lg text-sm font-bold">Enviar Invitación</button>
            </div>
        </div>

        <div class="overflow-x-auto rounded-xl border border-slate-200">
            <table class="w-full text-sm text-left text-slate-600">
                <thead class="bg-slate-50 text-slate-800 uppercase text-xs border-b">
                    <tr>
                        <th class="px-6 py-4">Estudiante</th>
                        <th class="px-6 py-4">Escuela / Origen</th>
                        <th class="px-6 py-4">Estado</th>
                        <th class="px-6 py-4 text-right">Acciones</th>
                    </tr>
                </thead>
                <tbody class="bg-white">
                    <tr v-for="std in allStudents" :key="std.id" class="border-b border-slate-100">
                        <td class="px-6 py-4">
                            <div class="font-medium text-slate-900">{{ std.firstName }} {{ std.lastName }}</div>
                            <div class="text-xs text-slate-400">{{ std.invitationEmail }}</div>
                        </td>
                        <td class="px-6 py-4 text-xs">{{ std.school?.name || '---' }}</td>
                        <td class="px-6 py-4">
                            <span :class="std.user?.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'" class="px-2 py-1 rounded text-[10px] font-bold">
                                {{ std.user?.status || 'SIN CUENTA' }}
                            </span>
                        </td>
                        <td class="px-6 py-4 text-right flex justify-end gap-2">
                             <button @click="updateUserStatus(std.user?.id, std.user?.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE')" class="text-slate-400 hover:text-rose-600 transition-colors" title="Baneo/Activar">
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728L5.636 5.636" /></svg>
                             </button>
                             <button @click="deleteUser(std.user?.id)" class="text-slate-400 hover:text-red-600" title="Eliminar definitivamente">
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                             </button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
      </div>

      <!-- GESTIÓN GLOBAL: ESCUELAS -->
      <div v-show="currentTab === 'control_escuelas'" class="p-6">
        <h3 class="text-lg font-bold text-slate-800 mb-4">Base de Datos de Instituciones</h3>
        <div class="overflow-x-auto rounded-xl border border-slate-200">
            <table class="w-full text-sm text-left text-slate-600">
                <thead class="bg-slate-50 text-slate-800 uppercase text-xs border-b">
                    <tr>
                        <th class="px-6 py-4">Escuela</th>
                        <th class="px-6 py-4">Ubicación</th>
                        <th class="px-6 py-4 text-right">Acciones</th>
                    </tr>
                </thead>
                <tbody class="bg-white">
                    <tr v-for="item in allSchools" :key="item.id" class="border-b border-slate-100">
                        <td class="px-6 py-4">
                            <div class="font-medium text-slate-900">{{ item.name }}</div>
                            <div class="text-xs text-slate-400">{{ item.user?.email }}</div>
                        </td>
                        <td class="px-6 py-4 text-xs">{{ item.city }}, {{ item.country }}</td>
                        <td class="px-6 py-4 text-right flex justify-end gap-2">
                             <button @click="deleteUser(item.user?.id)" class="bg-rose-50 text-rose-600 p-2 rounded hover:bg-rose-100 transition-colors text-xs font-bold">Eliminar Inst.</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
      </div>

      <!-- GESTIÓN GLOBAL: EMPRESAS -->
      <div v-show="currentTab === 'control_empresas'" class="p-6">
        <h3 class="text-lg font-bold text-slate-800 mb-4">Base de Datos de Empresas</h3>
        <div class="overflow-x-auto rounded-xl border border-slate-200">
            <table class="w-full text-sm text-left text-slate-600">
                <thead class="bg-slate-50 text-slate-800 uppercase text-xs border-b">
                    <tr>
                        <th class="px-6 py-4">Empresa</th>
                        <th class="px-6 py-4">Categoría</th>
                        <th class="px-6 py-4 text-right">Acciones</th>
                    </tr>
                </thead>
                <tbody class="bg-white">
                    <tr v-for="item in allCompanies" :key="item.id" class="border-b border-slate-100">
                        <td class="px-6 py-4">
                            <div class="font-medium text-slate-900">{{ item.name }}</div>
                            <a :href="item.website" target="_blank" class="text-xs text-primary-600">{{ item.user?.email }}</a>
                        </td>
                        <td class="px-6 py-4 text-xs">{{ item.sector }}</td>
                        <td class="px-6 py-4 text-right flex justify-end gap-2">
                             <button @click="deleteUser(item.user?.id)" class="bg-rose-50 text-rose-600 p-2 rounded hover:bg-rose-100 transition-colors text-xs font-bold">Eliminar Empresa</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
      </div>

      <!-- PANEL PLACEMENTS (Stage 4) -->
      <div v-show="currentTab === 'placements'" class="p-6">
        <h3 class="text-lg font-bold text-slate-800 mb-4">Acuerdos Pendientes de Validación Admin (Stage 4)</h3>
        
        <div v-if="pendingPlacements.length > 0" class="overflow-x-auto rounded-xl border border-slate-200">
            <table class="w-full text-sm text-left text-slate-600">
                <thead class="bg-slate-50 text-slate-800 uppercase text-xs border-b">
                    <tr>
                        <th class="px-6 py-4">Estudiante</th>
                        <th class="px-6 py-4">Oferta / Empresa</th>
                        <th class="px-6 py-4">Fase</th>
                        <th class="px-6 py-4 text-right">Acciones</th>
                    </tr>
                </thead>
                <tbody class="bg-white">
                    <tr v-for="app in pendingPlacements" :key="app.id" class="border-b border-slate-100 hover:bg-slate-50">
                        <td class="px-6 py-4">
                            <div class="font-medium text-slate-900">{{ app.student?.firstName }} {{ app.student?.lastName }}</div>
                            <div class="text-xs text-slate-400">{{ app.student?.invitationEmail }}</div>
                        </td>
                        <td class="px-6 py-4">
                            <div class="font-medium text-indigo-600">{{ app.opportunity?.title }}</div>
                            <div class="text-xs text-slate-500">{{ app.opportunity?.city }}, {{ app.opportunity?.country }}</div>
                        </td>
                        <td class="px-6 py-4">
                            <span class="bg-amber-100 text-amber-700 px-2 py-1 rounded text-[10px] font-bold">ST-ACCEPTED</span>
                        </td>
                        <td class="px-6 py-4 text-right flex justify-end gap-2">
                            <button @click="processPlacement(app.id, true)" class="bg-emerald-500 hover:bg-emerald-600 text-white px-3 py-1.5 rounded-md font-bold text-xs shadow-sm transition-all">Validar para Escuela</button>
                            <button @click="processPlacement(app.id, false)" class="text-rose-600 hover:bg-rose-50 px-3 py-1.5 rounded-md font-medium text-xs transition-all border border-rose-100">Rechazar</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
        <div v-else class="text-center py-12 text-slate-400">
            No hay convenios en espera de validación administrativa.
        </div>
      </div>

      <!-- Otros (Mantenimiento de secciones antiguas) -->
      <div v-show="currentTab === 'ofertas'" class="p-12 text-center text-slate-400">
          <svg class="w-16 h-16 mx-auto mb-4 opacity-20" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
          <p>Módulo de auditoría de {{ tabs.find(t=>t.id === currentTab).label }} activado en el Backend.</p>
          <p class="text-xs mt-2">Usa las nuevas pestañas de Gestión para controlar los perfiles actuales.</p>
      </div>
      
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import api from '../../services/api';

const currentTab = ref('escuelas');
const tabs = ref([
    { id: 'analiticas', label: 'Analíticas Globales', badge: 0 },
    { id: 'escuelas', label: 'Verificar Nuevos', badge: 0 },
    { id: 'control_estudiantes', label: 'Gestión Estudiantes', badge: 0 },
    { id: 'control_escuelas', label: 'Gestión Escuelas', badge: 0 },
    { id: 'control_empresas', label: 'Gestión Empresas', badge: 0 },
    { id: 'placements', label: 'Convenios Stage 4', badge: 0 }
]);

const stats = ref({ activeSchools: 0, activeStudents: 0 });
const funnelData = ref({});
const funnelStages = [
    { key: 'APPLIED', label: 'Aplicaciones', color: '#f1f5f9' },
    { key: 'INTERESTED', label: 'Interesados', color: '#e0f2fe' },
    { key: 'OFFERED', label: 'Ofertas Realizadas', color: '#fef3c7' },
    { key: 'STUDENT_ACCEPTED', label: 'Aceptado Alumno', color: '#dcfce7' },
    { key: 'ADMIN_APPROVED', label: 'Validado Admin', color: '#ecfdf5' },
    { key: 'CONFIRMED', label: 'Confirmados', color: '#10b981' }
];

const pendingSchools = ref([]);
const pendingCompanies = ref([]);
const pendingPlacements = ref([]);

const allSchoolsList = ref([]);
const allSchools = ref([]);
const allCompanies = ref([]);
const allStudents = ref([]);

const showInviteForm = ref(false);
const inviteForm = ref({ firstName: '', lastName: '', email: '', schoolId: '' });

const fetchDashboardData = async () => {
    try {
        const statsRes = await api.get('/admin/stats');
        stats.value = {
            activeSchools: statsRes.totalSchools || 0,
            activeStudents: statsRes.totalStudents || 0
        };
        funnelData.value = statsRes.funnel || {};
        
        tabs.value[1].badge = (statsRes.pendingSchoolsCount || 0) + (statsRes.pendingCompaniesCount || 0);
        tabs.value[5].badge = statsRes.pendingStage4Count || 0;

        if (currentTab.value === 'escuelas') {
            const schoolsRes = await api.get('/admin/pending-schools');
            pendingSchools.value = schoolsRes.content || [];
            const compRes = await api.get('/admin/pending-companies');
            pendingCompanies.value = compRes.content || [];
        } else if (currentTab.value === 'control_escuelas') {
            const res = await api.get('/admin/all-schools');
            allSchools.value = res.content || [];
        } else if (currentTab.value === 'control_empresas') {
            const res = await api.get('/admin/all-companies');
            allCompanies.value = res.content || [];
        } else if (currentTab.value === 'control_estudiantes') {
            const res = await api.get('/admin/all-students');
            allStudents.value = res.content || [];
            if (allSchoolsList.value.length === 0) {
                const schoolsRes = await api.get('/admin/all-schools', { params: { size: 1000 } });
                allSchoolsList.value = schoolsRes.content || [];
            }
        } else if (currentTab.value === 'placements') {
            const res = await api.get('/admin/pending-placements');
            pendingPlacements.value = res.content || [];
        }
    } catch (err) {
        console.error("Error cargando Dashboard:", err);
    }
};

const getPercentage = (key) => {
    const total = funnelData.value['APPLIED'] || 1;
    const value = funnelData.value[key] || 0;
    return Math.round((value / total) * 100);
};

onMounted(fetchDashboardData);
watch(currentTab, fetchDashboardData);

const submitInvitation = async () => {
    if (!inviteForm.value.email || !inviteForm.value.schoolId) {
        alert("Email y Escuela son obligatorios.");
        return;
    }
    try {
        await api.post('/admin/students/invite', null, { params: inviteForm.value });
        alert("Invitación enviada con éxito.");
        showInviteForm.value = false;
        inviteForm.value = { firstName: '', lastName: '', email: '', schoolId: '' };
        fetchDashboardData();
    } catch (err) {
        alert(err.message || "Error al invitar al alumno.");
    }
};

const updateUserStatus = async (userId, status) => {
    if (!userId) return;
    if (!confirm(`¿Seguro que quieres cambiar el estado de este usuario a ${status}?`)) return;
    try {
        await api.patch(`/admin/users/${userId}/status`, null, { params: { status } });
        fetchDashboardData();
    } catch (err) {
        alert("Error al actualizar el usuario.");
    }
};

const deleteUser = async (userId) => {
    if (!userId) return;
    if (!confirm("¿ESTÁS SEGURO? Esta acción es irreversible y borrará toda la ficha y accesos del usuario.")) return;
    try {
        await api.delete(`/admin/users/${userId}`);
        fetchDashboardData();
    } catch (err) {
        alert("Error al eliminar el usuario.");
    }
};

const processPlacement = async (appId, approve) => {
    try {
        await api.patch(`/admin/applications/${appId}/approve-stage4`, null, {
            params: { approve }
        });
        fetchDashboardData();
    } catch (err) {
        alert("Error al procesar el placement.");
    }
};

const processEntity = async (type, id, approve) => {
    try {
        const endpoint = type === 'company' ? 'companies' : `${type}s`;
        await api.patch(`/admin/${endpoint}/${id}/process`, null, {
            params: { approve }
        });
        fetchDashboardData();
    } catch (err) {
        alert("Error al procesar la solicitud.");
    }
};
</script>
