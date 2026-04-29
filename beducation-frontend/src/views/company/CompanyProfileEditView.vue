<template>
  <div class="max-w-4xl mx-auto py-8">
    <!-- Cabecera de la página -->
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-slate-800">Editar Perfil de Empresa</h1>
        <p class="text-sm text-slate-500 mt-1">Actualice la información de su organización para los estudiantes.</p>
      </div>
      <!-- Enlace para volver al dashboard de empresa -->
      <router-link to="/company/dashboard" class="text-sm font-medium text-slate-500 hover:text-slate-800 flex items-center gap-2 transition-colors">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>
        Volver al Panel
      </router-link>
    </div>

    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
      <!-- Formulario de edición -->
      <form @submit.prevent="saveProfile" class="p-6 md:p-8 space-y-6">
        
        <!-- SECCIÓN: Información Corporativa -->
        <section>
          <h2 class="text-lg font-bold text-slate-800 mb-4 border-b border-slate-100 pb-2">Información Corporativa</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Nombre de la Empresa</label>
              <input v-model="profile.name" type="text" class="input-field" placeholder="Ej: Tech Solutions S.L." required />
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Sector / Industria</label>
              <input v-model="profile.sector" type="text" class="input-field" placeholder="Ej: Tecnología, Salud, Marketing..." />
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Tamaño de la Empresa</label>
              <select v-model="profile.companySize" class="input-field">
                <option value="1-10">1-10 empleados</option>
                <option value="11-50">11-50 empleados</option>
                <option value="51-200">51-200 empleados</option>
                <option value="201-500">201-500 empleados</option>
                <option value="500+">Más de 500 empleados</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Sitio Web</label>
              <input v-model="profile.website" type="url" class="input-field" placeholder="https://www.empresa.com" />
            </div>
            <div class="md:col-span-2">
              <label class="block text-sm font-medium text-slate-700 mb-1">Descripción de la Empresa</label>
              <textarea v-model="profile.description" rows="4" class="input-field resize-none" placeholder="Cuéntanos sobre tu empresa, cultura y qué buscas en los estudiantes..."></textarea>
            </div>
          </div>
        </section>

        <!-- SECCIÓN: Ubicación -->
        <section>
          <h2 class="text-lg font-bold text-slate-800 mb-4 border-b border-slate-100 pb-2 mt-8">Ubicación</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">País</label>
              <input v-model="profile.country" type="text" class="input-field" placeholder="Ej: España" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Ciudad</label>
              <input v-model="profile.city" type="text" class="input-field" placeholder="Ej: Madrid" />
            </div>
          </div>
        </section>

        <!-- ACCIONES: Botones de cancelar y guardar -->
        <div class="pt-6 border-t border-slate-100 flex justify-end gap-3 mt-4">
          <router-link to="/company/dashboard" class="px-5 py-2.5 text-sm font-medium text-slate-600 hover:bg-slate-50 rounded-lg transition-colors border border-slate-200">
            Cancelar
          </router-link>
          <button type="submit" :disabled="isSaving" class="btn-primary px-6 py-2.5 flex items-center gap-2 text-sm">
            <span v-if="isSaving" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
            <svg v-else class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
            {{ isSaving ? 'Guardando...' : 'Guardar Cambios' }}
          </button>
        </div>

        <!-- NOTIFICACIÓN: Mensaje de éxito tras el guardado -->
        <div v-if="saveSuccess" class="mt-4 p-4 bg-emerald-50 border border-emerald-200 text-emerald-700 rounded-xl text-sm flex items-center gap-2">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path></svg>
          Perfil actualizado correctamente.
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
/**
 * Lógica de la Vista de Edición de Perfil de Empresa
 * ------------------------------------------------
 * Este componente permite a la empresa autenticada recuperar sus datos actuales
 * y enviarlos al backend para su actualización.
 */
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '../../services/api';
import { useNotificationStore } from '../../store/notifications';

const router = useRouter();
const notifications = useNotificationStore();
const isSaving = ref(false); // Estado para controlar el spinner del botón
const saveSuccess = ref(false); // Estado para mostrar el mensaje de éxito

// Objeto reactivo que contiene los campos del perfil
const profile = ref({
  id: null,
  name: '',
  sector: '',
  companySize: '1-10',
  website: '',
  description: '',
  country: '',
  city: '',
  email: '' // El email suele venir del objeto de usuario relacionado
});

/**
 * Carga inicial de datos al montar el componente.
 * Se obtiene el perfil de la empresa que ha iniciado sesión mediante el endpoint '/companies/me'.
 */
onMounted(async () => {
  try {
    const res = await api.get('/companies/me');
    profile.value = { 
        ...res,
        email: res.user?.email || '' // Mapeamos el email desde el objeto user
    };
  } catch (e) {
    notifications.error("No se pudo cargar la información del perfil corporativo.");
  }
});

/**
 * Función para enviar los cambios al servidor.
 * Transforma el objeto local en el DTO que espera el backend.
 */
const saveProfile = async () => {
  // Validación básica de ID
  if (!profile.value.id && profile.value.id !== 0) {
      notifications.error("Error crítico: No se encontró el identificador de la empresa.");
      return;
  }

  isSaving.value = true;
  saveSuccess.value = false;
  
  try {
    // Mapeo al DTO (CompanyRegistrationDto) esperado por el controlador backend
    const dto = {
        name: profile.value.name,
        country: profile.value.country,
        city: profile.value.city,
        website: profile.value.website,
        description: profile.value.description,
        companySize: profile.value.companySize,
        sector: profile.value.sector,
        email: profile.value.email,
        auth0Id: profile.value.user?.auth0Id || 'placeholder'
    };

    await api.put(`/companies/${profile.value.id}`, dto);
    
    notifications.success("Perfil corporativo actualizado correctamente.");
    saveSuccess.value = true;
    setTimeout(() => {
      saveSuccess.value = false;
      router.push('/company/dashboard');
    }, 1500);
  } catch (error) {
    notifications.error("Hubo un error al guardar el perfil institucional.");
  } finally {
    isSaving.value = false;
  }
};
</script>


