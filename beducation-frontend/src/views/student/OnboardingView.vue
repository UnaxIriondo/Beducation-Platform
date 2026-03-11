<template>
  <div class="max-w-4xl mx-auto py-8">
    <div class="glass-card p-8 rounded-2xl relative overflow-hidden">
        <!-- Decoracion visual -->
        <div class="absolute top-0 right-0 w-32 h-32 bg-sky-200 rounded-bl-full opacity-30"></div>

        <h2 class="text-3xl font-extrabold text-slate-800 mb-2">Construye tu Perfil BeDucation</h2>
        <p class="text-slate-600 mb-8 max-w-2xl border-b border-slate-200 pb-6">
            Para poder aplicar a ofertas de nuestro catálogo y que nuestro algoritmo inteligente te muestre resultados precisos, 
            necesitamos que completes la configuración de tu cuenta.
        </p>

        <!-- Progress Steps -->
        <div class="flex items-center justify-between mb-8 relative">
            <div class="absolute left-0 top-1/2 w-full h-1 bg-slate-200 -translate-y-1/2 z-0 hidden sm:block"></div>
            
            <div v-for="step in 3" :key="step" class="z-10 bg-white border-4 border-slate-100 rounded-full w-12 h-12 flex items-center justify-center font-bold relative"
                 :class="{'bg-sky-50 text-sky-600 border-sky-100': currentStep === step, 'bg-emerald-50 text-emerald-600 border-emerald-100': currentStep > step, 'text-slate-400': currentStep < step}">
                <span v-if="currentStep > step">✓</span>
                <span v-else>{{ step }}</span>
            </div>
        </div>

        <form @submit.prevent="saveProfile" class="space-y-6">
            
            <!-- STEP 1: Datos Personales Basicos -->
            <div v-if="currentStep === 1" class="space-y-6 animate-fade-in relative z-10">
                <h3 class="text-xl font-bold text-slate-700">Paso 1: Información Base</h3>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <label class="block mb-2 text-sm font-medium text-slate-700">Nombre</label>
                        <input v-model="form.firstName" type="text" required class="input-field" placeholder="P.ej. Ana" />
                    </div>
                    <div>
                        <label class="block mb-2 text-sm font-medium text-slate-700">Apellidos</label>
                        <input v-model="form.lastName" type="text" required class="input-field" placeholder="García López" />
                    </div>
                    <div>
                        <label class="block mb-2 text-sm font-medium text-slate-700">Teléfono móvil</label>
                        <input v-model="form.phone" type="tel" class="input-field" placeholder="+34 600 000 000" />
                    </div>
                    <div>
                        <label class="block mb-2 text-sm font-medium text-slate-700">Email de contacto institucional</label>
                        <input v-model="form.email" type="email" class="input-field bg-slate-100 cursor-not-allowed" readonly title="Fijado por tu escuela al invitarte."/>
                    </div>
                </div>
                <div class="flex justify-end pt-4">
                     <button @click.prevent="currentStep = 2" class="btn-primary w-full sm:w-auto">Siguiente: Experiencia ➔</button>
                </div>
            </div>

            <!-- STEP 2: Background y Skills -->
            <div v-if="currentStep === 2" class="space-y-6 animate-fade-in z-10">
                 <h3 class="text-xl font-bold text-slate-700">Paso 2: Académico y Competencias</h3>
                 <div>
                    <label class="block mb-2 text-sm font-medium text-slate-700">Tipo de Educación Actual</label>
                    <select v-model="form.educationTypeId" required class="input-field">
                        <option :value="null">Selecciona una opción...</option>
                        <option value="1">Formación Profesional Básica</option>
                        <option value="2">Ciclo Formativo Grado Medio</option>
                        <option value="3">Ciclo Formativo Grado Superior (CFGS)</option>
                        <option value="4">Grado Universitario</option>
                    </select>
                 </div>
                 
                 <div>
                    <label class="block mb-2 text-sm font-medium text-slate-700">Destinos Deseados (Países)</label>
                    <input type="text" class="input-field text-sm" placeholder="Añade separando por comas (ej, Germany, Italy, Ireland)..." />
                    <p class="text-xs text-slate-500 mt-1">Usado para el 20% del algoritmo MatchMaker de compatibilidad.</p>
                 </div>

                 <div>
                    <label class="block mb-2 text-sm font-medium text-slate-700">Motivación personal e Idiomas (Carta presentación corta)</label>
                    <textarea v-model="form.motivation" rows="4" class="input-field resize-none" placeholder="Escribe al menos 100 caracteres sobre por qué quieres hacer prácticas internacionales..."></textarea>
                 </div>

                 <div class="flex justify-between pt-4">
                     <button @click.prevent="currentStep = 1" class="btn-secondary w-full sm:w-auto">🡰 Atrás</button>
                     <button @click.prevent="currentStep = 3" class="btn-primary w-full sm:w-auto">Siguiente: Sube Documentos ➔</button>
                 </div>
            </div>

            <!-- STEP 3: Subida Curricular AWS S3 -->
            <div v-if="currentStep === 3" class="space-y-6 animate-fade-in z-10 text-center">
                 <h3 class="text-xl font-bold text-slate-700 text-left">Paso 3: CV Oficial</h3>
                 
                 <div class="border-2 border-dashed border-sky-300 bg-sky-50 rounded-2xl p-10 hover:bg-sky-100 transition-colors cursor-pointer" @click="fileInput.click()">
                      <svg class="mx-auto h-12 w-12 text-sky-500 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"></path></svg>
                      <p class="font-bold text-sky-800">Haz clic para subir tu Curriculum Vitae (PDF)</p>
                      <p class="text-sm text-sky-600 mt-1">Obligatorio. Se guardará de forma segura en los servidores en la nube.</p>
                      <input type="file" ref="fileInput" @change="handleCvUpload" accept=".pdf" class="hidden" />
                 </div>

                 <div v-if="cvFileName" class="bg-white border border-emerald-200 p-3 rounded-xl flex items-center justify-between shadow-sm">
                     <span class="flex items-center gap-2 text-sm font-bold text-slate-700">
                         <svg class="h-4 w-4 text-emerald-500" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z" clip-rule="evenodd"></path></svg>
                         {{ cvFileName }}
                     </span>
                     <span class="text-xs bg-emerald-100 text-emerald-700 px-2 py-1 rounded font-medium">Adjunto</span>
                 </div>

                 <div class="flex justify-between pt-8">
                     <button @click.prevent="currentStep = 2" class="btn-secondary w-full sm:w-auto">🡰 Atrás</button>
                     <button type="submit" :disabled="loading" class="btn-primary w-full sm:w-auto bg-emerald-600 hover:bg-emerald-700">
                         {{ loading ? 'Sincronizando...' : 'Finalizar y Guardar Perfil' }}
                     </button>
                 </div>
                 <p v-if="error" class="text-rose-500 text-sm mt-2 text-center">{{ error }}</p>
                 <p v-if="success" class="text-emerald-600 text-sm mt-2 text-center font-bold">¡Perfil actualizado con éxito! Redirigiendo a tu Panel de mando...</p>
            </div>

        </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '../../services/api';

