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
                        <option v-for="type in educationTypes" :key="type.id" :value="type.id">
                            {{ type.name }}
                        </option>
                    </select>
                 </div>
                 
                  <div>
                    <label class="block mb-2 text-sm font-medium text-slate-700">Competencias y Tecnologías (Keywords)</label>
                    <div class="grid grid-cols-2 md:grid-cols-3 gap-2 bg-slate-50 p-4 rounded-xl border border-slate-200 max-h-48 overflow-y-auto">
                        <label v-for="kw in allKeywords" :key="kw.id" class="flex items-center gap-2 p-2 hover:bg-white rounded-lg cursor-pointer transition-colors">
                            <input type="checkbox" :value="kw.id" v-model="form.keywordIds" class="rounded text-sky-600 focus:ring-sky-500" />
                            <span class="text-xs text-slate-600 font-medium">{{ kw.name }}</span>
                        </label>
                    </div>
                    <p class="text-xs text-slate-500 mt-2">Selecciona las tecnologías o habilidades que dominas. Esto es clave para el MatchMaker (70% del peso).</p>
                 </div>

                 <div>
                    <label class="block mb-2 text-sm font-medium text-slate-700">Destinos Deseados (Países)</label>
                    <div class="flex flex-wrap gap-2 mb-3 max-h-32 overflow-y-auto p-2 bg-slate-50/50 rounded-xl border border-dashed border-slate-200">
                        <div v-for="code in form.countryPreferences" :key="code" 
                             class="bg-white text-emerald-700 px-3 py-1.5 rounded-lg text-xs font-bold border border-emerald-200 flex items-center gap-2 shadow-sm hover:border-emerald-400 transition-all">
                            <span class="opacity-60">{{ code }}</span>
                            {{ getCountryName(code) }}
                            <button @click.prevent="removeCountry(code)" class="text-slate-400 hover:text-rose-500 transition-colors p-0.5">
                                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
                            </button>
                        </div>
                        <div v-if="form.countryPreferences.length === 0" class="text-xs text-slate-400 italic py-1 px-1">No has seleccionado ningún destino aún...</div>
                    </div>
                    
                    <select @change="addCountry" class="input-field text-sm font-medium focus:ring-emerald-500/20">
                        <option value="">+ Añadir país de destino (31 disponibles)...</option>
                        <option v-for="country in AVAILABLE_COUNTRIES" :key="country.code" :value="country.code" :disabled="form.countryPreferences.includes(country.code)">
                            {{ country.name }}
                        </option>
                    </select>
                    <p class="text-xs text-slate-500 mt-2">Puedes elegir múltiples destinos. Esto ayudará a priorizar las ofertas más adecuadas para ti.</p>
                 </div>

                 <div>
                    <div class="flex justify-between items-center mb-2">
                        <label class="text-sm font-medium text-slate-700">Motivación personal e Idiomas</label>
                        <span class="text-xs font-bold" :class="form.motivation.length >= 100 ? 'text-emerald-500' : 'text-rose-400'">
                            {{ form.motivation.length }} / 100 mín.
                        </span>
                    </div>
                    <textarea v-model="form.motivation" rows="4" class="input-field resize-none" :class="{'border-rose-300': form.motivation.length > 0 && form.motivation.length < 100}" placeholder="Escribe al menos 100 caracteres sobre por qué quieres hacer prácticas internacionales..."></textarea>
                    <p v-if="form.motivation.length > 0 && form.motivation.length < 100" class="text-[10px] text-rose-500 mt-1 font-bold italic animate-pulse">¡Falta un poco más para que tu perfil sea válido!</p>
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
                         CV: {{ cvFileName }}
                     </span>
                     <span class="text-xs bg-emerald-100 text-emerald-700 px-2 py-1 rounded font-medium">Adjunto</span>
                 </div>

                 <!-- Optional Docs -->
                 <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-6">
                    <div class="p-4 border border-slate-200 rounded-xl bg-slate-50/50 hover:bg-slate-50 transition-colors cursor-pointer text-left" @click="$refs.coverLetterInput.click()">
                        <div class="flex items-center justify-between mb-2">
                            <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Opcional</span>
                            <svg v-if="clFileName" class="h-4 w-4 text-emerald-500" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path></svg>
                        </div>
                        <p class="text-sm font-bold text-slate-700">Carta de Presentación</p>
                        <p class="text-[10px] text-slate-500">{{ clFileName || 'Añadir PDF motivacional' }}</p>
                        <input type="file" ref="coverLetterInput" @change="handleClUpload" accept=".pdf" class="hidden" />
                    </div>

                    <div class="p-4 border border-slate-200 rounded-xl bg-slate-50/50 hover:bg-slate-50 transition-colors cursor-pointer text-left" @click="$refs.photoInput.click()">
                        <div class="flex items-center justify-between mb-2">
                            <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Opcional</span>
                            <svg v-if="photoFileName" class="h-4 w-4 text-emerald-500" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path></svg>
                        </div>
                        <p class="text-sm font-bold text-slate-700">Foto de Perfil</p>
                        <p class="text-[10px] text-slate-500">{{ photoFileName || 'Añadir imagen (JPG/PNG)' }}</p>
                        <input type="file" ref="photoInput" @change="handlePhotoUpload" accept="image/*" class="hidden" />
                    </div>
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
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../store/auth';
import api from '../../services/api';

