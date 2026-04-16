<template>
  <div class="space-y-6 max-w-7xl mx-auto py-8 px-4">
    <!-- Header -->
    <div class="section-card border-l-4 border-l-sky-500 flex flex-col md:flex-row justify-between items-center gap-4">
      <div>
        <h1 class="text-3xl font-extrabold text-slate-900 tracking-tight">Galería de Eventos</h1>
        <p class="text-slate-500 mt-1">Explora los momentos más memorables de nuestros encuentros internacionales.</p>
      </div>
      <div class="flex flex-wrap gap-2">
         <button @click="goBackToDashboard" class="btn-secondary flex items-center gap-2">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>
            Volver a mi Panel
         </button>
         <template v-if="authStore.role === 'ADMIN'">
             <button @click="showUploadModal = true" class="btn-primary flex items-center gap-2">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
                Subir Fotos
             </button>
             <button @click="activeTab = activeTab === 'gallery' ? 'requests' : 'gallery'" class="btn-secondary">
                {{ activeTab === 'gallery' ? 'Gestionar Accesos' : 'Ver Galería' }}
             </button>
         </template>
      </div>
    </div>

    <!-- TABS (Solo para Admin) -->
    <div v-if="authStore.role === 'ADMIN' && activeTab === 'requests'" class="animate-fade-in">
        <div class="glass-card p-6">
            <h2 class="text-xl font-bold text-slate-800 mb-6">Solicitudes de Acceso Pendientes</h2>
            
            <div v-if="pendingRequests.length === 0" class="text-center py-12 text-slate-400 italic">
                No hay solicitudes pendientes en este momento.
            </div>
            
            <div v-else class="overflow-x-auto">
                <table class="w-full text-left">
                    <thead>
                        <tr class="border-b border-slate-100 text-slate-400 text-xs uppercase tracking-widest font-black">
                            <th class="pb-3 text-center">Usuario</th>
                            <th class="pb-3 text-center">Email</th>
                            <th class="pb-3 text-center">Rol</th>
                            <th class="pb-3 text-center">Fecha</th>
                            <th class="pb-3 text-center">Acciones</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-slate-50">
                        <tr v-for="req in pendingRequests" :key="req.id" class="hover:bg-slate-50 transition-colors">
                            <td class="py-4 font-bold text-slate-700 text-center">{{ req.user.fullName || 'Usuario' }}</td>
                            <td class="py-4 text-slate-500 text-center">{{ req.user.email }}</td>
                            <td class="py-4 text-center">
                                <span class="px-2 py-1 rounded-md text-[10px] font-black uppercase tracking-wider" 
                                      :class="req.user.role === 'STUDENT' ? 'bg-sky-100 text-sky-700' : 'bg-indigo-100 text-indigo-700'">
                                    {{ req.user.role }}
                                </span>
                            </td>
                            <td class="py-4 text-slate-400 text-xs text-center">{{ new Date(req.requestedAt).toLocaleDateString() }}</td>
                            <td class="py-4 text-center">
                                <div class="flex justify-center gap-2">
                                    <button @click="processRequest(req.id, true)" class="p-2 bg-emerald-100 text-emerald-600 rounded-lg hover:bg-emerald-200 transition-colors" title="Aprobar">
                                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                                    </button>
                                    <button @click="processRequest(req.id, false)" class="p-2 bg-rose-100 text-rose-600 rounded-lg hover:bg-rose-200 transition-colors" title="Rechazar">
                                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                                    </button>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- MAIN GALLERY CONTENT -->
    <div v-else class="animate-fade-in">
        <!-- ACCESS CONTROL VIEW (For non-approved users) -->
        <div v-if="!hasAccess && authStore.role !== 'ADMIN'" class="glass-card p-12 text-center max-w-2xl mx-auto">
            <div class="w-20 h-20 bg-sky-50 rounded-full flex items-center justify-center mx-auto mb-6 text-sky-500">
                <svg class="w-10 h-10" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
            </div>
            <h2 class="text-2xl font-bold text-slate-800 mb-2">Galería Restringida</h2>
            
            <!-- CASO 1: Sin solicitud aún -->
            <template v-if="!myRequest">
                <p class="text-slate-500 mb-8">
                    La galería de fotos contiene imágenes de los eventos de Beducation. Por privacidad, debes solicitar acceso para ver el contenido.
                </p>
                <button @click="requestAccess" :disabled="loading" class="btn-primary flex items-center gap-2 mx-auto">
                    {{ loading ? 'Enviando...' : 'Solicitar Acceso' }}
                    <svg v-if="!loading" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path></svg>
                </button>
            </template>

            <!-- CASO 2: Solicitud Pendiente -->
            <template v-else-if="myRequest.status === 'PENDING'">
                <div class="bg-amber-50 border border-amber-200 p-4 rounded-xl text-amber-800 mb-6">
                    <p class="font-bold flex items-center justify-center gap-2">
                        <span class="w-2 h-2 bg-amber-500 rounded-full animate-ping"></span>
                        Solicitud Pendiente de Revisión
                    </p>
                    <p class="text-sm mt-1">Enviada el {{ new Date(myRequest.requestedAt).toLocaleDateString() }}. Te notificaremos en cuanto el Administrador la apruebe.</p>
                </div>
                <button disabled class="btn-secondary opacity-60">Esperando Probación Administrativa...</button>
            </template>

            <!-- CASO 3: Reclamada/Rechazada -->
            <template v-else-if="myRequest.status === 'REJECTED'">
                 <div class="bg-rose-50 border border-rose-200 p-4 rounded-xl text-rose-800 mb-6">
                    <p class="font-bold">Solicitud Denegada</p>
                    <p class="text-sm mt-1">Motivo: {{ myRequest.rejectionReason || 'No especificado por el administrador.' }}</p>
                </div>
                <p class="text-xs text-slate-400">Ponte en contacto con soporte si crees que esto es un error.</p>
            </template>
        </div>

        <!-- PHOTO GRID VIEW (Admin or Approved Users) -->
        <div v-else>
            <div v-if="loadingPhotos" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 animate-pulse">
                <div v-for="i in 8" :key="i" class="bg-slate-100 aspect-square rounded-xl"></div>
            </div>
            
            <div v-else-if="photos.length === 0" class="text-center py-20 bg-slate-50 border border-slate-200 border-dashed rounded-2xl">
                <div class="w-16 h-16 bg-white rounded-full flex items-center justify-center mx-auto mb-4 text-slate-300 shadow-sm">
                    <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                </div>
                <h3 class="text-slate-500 font-bold">No hay fotos en la galería todavía</h3>
                <p v-if="authStore.role === 'ADMIN'" class="text-slate-400 text-sm mt-1">Usa el botón superior para subir las primeras imágenes.</p>
            </div>

            <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                <div v-for="photo in photos" :key="photo.id" 
                     @click="selectedPhoto = photo"
                     class="group relative aspect-square rounded-2xl overflow-hidden bg-slate-100 border border-slate-200 shadow-sm hover:shadow-xl transition-all cursor-pointer">
                    <img :src="getPhotoUrl(photo.s3Key)" :alt="photo.description" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110" />
                    
                    <div class="absolute inset-0 bg-gradient-to-t from-slate-900/80 via-transparent opacity-0 group-hover:opacity-100 transition-opacity flex flex-col justify-end p-4">
                        <p class="text-white text-xs font-medium">{{ photo.description || 'Evento Beducation' }}</p>
                        <p class="text-slate-300 text-[10px] mt-1">{{ new Date(photo.uploadedAt).toLocaleDateString() }}</p>
                        <button v-if="authStore.role === 'ADMIN'" @click.stop="deletePhoto(photo.id)" 
                                class="absolute top-2 right-2 p-1.5 bg-rose-500/80 text-white rounded-lg hover:bg-rose-600 transition-colors">
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- MODAL DE SUBIDA (Solo Admin) -->
    <div v-if="showUploadModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
        <div class="bg-white w-full max-w-md rounded-2xl shadow-2xl overflow-hidden border border-slate-200">
            <div class="p-6 border-b border-slate-100 flex justify-between items-center">
                <h3 class="text-xl font-bold text-slate-800">Cargar Fotografías</h3>
                <button @click="showUploadModal = false" class="text-slate-400 hover:text-slate-600">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                </button>
            </div>
            
            <form @submit.prevent="handleUpload" class="p-6 space-y-4 text-left">
                <div>
                    <label class="label-caps">Archivos de Imagen</label>
                    <div class="border-2 border-dashed border-slate-200 rounded-xl p-8 text-center hover:bg-slate-50 transition-colors cursor-pointer" @click="$refs.fileInput.click()">
                        <svg class="w-10 h-10 text-slate-300 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
                        <p class="text-sm font-bold text-slate-600">Haz clic para subir (JPG/PNG)</p>
                        <p class="text-xs text-slate-400 mt-1">Puedes subir múltiples fotos a la vez.</p>
                    </div>
                    <input type="file" ref="fileInput" multiple accept="image/*" class="hidden" @change="onFilesSelected" />
                </div>

                <div v-if="selectedFiles.length > 0" class="bg-slate-50 p-3 rounded-lg flex items-center justify-between">
                    <span class="text-xs font-bold text-slate-600">{{ selectedFiles.length }} archivos seleccionados</span>
                    <button type="button" @click="selectedFiles = []" class="text-rose-500 text-xs font-bold hover:underline">Limpiar</button>
                </div>

                <div>
                    <label class="label-caps">Descripción del Evento</label>
                    <input v-model="uploadDescription" type="text" class="input-field" placeholder="P.ej: Feria de Prácticas 2024" />
                </div>

                <div class="pt-4 flex gap-3">
                    <button type="button" @click="showUploadModal = false" class="btn-secondary flex-1">Cancelar</button>
                    <button type="submit" :disabled="uploading || selectedFiles.length === 0" class="btn-primary flex-1">
                        {{ uploading ? 'Subiendo...' : 'Publicar Ahora' }}
                    </button>
                </div>
            </form>
        </div>
    </div>
    <!-- LIGHTBOX MODAL -->
    <div v-if="selectedPhoto" class="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-slate-950/95 backdrop-blur-md animate-fade-in" @click="selectedPhoto = null">
        <!-- Close button (top right of screen) -->
        <button @click="selectedPhoto = null" class="fixed top-6 right-6 text-white/50 hover:text-white transition-colors z-[110] bg-white/10 p-2 rounded-full backdrop-blur-md">
            <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
        </button>

        <div class="relative max-w-5xl w-full flex flex-col items-center animate-scale-in" @click.stop>
            <img :src="getPhotoUrl(selectedPhoto.s3Key)" :alt="selectedPhoto.description" 
                 class="max-h-[75vh] w-auto rounded-xl shadow-2xl border border-white/10 object-contain" />
            
            <div class="mt-6 text-center text-white space-y-2">
                <h4 class="text-2xl font-bold tracking-tight">{{ selectedPhoto.description || 'Evento Beducation' }}</h4>
                <p class="text-white/50 text-sm italic">{{ new Date(selectedPhoto.uploadedAt).toLocaleDateString('es-ES', { day: 'numeric', month: 'long', year: 'numeric' }) }}</p>
                
                <button @click="selectedPhoto = null" class="mt-4 px-6 py-2 bg-white/10 hover:bg-white/20 text-white text-sm font-bold rounded-full transition-all border border-white/10">
                    Cerrar Vista
                </button>
            </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../store/auth';
