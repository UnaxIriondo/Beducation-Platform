<template>
  <div class="max-w-2xl mx-auto py-10">
    <div class="glass-card p-8 rounded-2xl">
      <h2 class="text-3xl font-bold text-slate-800 mb-6">Registra tu Escuela en BeDucation</h2>
      
      <!-- Stepper / Info visual -->
      <div class="mb-8 p-4 bg-sky-50 rounded-lg border border-sky-100 flex gap-4 items-start">
        <svg class="w-6 h-6 text-sky-500 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
        <p class="text-sm text-sky-800">
          Este formulario es el paso inicial. Tras enviarlo quedarás en estado <span class="font-bold">PENDING</span> 
          hasta que un administrador apruebe la validez legal de tu institución educativa.
        </p>
      </div>

      <form @submit.prevent="submitForm" class="space-y-6">
        
        <!-- Grid de dos columnas para campos compactos -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="col-span-1 md:col-span-2">
            <label class="block mb-2 text-sm font-medium text-slate-700">Nombre Oficial del Centro</label>
            <input v-model="form.name" type="text" required class="input-field" placeholder="P.ej. IES Oretania" />
          </div>

          <div>
            <label class="block mb-2 text-sm font-medium text-slate-700">Email de Contacto</label>
            <input v-model="form.email" type="email" required class="input-field" placeholder="info@iesoretania.es" />
          </div>

          <div>
            <label class="block mb-2 text-sm font-medium text-slate-700">País</label>
            <input v-model="form.country" type="text" required class="input-field" placeholder="España" />
          </div>
          
          <div>
            <label class="block mb-2 text-sm font-medium text-slate-700">Ciudad</label>
            <input v-model="form.city" type="text" class="input-field" placeholder="Linares" />
          </div>

          <div>
            <label class="block mb-2 text-sm font-medium text-slate-700">Persona de Contacto</label>
            <input v-model="form.contactPerson" type="text" required class="input-field" placeholder="Juan López (Director CE)" />
          </div>
          
          <div class="col-span-1 md:col-span-2">
            <label class="block mb-2 text-sm font-medium text-slate-700">ID de Auth0 pre-logeado</label>
            <input v-model="form.auth0Id" type="text" required class="input-field bg-slate-100 text-slate-500 cursor-not-allowed" readonly title="ID de Autenticación Central"/>
          </div>
        </div>

        <button type="submit" :disabled="loading" class="btn-primary w-full mt-4 flex items-center justify-center gap-2">
          <svg v-if="loading" class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          {{ loading ? 'Enviando solicitud...' : 'Solicitar Acceso a BeDucation' }}
        </button>

        <p v-if="error" class="text-rose-500 text-sm mt-2 text-center">{{ error }}</p>
        <p v-if="success" class="text-emerald-600 text-sm mt-2 text-center font-medium bg-emerald-50 px-3 py-2 rounded-lg">
          ¡Registrado exitosamente! En breve el departamento lo comprobará y aprobará.
        </p>

      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useAuth0 } from '@auth0/auth0-vue';
import api from '../../services/api';

const { user, isAuthenticated, loginWithRedirect } = useAuth0();

const form = ref({
  name: '', email: '', country: '', city: '', contactPerson: '', auth0Id: ''
});
const loading = ref(false);
const error = ref('');
const success = ref(false);

onMounted(() => {
  // Para registrar una escuela en BeDucation, la idea de la arquitectura híbrida
  // es que el usuario se loguee a Auth0 PRIMERO, y luego lo redirigimos aquí.
  if (!isAuthenticated.value) {
    // Alertar 
    alert("Para registrar tu escuela, primero iniciaremos proceso con Google/Identidad Central Auth0.");
    loginWithRedirect({ appState: { targetUrl: '/register/school' } });
  } else {
    // Rellenamos datos que sí conocemos del proveedor Social
    form.value.auth0Id = user.value.sub;
    form.value.email = user.value.email;
  }
});

const submitForm = async () => {
  loading.value = true;
  error.value = '';
  success.value = false;

  try {
    const response = await api.post('/schools', form.value);
    if(response) success.value = true;
  } catch (err) {
    error.value = err.message || 'Ocurrió un error en el servidor. Inténtalo más tarde.';
  } finally {
    loading.value = false;
  }
};
</script>
