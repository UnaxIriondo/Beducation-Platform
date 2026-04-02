<template>
  <div class="min-h-[80vh] flex items-center justify-center p-4">
    <div class="max-w-md w-full space-y-8 bg-white p-8 rounded-2xl shadow-xl border border-slate-100 relative overflow-hidden">
      <!-- Decoración de fondo -->
      <div class="absolute -top-24 -right-24 w-48 h-48 bg-primary-100 rounded-full opacity-50 blur-3xl"></div>
      
      <div class="relative z-10 text-center">
        <div class="flex justify-center mb-4">
          <div class="bg-primary-50 p-3 rounded-xl">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-primary-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 00-2 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
          </div>
        </div>
        <h2 class="text-3xl font-extrabold text-slate-900 tracking-tight">Bienvenido</h2>
        <p class="mt-2 text-sm text-slate-500">Inicia sesión con tu cuenta de BeDucation</p>
      </div>

      <form class="mt-8 space-y-6 relative z-10" @submit.prevent="handleLogin">
        <div class="rounded-md space-y-4">
          <div>
            <label for="email" class="block text-sm font-medium text-slate-700 mb-1">Correo Electrónico</label>
            <input 
              id="email" 
              v-model="email" 
              name="email" 
              type="email" 
              required 
              class="appearance-none block w-full px-4 py-3 border border-slate-300 rounded-xl placeholder-slate-400 text-slate-900 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all sm:text-sm" 
              placeholder="tu@email.com"
            />
          </div>
          <div>
            <label for="password" class="block text-sm font-medium text-slate-700 mb-1">Contraseña</label>
            <input 
              id="password" 
              v-model="password" 
              name="password" 
              type="password" 
              required 
              class="appearance-none block w-full px-4 py-3 border border-slate-300 rounded-xl placeholder-slate-400 text-slate-900 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all sm:text-sm" 
              placeholder="••••••••"
            />
          </div>
        </div>

        <div v-if="error" class="bg-rose-50 border border-rose-100 text-rose-600 p-3 rounded-xl text-sm animate-shake">
          {{ error }}
        </div>

        <div>
          <button 
            type="submit" 
            :disabled="loading"
            class="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-bold rounded-xl text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 transition-all shadow-lg shadow-primary-200 disabled:opacity-70 disabled:cursor-not-allowed"
          >
            <span v-if="loading" class="absolute left-0 inset-y-0 flex items-center pl-3">
              <svg class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            </span>
            {{ loading ? 'Iniciando sesión...' : 'Entrar' }}
          </button>
        </div>
      </form>

      <div class="mt-6 text-center z-10 relative">
        <p class="text-xs text-slate-400">
          ¿No tienes cuenta? Contacta con tu centro educativo.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '../store/auth';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

const email = ref('');
const password = ref('');
const error = ref('');
const loading = ref(false);

const handleLogin = async () => {
  loading.value = true;
  error.value = '';
  
  try {
    const success = await authStore.localLogin(email.value, password.value);
    if (success) {
      // Redireccionar según el rol
      const role = authStore.role;
      if (role === 'STUDENT') router.push('/student/dashboard');
      else if (role === 'SCHOOL') router.push('/school/dashboard');
      else if (role === 'COMPANY') router.push('/company/dashboard');
      else if (role === 'ADMIN') router.push('/admin/dashboard');
    }
  } catch (err) {
    error.value = err.message || 'Error al iniciar sesión. Verifica tus credenciales.';
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}
.animate-shake {
  animation: shake 0.4s ease-in-out;
}
</style>