import api from '../../services/api';

const router = useRouter();
const authStore = useAuthStore();
const activeTab = ref('gallery');
const hasAccess = ref(false);
const myRequest = ref(null);
const photos = ref([]);
const pendingRequests = ref([]);
const loading = ref(false);
const loadingPhotos = ref(false);
const showUploadModal = ref(false);
const selectedPhoto = ref(null);

// Upload state
const fileInput = ref(null);
const selectedFiles = ref([]);
const uploadDescription = ref('');
const uploading = ref(false);

onMounted(async () => {
    if (!authStore.user) {
        await authStore.fetchLocalUserProfile();
    }
    init();
});

const init = async () => {
    if (authStore.role === 'ADMIN') {
        hasAccess.value = true;
        fetchPhotos();
        fetchPendingRequests();
    } else {
        await checkAccessStatus();
        if (hasAccess.value) {
            fetchPhotos();
        } else {
            fetchMyRequest();
        }
    }
};

const checkAccessStatus = async () => {
    try {
        const res = await api.get('/gallery/photos/check-access');
        hasAccess.value = res.hasAccess;
    } catch (e) {
        console.error("Error checking gallery access:", e);
    }
};

const fetchMyRequest = async () => {
    try {
        myRequest.value = await api.get('/gallery/access/my-request');
    } catch (e) {
        if (e.status !== 404) console.warn("Error fetching my gallery request:", e);
        myRequest.value = null;
    }
};