const router = useRouter();
const authStore = useAuthStore();
const currentStep = ref(1);

const AVAILABLE_COUNTRIES = [
    { code: 'AT', name: 'Austria' }, { code: 'BE', name: 'Bélgica' }, { code: 'BG', name: 'Bulgaria' },
    { code: 'CY', name: 'Chipre' }, { code: 'HR', name: 'Croacia' }, { code: 'DK', name: 'Dinamarca' },
    { code: 'SK', name: 'Eslovaquia' }, { code: 'SI', name: 'Eslovenia' }, { code: 'ES', name: 'España' },
    { code: 'EE', name: 'Estonia' }, { code: 'FI', name: 'Finlandia' }, { code: 'FR', name: 'Francia' },
    { code: 'GR', name: 'Grecia' }, { code: 'HU', name: 'Hungría' }, { code: 'IE', name: 'Irlanda' },
    { code: 'IT', name: 'Italia' }, { code: 'LV', name: 'Letonia' }, { code: 'LT', name: 'Lituania' },
    { code: 'LU', name: 'Luxemburgo' }, { code: 'MT', name: 'Malta' }, { code: 'NL', name: 'Países Bajos' },
    { code: 'PL', name: 'Polonia' }, { code: 'PT', name: 'Portugal' }, { code: 'CZ', name: 'República Checa' },
    { code: 'RO', name: 'Rumanía' }, { code: 'SE', name: 'Suecia' }, { code: 'DE', name: 'Alemania' },
    { code: 'GB', name: 'Reino Unido' }, { code: 'CH', name: 'Suiza' }, { code: 'NO', name: 'Noruega' },
    { code: 'IS', name: 'Islandia' }
];

const addCountry = (event) => {
    const code = event.target.value;
    if (code && !form.value.countryPreferences.includes(code)) {
        form.value.countryPreferences.push(code);
    }
    event.target.value = "";
};

const removeCountry = (code) => {
    form.value.countryPreferences = form.value.countryPreferences.filter(c => c !== code);
};

const getCountryName = (code) => {
    return AVAILABLE_COUNTRIES.find(c => c.code === code)?.name || code;
};

const form = ref({
    firstName: '',
    lastName: '',
    phone: '',
    email: '',
    educationTypeId: null,
    motivation: '',
    countryPreferences: [],
    keywordIds: []
});

const cvFile = ref(null);
const cvFileName = ref('');
const clFile = ref(null);
const clFileName = ref('');
const photoFile = ref(null);
const photoFileName = ref('');

const fileInput = ref(null);
const coverLetterInput = ref(null);
const photoInput = ref(null);

const loading = ref(false);
const error = ref('');
const success = ref(false);
const allKeywords = ref([]);
const educationTypes = ref([]);

const studentId = ref(null);

onMounted(async () => {
    if (!authStore.user) {
        await authStore.fetchLocalUserProfile();
    }
    fetchKeywords();
    fetchEducationTypes();
});

const fetchEducationTypes = async () => {
    try {
        const res = await api.get('/education-types');
        educationTypes.value = res;
    } catch (e) {
        console.error("Error fetching education types:", e);
    }
};

const fetchKeywords = async () => {
    try {
        const res = await api.get('/keywords');
        allKeywords.value = res;
    } catch (e) {
        console.error("Error fetching keywords:", e);
    }
}

