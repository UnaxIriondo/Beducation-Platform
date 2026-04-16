<template>
  <div class="min-h-screen flex items-center justify-center bg-white p-6 font-outfit">
    <!-- Contenedor Minimalista -->
    <div class="w-full max-w-md">
      <div class="text-center mb-10">
        <h1 class="text-3xl font-bold text-slate-900 tracking-tight">BeDucation</h1>
        <p class="text-slate-500 mt-1 text-sm">Plataforma de Movilidad Internacional</p>
      </div>

      <div class="bg-white border border-slate-200 rounded-2xl p-8">
        <div class="space-y-6">
          <!-- Botón Auth0 Principal -->
          <button 
            @click="login"
            class="w-full h-12 bg-slate-900 text-white rounded-xl font-bold text-sm hover:bg-slate-800 transition-all flex items-center justify-center gap-3 shadow-lg shadow-slate-200"
          >
            <svg class="w-5 h-5" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.477 2 2 6.477 2 12s4.477 10 10 10 10-4.477 10-10S17.523 2 12 2zm0 18c-4.411 0-8-3.589-8-8s3.589-8 8-8 8 3.589 8 8-3.589 8-8 8z"/></svg>
            Continuar con Auth0
          </button>

          <p class="text-[10px] text-slate-400 text-center uppercase tracking-[0.2em] font-bold">O usa el acceso de depuración</p>

          <!-- Separador sutil -->
          <div class="relative py-2">
            <div class="absolute inset-0 flex items-center">
              <div class="w-full border-t border-slate-100"></div>
            </div>
          </div>

          <!-- Panel de Acceso Rápido Simplificado -->
          <div class="grid grid-cols-2 gap-2">
            <button type="button" @click="quickLogin('admin@beducation.com')" class="text-[10px] font-bold p-3 bg-slate-50 text-slate-600 rounded-lg hover:bg-slate-100 transition-colors border border-slate-100">ADMINISTRADOR</button>
            <button type="button" @click="quickLogin('school@beducation.com')" class="text-[10px] font-bold p-3 bg-slate-50 text-slate-600 rounded-lg hover:bg-slate-100 transition-colors border border-slate-100">CENTRO EDUCATIVO</button>
            <button type="button" @click="quickLogin('company@beducation.com')" class="text-[10px] font-bold p-3 bg-slate-50 text-slate-600 rounded-lg hover:bg-slate-100 transition-colors border border-slate-100">EMPRESA</button>
            <button type="button" @click="quickLogin('student@beducation.com')" class="text-[10px] font-bold p-3 bg-slate-50 text-slate-600 rounded-lg hover:bg-slate-100 transition-colors border border-slate-100">ESTUDIANTE</button>
          </div>

          <!-- Botón de Reset DB (Sutil) -->
          <div class="pt-4 text-center">
            <button 
                type="button" 
                @click="resetDB" 
                class="text-[9px] text-slate-300 hover:text-rose-400 font-bold uppercase tracking-widest transition-colors"
                title="Elimina todos los datos de prueba actuales"
            >
                [ Limpiar Base de Datos ]
            </button>
          </div>
        </div>
      </div>

      <div class="mt-8 text-center">
        <p class="text-[11px] text-slate-400">
          BeDucation v2.0 · Entorno de pruebas
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '../store/auth';
import { useRouter } from 'vue-router';
import api from '../services/api';
import { useAuth0 } from '@auth0/auth0-vue';

const authStore = useAuthStore();
const router = useRouter();
const { loginWithRedirect } = useAuth0();

const email = ref('');
const error = ref('');
const loading = ref(false);

const login = () => {
    loginWithRedirect();
};

const quickLogin = async (debugEmail) => {
    email.value = debugEmail;
    loading.value = true;
    error.value = '';
    try {
        const success = await authStore.debugLogin(debugEmail);
        if (success) {
            const role = authStore.role;
            if (role === 'STUDENT') router.push('/student/dashboard');
            else if (role === 'SCHOOL') router.push('/school/dashboard');
            else if (role === 'COMPANY') router.push('/company/dashboard');
            else if (role === 'ADMIN') router.push('/admin/dashboard');
        }
    } catch (err) {
        error.value = 'Error en debug login: ' + err.message;
    } finally {
        loading.value = false;
    }
};

const resetDB = async () => {
    if (!confirm('¿Estás seguro de que quieres borrar TODOS los datos de prueba? Esta acción no se puede deshacer.')) return;
    try {
        await api.post('/debug/reset-database');
        alert('Base de datos limpiada. Todos los mocks han sido eliminados.');
        window.location.reload();
    } catch (err) {
        alert('Error al resetear BD: ' + (err.response?.data?.message || err.message));
    }
};
</script>