const requestAccess = async () => {
    loading.value = true;
    try {
        await api.post('/gallery/access/request');
        await fetchMyRequest();
    } catch (e) {
        alert("No se pudo enviar la solicitud: " + e.message);
    } finally {
        loading.value = false;
    }
};

const fetchPhotos = async () => {
    loadingPhotos.value = true;
    try {
        photos.value = await api.get('/gallery/photos');
    } catch (e) {
        console.error("Error fetching photos:", e);
    } finally {
        loadingPhotos.value = false;
    }
};

const fetchPendingRequests = async () => {
    if (authStore.role !== 'ADMIN') return;
    try {
        const res = await api.get('/gallery/access/admin/pending');
        pendingRequests.value = res.content || [];
    } catch (e) {
        console.error("Error fetching pending requests:", e);
    }
};

const processRequest = async (requestId, approve) => {
    try {
        await api.patch(`/gallery/access/admin/process/${requestId}`, null, {
            params: { approve }
        });
        await fetchPendingRequests();
    } catch (e) {
        alert("Error al procesar solicitud: " + e.message);
    }
};

const onFilesSelected = (e) => {
    selectedFiles.value = Array.from(e.target.files);
};

const handleUpload = async () => {
    uploading.value = true;
    try {
        const formData = new FormData();
        selectedFiles.value.forEach(file => {
            formData.append('files', file);
        });
        formData.append('description', uploadDescription.value);

        await api.post('/gallery/photos', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });

        showUploadModal.value = false;
        selectedFiles.value = [];
        uploadDescription.value = '';
        fetchPhotos();
    } catch (e) {
        alert("Error al subir fotos: " + e.message);
    } finally {
        uploading.value = false;
    }
};

