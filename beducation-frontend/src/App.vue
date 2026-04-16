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
            <div class="flex items-center gap-4">
              <span class="text-[11px] text-slate-500 font-medium uppercase tracking-wider hidden sm:block">
                {{ authStore.user?.name || 'Usuario' }}
                <span class="ml-2 px-1.5 py-0.5 bg-slate-50 text-slate-400 rounded border border-slate-100 text-[10px] font-bold">
                  {{ authStore.role }}
                </span>
              </span>
              
              <button @click="logoutAndClear" class="text-slate-400 hover:text-slate-900 font-bold text-[11px] uppercase tracking-widest transition-colors">
                Salir
              </button>
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

  </div>
</template>

<script setup>
import { useAuth0 } from '@auth0/auth0-vue';
import { useAuthStore } from './store/auth';
import { onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';

const { isAuthenticated, user, isLoading, loginWithRedirect, getAccessTokenSilently } = useAuth0();
const authStore = useAuthStore();
const router = useRouter();

onMounted(() => {
  if (isAuthenticated.value) {
    authStore.initialize({
      isAuthenticated,
      user,
      getAccessTokenSilently
    });
  } else if (authStore.isAuthenticated && authStore.role && !authStore.user) {
    authStore.fetchLocalUserProfile();
  }
});

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
