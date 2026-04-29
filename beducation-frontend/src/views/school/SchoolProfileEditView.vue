<template>
  <div class="max-w-4xl mx-auto py-8">
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-slate-800">Editar Perfil Institucional</h1>
        <p class="text-sm text-slate-500 mt-1">Actualice la información pública y de contacto de su institución.</p>
      </div>
      <router-link to="/school/dashboard" class="text-sm font-medium text-slate-500 hover:text-slate-800 flex items-center gap-2 transition-colors">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>
        Volver al Panel
      </router-link>
    </div>

    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
      <form @submit.prevent="saveProfile" class="p-6 md:p-8 space-y-6">
        
        <!-- Información Básica -->
        <section>
          <h2 class="text-lg font-bold text-slate-800 mb-4 border-b border-slate-100 pb-2">Información Básica</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Nombre de la Institución</label>
              <input v-model="profile.name" type="text" class="input-field" placeholder="Ej: IES Ejemplo" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Tipo de Institución</label>
              <select v-model="profile.institutionType" class="input-field">
                <option value="PUBLIC">Pública</option>
                <option value="PRIVATE">Privada</option>
                <option value="CONCERTADA">Concertada</option>
              </select>
            </div>
            <div class="md:col-span-2">
              <label class="block text-sm font-medium text-slate-700 mb-1">Descripción / Sobre nosotros</label>
              <textarea v-model="profile.description" rows="4" class="input-field resize-none" placeholder="Breve descripción de su institución educativa..."></textarea>
            </div>
          </div>
        </section>

        <!-- Contacto -->
        <section>
          <h2 class="text-lg font-bold text-slate-800 mb-4 border-b border-slate-100 pb-2 mt-8">Información de Contacto</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Email de Contacto</label>
              <input v-model="profile.contactEmail" type="email" class="input-field" placeholder="contacto@institucion.es" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Teléfono</label>
              <input v-model="profile.phone" type="tel" class="input-field" placeholder="+34 900 000 000" />
            </div>
            <div class="md:col-span-2">
              <label class="block text-sm font-medium text-slate-700 mb-1">Dirección Completa</label>
              <input v-model="profile.address" type="text" class="input-field" placeholder="Calle Ejemplo 123, Ciudad, Código Postal" />
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Sitio Web</label>
              <input v-model="profile.website" type="url" class="input-field" placeholder="https://www.institucion.es" />
            </div>
          </div>
        </section>

        <!-- Acciones -->
        <div class="pt-6 border-t border-slate-100 flex justify-end gap-3 mt-4">
          <router-link to="/school/dashboard" class="px-5 py-2.5 text-sm font-medium text-slate-600 hover:bg-slate-50 rounded-lg transition-colors border border-slate-200">
            Cancelar
          </router-link>
          <button type="submit" :disabled="isSaving" class="btn-primary px-6 py-2.5 flex items-center gap-2 text-sm">
            <span v-if="isSaving" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
            <svg v-else class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
            {{ isSaving ? 'Guardando...' : 'Guardar Cambios' }}
          </button>
        </div>

        <!-- Mensaje de éxito -->
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
 * LÓGICA: Edición de Perfil Institucional
 * --------------------------------------
 * Permite a los gestores de centros educativos actualizar su información
 * pública, datos de contacto y tipo de institución.
 */
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '../../services/api';
import { useNotificationStore } from '../../store/notifications';

const router = useRouter();
const notifications = useNotificationStore();
const isSaving = ref(false);     // Estado de carga durante el guardado
const saveSuccess = ref(false);   // Control de la notificación de éxito

const profile = ref({
  id: null,
  name: '',
  institutionType: 'PUBLIC',
  description: '',
  contactEmail: '',
  phone: '',
  address: '',
  website: '',
  contactPerson: ''
});

/**
 * Carga inicial de datos de la escuela logueada.
 * Utiliza el endpoint especial '/schools/me'.
 */
onMounted(async () => {
  try {
    const res = await api.get('/schools/me');
    profile.value = { ...res };
  } catch (e) {
    notifications.error("No se pudo cargar la información del perfil.");
  }
});

/**
 * Persiste los cambios en el backend.
 */
const saveProfile = async () => {
  if (!profile.value.id && profile.value.id !== 0) {
      notifications.error("Error: No se encontró el ID de la escuela.");
      return;
  }

  isSaving.value = true;
  saveSuccess.value = false;
  
  try {
    // El backend espera un objeto SchoolProfileDto
    await api.put(`/schools/${profile.value.id}`, profile.value);
    
    notifications.success("Perfil institucional actualizado correctamente.");
    saveSuccess.value = true;
    setTimeout(() => {
      saveSuccess.value = false;
      router.push('/school/dashboard');
    }, 1500);
  } catch (error) {
    console.error("Error al guardar perfil institucional:", error);
    notifications.error(error.message || "Hubo un error al guardar el perfil.");
  } finally {
    isSaving.value = false;
  }
};
</script>