const deletePhoto = async (id) => {
    if (!confirm('¿Seguro que deseas eliminar esta foto permanentemente?')) return;
    try {
        await api.delete(`/gallery/photos/${id}`);
        fetchPhotos();
    } catch (e) {
        alert("No se pudo eliminar la foto.");
    }
};

/**
 * Resuelve la URL de la imagen. 
 * En modo simulación (localhost), aprovecharemos una URL de placeholder.
 * En prod, construiría la URL de CloudFront o S3.
 */
const getPhotoUrl = (s3Key) => {
    // Si estamos en localhost o es una simulación
    if (s3Key.includes('gallery/')) {
        // Limpiar la key para que Picsum no la interprete como segmentos de ruta
        const cleanSeed = s3Key.replace(/\//g, '_');
        // Generar un placeholder bonito basado en el ID o key para demo si no hay AWS real
        return `https://picsum.photos/seed/${cleanSeed}/800/800`;
    }
    return `https://beducation-files.s3.eu-west-1.amazonaws.com/${s3Key}`;
};

const goBackToDashboard = () => {
    const role = authStore.role;
    if (role === 'STUDENT') router.push('/student/dashboard');
    else if (role === 'SCHOOL') router.push('/school/dashboard');
    else if (role === 'COMPANY') router.push('/company/dashboard');
    else if (role === 'ADMIN') router.push('/admin/dashboard');
    else router.push('/');
};
</script>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
