<template>
  <div class="max-w-3xl mx-auto py-10 px-4">
    <div class="glass-card p-8 md:p-12 rounded-3xl shadow-2xl relative overflow-hidden">
        <!-- Glow top corner -->
        <div class="absolute -top-16 -right-16 w-48 h-48 bg-emerald-400 rounded-full blur-[80px] opacity-20 pointer-events-none"></div>

      <h2 class="text-3xl font-extrabold text-slate-900 mb-2">Publica en BeDucation</h2>
      <p class="text-slate-600 mb-8 border-b border-slate-200 pb-6">
          Registra tu Empresa/Organización para crear oportunidades de prácticas profesionales (Erasmus). 
          Accede al talento europeo de forma directa.
      </p>

      <form @submit.prevent="submitForm" class="space-y-6">
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 relative z-10">
          <div class="col-span-1 md:col-span-2">
            <label class="block mb-2 text-sm font-bold text-slate-700">Razón Social Contratante</label>
            <input v-model="form.name" type="text" required class="input-field" placeholder="Empresa S.A." />
          </div>

          <div>
            <label class="block mb-2 text-sm font-bold text-slate-700">Sede (País fiscal)</label>
            <input v-model="form.country" type="text" required class="input-field" placeholder="Alemania" />
          </div>
          
          <div>
            <label class="block mb-2 text-sm font-bold text-slate-700">Ciudad principal</label>
            <input v-model="form.city" type="text" class="input-field" placeholder="Berlín" />
          </div>

          <div class="col-span-1 md:col-span-2">
             <label class="block mb-2 text-sm font-bold text-slate-700">Descripción Pública de la Empresa</label>
             <textarea v-model="form.description" rows="3" class="input-field resize-none" placeholder="Somos una startup tecnológica orientada a..."></textarea>
          </div>

          <div>
            <label class="block mb-2 text-sm font-bold text-slate-700">Web corporativa</label>
            <input v-model="form.website" type="url" class="input-field" placeholder="https://..." />
          </div>

           <div>
            <label class="block mb-2 text-sm font-bold text-slate-700">Tamaño (Num. Empleados)</label>
            <select v-model="form.companySize" class="input-field border-r-8 border-transparent">
                <option value="1-10">1-10 Empleados (Micro)</option>
                <option value="11-50">11-50 Empleados (Pequeña)</option>
                <option value="51-250">51-250 Empleados (Mediana)</option>
                <option value="250+">Más de 250 (Gran Empresa)</option>
            </select>
          </div>
          
          <div class="col-span-1 md:col-span-2">
            <label class="block mb-2 text-sm font-bold text-slate-700" title="Información protegida recuperada de su inicio de sesión Google/SSO">Identidad Gestor Autorizado (SSO/Email)</label>
            <div class="flex gap-4">
               <input v-model="form.auth0Id" type="text" class="input-field flex-1 bg-slate-100/50 text-slate-500 cursor-not-allowed text-xs font-mono" readonly/>
               <input v-model="form.email" type="text" class="input-field flex-1 bg-slate-100/50 text-slate-500 cursor-not-allowed" readonly/>
            </div>
            <p class="text-xs text-emerald-600 mt-1 flex items-center gap-1 font-medium">
                <svg class="h-3 w-3" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path></svg>
                Vinculado a la IdP correctamente.
            </p>
          </div>
        </div>

        <button type="submit" :disabled="loading" class="btn-primary w-full mt-8 py-3.5 text-lg font-bold bg-emerald-600 hover:bg-emerald-700 focus:ring-emerald-200">
           <!-- Spinner svg inline -->
           <span v-if="loading" class="inline-block animate-spin mr-2">◷</span>
          {{ loading ? 'Procesando registro en Beducation...' : 'Enviar Solicitud de Onboarding' }}
        </button>

        <p v-if="error" class="text-rose-500 text-sm mt-4 text-center p-3 bg-rose-50 rounded-lg">{{ error }}</p>
        <div v-if="success" class="mt-6 bg-emerald-50 border border-emerald-100 p-4 rounded-xl text-center">
            <h4 class="text-emerald-800 font-bold mb-1">¡Solicitud recibida!</h4>
            <p class="text-emerald-600 text-sm">Nuestro equipo de soporte revisará la información y activará tu cuenta Empresa vía email en menos de 24 horas laborables.</p>
        </div>

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
  name: '', email: '', country: '', city: '', website: '', description: '', companySize: '11-50', auth0Id: ''
});
const loading = ref(false);
const error = ref('');
const success = ref(false);

onMounted(() => {
  if (!isAuthenticated.value) {
    loginWithRedirect({ appState: { targetUrl: '/register/company' } });
  } else {
    form.value.auth0Id = user.value.sub;
    form.value.email = user.value.email;
  }
});

const submitForm = async () => {
  loading.value = true;
  error.value = '';
  try {
    const response = await api.post('/companies', form.value);
    if(response) success.value = true;
  } catch (err) {
    error.value = err.message || 'Error registrando la empresa.';
  } finally {
    loading.value = false;
  }
};
</script>
