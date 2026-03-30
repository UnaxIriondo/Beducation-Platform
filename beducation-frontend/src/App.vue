<template>
  <!-- Main layout con navbar y router view -->
  <div class="min-h-screen flex flex-col w-full font-outfit">
    <!-- Navbar global -->
    <header class="bg-white shadow-sm border-b border-slate-200">
      <nav class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        
        <!-- Logo animado -->
        <div class="flex items-center gap-2 cursor-pointer transition-transform hover:scale-105" @click="$router.push('/')">
          <div class="overflow-hidden rounded-lg shadow-md border border-slate-100 flex items-center justify-center">
            <img src="/logo.jpg" alt="B Education Logo" class="h-10 w-10 object-cover" />
          </div>
          <span class="text-2xl font-bold text-slate-900">
            B Education
          </span>
        </div>

        <!-- Acciones a la derecha basadas en auth0 state -->
        <div class="flex items-center gap-4">
          <div v-if="isLoading" class="animate-pulse text-slate-400 font-medium">Cargando...</div>
          
          <template v-else-if="!authStore.isAuthenticated">
            <div class="flex gap-2">
              <button @click="mockLogin('SCHOOL')" class="text-xs bg-indigo-100 text-indigo-700 font-bold px-2 py-1 rounded hover:bg-indigo-200 transition-colors">
                Test Escuela
              </button>
              <button @click="mockLogin('STUDENT')" class="text-xs bg-sky-100 text-sky-700 font-bold px-2 py-1 rounded hover:bg-sky-200 transition-colors">
                Test Alumno
              </button>
            </div>
          </template>

          <template v-else>
            <!-- Dropdown o Menú rápido de Auth0 mockeado -->
            <div class="flex items-center gap-4">
              <span class="text-sm text-slate-500 hidden sm:block">Bienvenid@, <b>{{ authStore.user?.name || 'Invitado' }}</b></span>
              <div class="w-10 h-10 rounded-full border-2 border-primary-200 bg-primary-100 flex items-center justify-center text-primary-700 font-bold">
                {{ authStore.user?.name ? authStore.user.name.charAt(0) : 'U' }}
              </div>
              
              <button @click="logoutAndClear" class="text-rose-600 hover:text-rose-700 font-medium transition-colors cursor-pointer text-sm bg-rose-50 px-3 py-1.5 rounded-md hover:bg-rose-100">
                Salir (Mock)
              </button>
            </div>
          </template>
        </div>

      </nav>
    </header>

    <!-- App Body (Router target) -->
    <main class="flex-grow flex flex-col bg-slate-50 relative overflow-hidden">
      <!-- Glow Background Elements for Modern Look -->
      <div class="absolute top-0 -left-4 w-72 h-72 bg-primary-300 rounded-full mix-blend-multiply filter blur-3xl opacity-20 pointer-events-none animate-blob"></div>
      <div class="absolute top-0 -right-4 w-72 h-72 bg-emerald-300 rounded-full mix-blend-multiply filter blur-3xl opacity-20 pointer-events-none animate-blob animation-delay-2000"></div>
      
      <div class="flex-grow z-10 container mx-auto px-4 py-8">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>

    <!-- Pie de página simple -->
    <footer class="bg-white border-t border-slate-200 py-6 text-center text-slate-500 text-sm">
      <p>© 2026 BeDucation Platform. Todos los derechos reservados.</p>
    </footer>

  </div>
</template>

<script setup>
import { useAuth0 } from '@auth0/auth0-vue';
import { useAuthStore } from './store/auth';
import { onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';

// Auth0 es mantenido para la integracion final, pero vamos a usar AuthStore mockeado ahora
const { isAuthenticated, user, isLoading, loginWithRedirect, logout, getAccessTokenSilently } = useAuth0();
const authStore = useAuthStore();
const router = useRouter();

onMounted(() => {
  if (isAuthenticated.value) {
    authStore.initialize({
      isAuthenticated,
      user,
      getAccessTokenSilently
    });
  }
});

watch(isAuthenticated, (newState) => {
  if (newState) {
    authStore.initialize({
      isAuthenticated,
      user,
      getAccessTokenSilently
    });
  } else {
    authStore.clearSession();
  }
});

const login = () => {
  loginWithRedirect();
};

const mockLogin = (role) => {
  authStore.mockLogin(role);
  // Redireccionar al dashboard correspondiente
  if (role === 'STUDENT') router.push('/student/dashboard');
  if (role === 'COMPANY') router.push('/company/dashboard');
  if (role === 'SCHOOL') router.push('/school/dashboard');
  if (role === 'ADMIN') router.push('/admin/dashboard');
};

const logoutAndClear = () => {
  authStore.clearSession();
  router.push('/');
};
</script>

<style>
/* Transition simple de vistas */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* Keyframes de aesthetic glow effects */
@keyframes blob {
  0% { transform: translate(0px, 0px) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0px, 0px) scale(1); }
}
.animate-blob {
  animation: blob 7s infinite;
}
.animation-delay-2000 {
  animation-delay: 2s;
}
</style>
