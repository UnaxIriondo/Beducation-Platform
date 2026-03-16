<template>
  <div class="h-full flex flex-col items-center justify-center text-center space-y-6 animate-fade-in py-16">
    <!--
    <div class="max-w-3xl mx-auto space-y-8 glass-card p-12 rounded-3xl relative overflow-hidden">
      <div class="absolute -top-10 -right-10 w-40 h-40 bg-primary-100 rounded-full blur-3xl opacity-50"></div>
      
      <h1 class="text-5xl md:text-6xl font-extrabold tracking-tight text-slate-900 leading-tight">
        Conecta Talento con el <span class="bg-clip-text text-transparent bg-gradient-to-r from-primary-600 to-sky-500">Mundo Internacional</span>
      </h1>
      
      <p class="text-lg md:text-xl text-slate-600 max-w-2xl mx-auto font-light leading-relaxed">
        BeDucation es la plataforma integral para la gestión de prácticas empresariales Erasmus+. 
        Escuelas, empresas y estudiantes conectados bajo un flujo de trabajo optimizado y seguro.
      </p>

      <div class="flex flex-col sm:flex-row gap-4 justify-center mt-10">
        <button v-if="!authStore.isAuthenticated" @click="mockLogin('STUDENT')" class="btn-primary px-8 py-4 text-lg w-full sm:w-auto hover-scale shadow-primary-500/30">
          Soy Estudiante
        </button>
        <button v-else @click="goToDashboard" class="bg-emerald-500 text-white font-medium rounded-lg px-8 py-4 text-lg w-full sm:w-auto hover:bg-emerald-600 hover-scale shadow-emerald-500/30 transition-all">
          Ir a mi Panel de Control
        </button>
        
        <button v-if="!authStore.isAuthenticated" @click="mockLogin('COMPANY')" class="btn-secondary px-8 py-4 text-lg w-full sm:w-auto hover-scale">
          Soy Empresa Receptora
        </button>
      </div>
    </div>
    -->
    
    <!-- Sección Features Rápida -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto mt-20 px-4">
      <div class="glass-card p-8 rounded-2xl hover-scale bg-white">
        <div class="h-12 w-12 bg-primary-100 text-primary-600 rounded-xl flex items-center justify-center mb-6">
          <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
          </svg>
        </div>
        <h3 class="text-xl font-semibold mb-3">Escuelas</h3>
        <p class="text-slate-600">Gestiona convenios, aprueba el progreso de tus alumnos e invita estuidantes masivamente por CSV.</p>
      </div>

      <div class="glass-card p-8 rounded-2xl hover-scale bg-white">
        <div class="h-12 w-12 bg-emerald-100 text-emerald-600 rounded-xl flex items-center justify-center mb-6">
          <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
          </svg>
        </div>
        <h3 class="text-xl font-semibold mb-3">Empresas</h3>
        <p class="text-slate-600">Publica ofertas Erasmus y evalúa candidatos usando nuestro algoritmo MatchMaker automatizado del 70-20-10.</p>
      </div>

      <div class="glass-card p-8 rounded-2xl hover-scale bg-white">
        <div class="h-12 w-12 bg-sky-100 text-sky-600 rounded-xl flex items-center justify-center mb-6">
          <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
        </div>
        <h3 class="text-xl font-semibold mb-3">Estudiantes</h3>
        <p class="text-slate-600">Crea tu perfil, sube documentación internacional y realiza seguimiento en todas las etapas del funnel.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '../store/auth';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

const mockLogin = (role) => {
  authStore.mockLogin(role);
  goToDashboard();
};

const goToDashboard = () => {
  // Redirigir dinámicamente según el claim de rol del usuario extraído de su Auth0 JWT
  const role = authStore.role;
  if (role === 'STUDENT') router.push('/student/dashboard');
  else if (role === 'SCHOOL') router.push('/school/dashboard');
  else if (role === 'COMPANY') router.push('/company/dashboard');
  else if (role === 'ADMIN') router.push('/admin/dashboard');
  else console.warn("Rol inválido o pending para dirigir Dashboard");
};
</script>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.8s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
