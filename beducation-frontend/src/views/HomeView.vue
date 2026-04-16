<template>
  <div class="min-h-screen relative flex items-center justify-center overflow-hidden bg-slate-50">
    <!-- Background Accents -->
    <div class="absolute top-0 left-0 w-full h-full pointer-events-none overflow-hidden">
      <div class="absolute top-[-10%] right-[-10%] w-[50%] h-[50%] bg-slate-100 rounded-full blur-[120px]"></div>
      <div class="absolute bottom-[-10%] left-[-10%] w-[40%] h-[40%] bg-slate-100 rounded-full blur-[100px]"></div>
    </div>

    <div class="relative z-10 w-full max-w-xl px-6 animate-fade-in">
      <div class="p-10 md:p-14 rounded-[2.5rem] text-center">
        <h1 class="text-5xl md:text-6xl font-black text-slate-900 tracking-tight leading-tight mb-4">
          BeDucation
        </h1>


        <!-- Access Container (Cleaned up glass) -->
        <div class="bg-white/40 backdrop-blur-xl rounded-3xl p-8 md:p-10 shadow-2xl shadow-slate-200/50">
          <!-- Authenticated State -->
          <div v-if="authStore.isAuthenticated" class="space-y-4">
            <p class="text-sm text-slate-500 mb-6 font-medium">Sesión iniciada correctamente</p>
            <button @click="goToDashboard" class="w-full bg-slate-900 text-white py-4 text-lg rounded-2xl font-bold shadow-xl shadow-slate-900/20 flex items-center justify-center gap-3 hover:bg-black transition-all">
              <span>Ir a mi Panel de Control</span>
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor font-bold"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M13 7l5 5m0 0l-5 5m5-5H6" /></svg>
            </button>
            <button @click="authStore.logout()" class="text-[10px] text-slate-400 hover:text-rose-500 font-black uppercase tracking-widest mt-6 transition-colors">Cerrar Sesión</button>
          </div>

          <!-- Login State -->
          <div v-else class="space-y-8">
            <div class="space-y-2 text-left">
                <label class="text-[10px] font-black uppercase tracking-[0.2em] text-slate-400 px-1">Email de Acceso</label>
                <div class="relative group">
                  <input 
                    v-model="debugEmail" 
                    type="email" 
                    @keyup.enter="debugLogin"
                    placeholder="Introduce tu dirección de email" 
                    class="w-full px-5 py-4 bg-white/80 border border-slate-100 rounded-2xl focus:ring-4 focus:ring-slate-900/5 focus:border-slate-900 outline-none transition-all font-medium text-slate-700 placeholder:text-slate-300 shadow-sm" 
                  />
                </div>
                <p v-if="debugError" class="text-[11px] text-rose-500 font-bold px-1 mt-2 animate-shake">{{ debugError }}</p>
            </div>

            <button 
              @click="debugLogin" 
              :disabled="!debugEmail || loadingDebug" 
              class="w-full bg-slate-900 text-white py-4 rounded-2xl font-bold flex items-center justify-center gap-3 hover:bg-black transition-all shadow-xl shadow-slate-900/10 active:scale-95 disabled:opacity-30 disabled:pointer-events-none"
            >
                <span v-if="loadingDebug" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                <span v-else>Entrar al Sistema</span>
            </button>

         
          </div>
        </div>
      </div>


    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useAuthStore } from '../store/auth';
import { useRouter, useRoute } from 'vue-router';
import { useAuth0 } from '@auth0/auth0-vue';

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();
const { loginWithRedirect } = useAuth0();

const debugEmail = ref('');
const loadingDebug = ref(false);
const debugError = ref('');

onMounted(() => {
  if (route.path === '/register' || route.query.email) {
    const email = route.query.email;
    if (!authStore.isAuthenticated) {
      loginWithRedirect({
        authorizationParams: {
          screen_hint: 'signup',
          login_hint: email
        }
      });
    }
  }
});

const debugLogin = async () => {
  if (!debugEmail.value) return;
  loadingDebug.value = true;
  debugError.value = '';
  try {
    await authStore.debugLogin(debugEmail.value);
    goToDashboard();
  } catch (e) {
    debugError.value = "Email no reconocido. Verifica tus credenciales.";
  } finally {
    loadingDebug.value = false;
  }
};

const goToDashboard = () => {
  const role = authStore.role;
  if (role === 'STUDENT') router.push('/student/dashboard');
  else if (role === 'SCHOOL') router.push('/school/dashboard');
  else if (role === 'COMPANY') router.push('/company/dashboard');
  else if (role === 'ADMIN') router.push('/admin/dashboard');
};
</script>

<style scoped>
.animate-fade-in {
  animation: fadeIn 1s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

.animate-shake {
  animation: shake 0.5s cubic-bezier(.36,.07,.19,.97) both;
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.95) translateY(30px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-4px, 0, 0); }
  40%, 60% { transform: translate3d(4px, 0, 0); }
}
</style>
