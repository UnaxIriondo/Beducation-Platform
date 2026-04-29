<template>
  <div class="min-h-screen flex flex-col w-full font-outfit bg-white">
    <!-- Navbar Minimalista -->
    <header class="bg-white border-b border-slate-100">
      <nav class="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
        
        <!-- Logo -->
        <div class="flex items-center gap-3 cursor-pointer" @click="$router.push('/')">
          <div class="w-8 h-8 overflow-hidden rounded-lg bg-slate-900 flex items-center justify-center">
            <span class="text-white font-bold text-xs">B</span>
          </div>
          <span class="text-lg font-bold text-slate-900 tracking-tight">
            BeDucation
          </span>
        </div>

        <!-- Acciones -->
        <div class="flex items-center gap-4">
          <div v-if="isLoading" class="text-xs text-slate-400 font-medium">Cargando...</div>
          
          <template v-else-if="!authStore.isAuthenticated">
            <button @click="$router.push('/login')" class="btn-primary text-xs py-2 px-4">
              Iniciar Sesión
            </button>
          </template>

          <template v-else>
            <div class="flex items-center gap-6">
              <div class="flex flex-col items-end">
                <span class="text-sm font-bold text-slate-900 leading-none">
                  {{ authStore.user?.firstName ? (authStore.user.firstName + ' ' + (authStore.user.lastName || '')) : (authStore.user?.name || 'Usuario') }}
                </span>
                <button @click="logoutAndClear" class="text-[10px] font-black text-slate-400 hover:text-rose-600 uppercase tracking-[0.2em] mt-1.5 transition-colors">
                  Cerrar Sesión
                </button>
              </div>
            </div>
          </template>
        </div>
      </nav>
    </header>

    <!-- Main Content -->
    <main class="flex-grow bg-white">
      <div class="max-w-7xl mx-auto px-6 py-10">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>

    <!-- Footer -->
    <footer class="bg-white border-t border-slate-50 py-10 text-center">
      <p class="text-[10px] text-slate-400 font-bold uppercase tracking-[0.2em]">© 2026 BeDucation Platform </p>
    </footer>

    <!-- Notificaciones Globales -->
    <NotificationToast />

  </div>
</template>


<script setup>
/**
 * COMPONENTE PRINCIPAL: App.vue
 * -----------------------------
 * Define el Layout global de la aplicación.
 * Gestiona la sincronización inicial entre el SDK de Auth0 y el store de Pinia.
 */
import { useAuth0 } from '@auth0/auth0-vue';
import { useAuthStore } from './store/auth';
import { onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import NotificationToast from './components/common/NotificationToast.vue';


const { isAuthenticated, user, isLoading, loginWithRedirect, getAccessTokenSilently } = useAuth0();
const authStore = useAuthStore();
const router = useRouter();

/**
 * Al montar, si ya estamos logueados en Auth0, inicializamos nuestro store local.
 * Esto asegura que tengamos el token y el rol cargado en toda la aplicación.
 */
onMounted(() => {
  if (isAuthenticated.value) {
    authStore.initialize({
      isAuthenticated,
      user,
      getAccessTokenSilently
    });
  } else if (authStore.isAuthenticated && authStore.role && !authStore.user) {
    // Si tenemos sesión local pero no datos de perfil, intentamos recuperarlos
    authStore.fetchLocalUserProfile();
  }
});

/**
 * Observa cambios en el estado de Auth0 (ej. tras redirección de login exitosa)
 */
watch(isAuthenticated, (newState) => {
  if (newState) {
    authStore.initialize({
      isAuthenticated,
      user,
      getAccessTokenSilently
    });
  } else if (!authStore.isAuthenticated) {
      authStore.clearSession();
  }
});

/**
 * Cierra la sesión en nuestro sistema y redirige a la home.
 */
const logoutAndClear = () => {
  authStore.clearSession();
  router.push('/');
};
</script>


<style>
/* Reset base styles */
body {
  @apply bg-white;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
