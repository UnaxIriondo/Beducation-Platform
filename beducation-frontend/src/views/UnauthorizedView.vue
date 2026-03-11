<template>
  <div class="h-[60vh] flex flex-col items-center justify-center text-center px-4">
    <div class="bg-red-50 text-red-500 rounded-full p-4 mb-6">
      <svg class="w-16 h-16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
         <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
      </svg>
    </div>
    
    <h1 class="text-4xl font-bold text-slate-800 mb-2">Acceso Denegado</h1>
    <p class="text-slate-600 mb-8 max-w-md">
      No posees los permisos necesarios o el rol adecuado para consultar esta ruta de BeDucation. 
      Si crees que se trata de un error, contacta con tu centro.
    </p>

    <div class="flex gap-4">
      <button @click="$router.push('/')" class="btn-primary" aria-label="Volver a Inicio">
        Volver al Inicio
      </button>
      <button @click="logout" class="btn-secondary">
        Cerrar Sesión
      </button>
    </div>
  </div>
</template>

<script setup>
import { useAuth0 } from '@auth0/auth0-vue';
const { logout } = useAuth0();
</script>
