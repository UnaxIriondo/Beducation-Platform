<template>
  <div class="fixed bottom-6 right-6 z-[9999] flex flex-col gap-3 pointer-events-none max-w-sm w-full">
    <transition-group name="toast">
      <div v-for="n in notificationStore.notifications" :key="n.id" 
           class="pointer-events-auto p-4 rounded-2xl shadow-2xl border flex items-start gap-3 animate-in slide-in-from-right duration-300"
           :class="typeClasses[n.type]">
        
        <!-- Iconos según tipo -->
        <div class="shrink-0 mt-0.5">
          <svg v-if="n.type === 'success'" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          <svg v-else-if="n.type === 'error'" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          <svg v-else class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
        </div>

        <div class="flex-1">
          <p class="text-sm font-bold leading-tight">{{ n.message }}</p>
        </div>

        <button @click="notificationStore.remove(n.id)" class="shrink-0 text-current opacity-50 hover:opacity-100 transition-opacity">
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
        </button>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
import { useNotificationStore } from '../../store/notifications';

const notificationStore = useNotificationStore();

const typeClasses = {
  success: 'bg-emerald-50 border-emerald-200 text-emerald-800',
  error: 'bg-rose-50 border-rose-200 text-rose-800',
  info: 'bg-sky-50 border-sky-200 text-sky-800',
  warning: 'bg-amber-50 border-amber-200 text-amber-800'
};
</script>

<style scoped>
.toast-enter-active, .toast-leave-active {
  transition: all 0.3s ease;
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(30px);
}
.toast-leave-to {
  opacity: 0;
  transform: scale(0.9);
}
</style>