// Reactividad: Si authStore.user cambia (cuando termina de cargar), actualizamos el formulario
watch(() => authStore.user, (newUser) => {
    console.log('Watcher triggered in onboarding. Full object:', newUser);
    if (newUser) {
        // Intentamos capturar el ID de varias formas por seguridad
        studentId.value = newUser.id || newUser.studentId;
        console.log('Assigned studentId:', studentId.value, 'Types:', typeof studentId.value, 'Keys:', Object.keys(newUser));
        
        form.value.firstName = newUser.firstName || '';
        form.value.lastName = newUser.lastName || '';
        form.value.email = newUser.invitationEmail || authStore.auth0User?.email || '';
        form.value.phone = newUser.phone || '';
        form.value.educationTypeId = newUser.educationType?.id || null;
        form.value.motivation = newUser.motivation || '';
        form.value.countryPreferences = newUser.countryPreferences || [];
        form.value.keywordIds = newUser.keywords?.map(k => k.id) || [];
    }
}, { immediate: true });

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

const handleClUpload = (event) => {
    const file = event.target.files[0];
    if (file && file.type === 'application/pdf') {
        clFile.value = file;
        clFileName.value = file.name;
    } else {
        alert("La carta de presentación debe ser un PDF.");
        event.target.value = null;
    }
};

const handlePhotoUpload = (event) => {
    const file = event.target.files[0];
    if (file && file.type.startsWith('image/')) {
        photoFile.value = file;
        photoFileName.value = file.name;
    } else {
        alert("La foto debe ser una imagen válida.");
        event.target.value = null;
    }
};

const saveProfile = async () => {
    // Usar el ID del store como fuente de verdad definitiva si el local falló
    const effectiveId = studentId.value || authStore.user?.id;
    
    console.log('Attempting to save profile. studentId:', effectiveId, 'From store:', authStore.user?.id);
    
    if (!effectiveId) {
        console.error('Save aborted: No ID found. Store user:', authStore.user);
        error.value = "No se ha podido identificar al estudiante. Prueba a recargar la página.";
        return;
    }

    // Validar motivación
    if (form.value.motivation.length < 100) {
        error.value = "Tu motivación debe tener al menos 100 caracteres.";
        currentStep.value = 2;
        return;
    }

    // Validar keywords
    if (form.value.keywordIds.length === 0) {
        error.value = "Debes seleccionar al menos una competencia tecnológica.";
        currentStep.value = 2;
        return;
    }

    // Si no ha subido currículum paramos, a menos que ya tenga uno (checking documents list)
    const hasExistingCv = authStore.user?.documents?.some(doc => doc.documentType === 'CV');
    if (!cvFile.value && !hasExistingCv && !authStore.user?.profileComplete) {
        error.value = "Debes subir tu Currículum Vitae oficial para terminar (al menos una vez).";
        currentStep.value = 3;
        return;
    }

    loading.value = true;
    error.value = '';
    
    try {
        // 2. Actualizar perfil
        await api.put(`/students/${effectiveId}/profile`, {
            firstName: form.value.firstName,
            lastName: form.value.lastName,
            phone: form.value.phone,
            educationTypeId: form.value.educationTypeId,
            motivation: form.value.motivation,
            keywordIds: form.value.keywordIds,
            countryPreferences: form.value.countryPreferences
        });

        // 3. Subir Documentos
        const uploadDoc = async (file, type) => {
            const formData = new FormData();
            formData.append('file', file);
            formData.append('type', type);
            await api.post(`/students/${effectiveId}/documents`, formData, {
                headers: { 'Content-Type': 'multipart/form-data' },
                params: { type: type }
            });
        };

        if (cvFile.value) await uploadDoc(cvFile.value, 'CV');
        if (clFile.value) await uploadDoc(clFile.value, 'COVER_LETTER');
        if (photoFile.value) await uploadDoc(photoFile.value, 'PHOTO');
        
        success.value = true;
        
        // Refrescar perfil en el store para que sepa que está completo
        await authStore.fetchLocalUserProfile();

        setTimeout(() => {
             router.push('/student/dashboard');
        }, 1500);

    } catch(e) {
        console.error("Error saving profile:", e);
        error.value = e.message || "Ups, ha ocurrido un error conectando al sistema de guardado.";
    } finally {
        loading.value = false;
    }
};
</script>