const router = useRouter();
const currentStep = ref(1);

const form = ref({
    firstName: '',
    lastName: '',
    phone: '',
    email: 'alumno@ejemplo.es', // placeholder real sacado de auth local
    educationTypeId: null,
    motivation: ''
});

const cvFile = ref(null);
const cvFileName = ref('');
const fileInput = ref(null);
const loading = ref(false);
const error = ref('');
const success = ref(false);

const handleCvUpload = (event) => {
    const file = event.target.files[0];
    if (file && file.type === 'application/pdf') {
        cvFile.value = file;
        cvFileName.value = file.name;
    } else {
        alert("Por favor, sube un archivo en formato PDF.");
        event.target.value = null;
    }
};

const saveProfile = async () => {
    // Si no ha subido currículum paramos.
    if (!cvFile.value) {
        error.value = "Debes subir tu Currículum Vitae oficial para terminar.";
        return;
    }

    loading.value = true;
    error.value = '';
    
    try {
        // En una app real haríamos:
        // 1. PUT a /students/{id}/profile con el this.form
        // 2. Si hay cvFile, haríamos POST a multipart /students/{id}/documents
        
        // Simulación:
        await new Promise(r => setTimeout(r, 1500));
        
        success.value = true;
        
        setTimeout(() => {
             router.push('/student/dashboard');
        }, 1500);

    } catch(e) {
        error.value = "Ups, ha ocurrido un error conectando al sistema de guardado.";
    } finally {
        loading.value = false;
    }
};
</script>
