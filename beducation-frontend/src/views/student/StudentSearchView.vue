<template>
  <div class="space-y-6">
    <!-- Header de Búsqueda Minimalista -->
    <div class="section-card">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div>
          <h1 class="text-2xl font-bold text-slate-900 tracking-tight">Catálogo de Vacantes</h1>
          <p class="text-sm text-slate-500 mt-1">Encuentre su próximo destino profesional internacional.</p>
        </div>
        
        <div class="flex flex-wrap gap-3">
            <select v-model="filters.country" class="input-field max-w-[160px]">
                <option value="">Cualquier País</option>
                <option value="DE">Alemania</option>
                <option value="IE">Irlanda</option>
                <option value="NL">Países Bajos</option>
                <option value="ES">España</option>
                <option value="IT">Italia</option>
            </select>

            <input 
              type="text" 
              v-model="filters.keyword" 
              placeholder="Habilidades (Java, SEO...)"
              @keyup.enter="performSearch"
              class="input-field max-w-[200px]"
            >

            <button @click="performSearch" class="btn-primary text-sm px-6">
                Filtrar
            </button>
        </div>
      </div>
    </div>

    <!-- Resultados -->
    <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
       <div v-for="n in 6" :key="n" class="h-48 bg-slate-50 border border-slate-100 animate-pulse rounded-xl"></div>
    </div>

    <div v-else-if="offers.length === 0" class="text-center py-20 section-card border-dashed">
        <p class="text-slate-400 font-medium italic">No se han encontrado ofertas con los criterios seleccionados.</p>
        <button @click="resetFilters" class="text-slate-900 mt-2 font-bold text-xs uppercase tracking-widest border-b border-slate-900 pb-0.5">Ver Todo</button>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="offer in offers" :key="offer.id" 
             @click="goToOffer(offer.id)"
             class="section-card hover:border-slate-400 transition-colors cursor-pointer group">
            
            <div class="flex justify-between items-start mb-4">
               <span class="text-[10px] font-bold px-2 py-0.5 bg-slate-100 text-slate-500 rounded uppercase tracking-widest">
                   {{ offer.country }}
               </span>
               <span class="text-[10px] text-slate-300 font-mono">#{{ offer.id }}</span>
            </div>

            <h3 class="text-lg font-bold text-slate-900 leading-tight mb-1">
                {{ offer.title }}
            </h3>
            <p class="text-xs text-slate-500 mb-6 uppercase tracking-wider font-bold">
                {{ offer.company?.name || 'EMPRESA COLABORADORA' }}
            </p>

            <div class="flex items-center gap-2 mb-6 text-[10px] font-bold text-slate-400 uppercase tracking-tighter">
                <span>{{ offer.spotsAvailable }} vacantes</span>
                <span class="w-1 h-1 bg-slate-200 rounded-full"></span>
                <span>{{ offer.city }}</span>
            </div>

            <div class="flex justify-between items-center pt-4 border-t border-slate-50">
               <span class="text-[10px] font-bold uppercase tracking-widest text-slate-400 group-hover:text-slate-900 transition-colors">
                   Ver Detalles →
               </span>
            </div>
        </div>
    </div>
  </div>
</template>

<script setup>
/**
 * LÓGICA: Buscador de Vacantes para Estudiantes
 * --------------------------------------------
 * Permite a los alumnos filtrar ofertas por país, palabras clave y tipo de formación.
 */
import { ref, onMounted, reactive, watch } from 'vue';
import { useRouter } from 'vue-router';
import api from '../../services/api';

const router = useRouter();
const offers = ref([]);       // Lista de ofertas recuperadas
const loading = ref(true);     // Estado de carga para el skeleton

// Filtros reactivos vinculados a los inputs de la UI
const filters = reactive({
    country: '',
    keyword: '',
    educTypeId: null
});

/**
 * Realiza la llamada a la API con los filtros actuales.
 * Utiliza el endpoint '/opportunities/search' que soporta paginación y filtrado dinámico.
 */
const loadOffers = async () => {
    loading.value = true;
    try {
        const res = await api.get('/opportunities/search', {
            params: {
               country: filters.country || undefined,
               keyword: filters.keyword || undefined,
               educTypeId: filters.educTypeId || undefined
            }
        });
        offers.value = res.content || []; // El backend devuelve una estructura Page de Spring Data
    } catch (e) {
        console.error("Error al buscar ofertas:", e);
    } finally {
        loading.value = false;
    }
};

const educTypes = ref([]);

/**
 * Carga inicial: Obtiene tipos de educación y luego carga las ofertas iniciales.
 */
const loadInitialData = async () => {
    try {
        const res = await api.get('/education-types');
        educTypes.value = res || [];
    } catch (e) {
        console.warn("No se pudieron cargar los tipos de educación:", e);
    }
    loadOffers();
};

onMounted(loadInitialData);

/**
 * Ejecuta la búsqueda (usado en el botón y con ENTER en el buscador).
 */
const performSearch = () => {
    loadOffers();
};

/**
 * Limpia los filtros y recarga la lista completa.
 */
const resetFilters = () => {
    filters.country = '';
    filters.keyword = '';
    loadOffers();
};

/**
 * Navega al detalle de una oferta específica.
 */
const goToOffer = (id) => {
    router.push(`/student/offers/${id}`);
};

// Observadores para recarga automática al cambiar el país o el tipo de estudio
watch(() => filters.country, loadOffers);
watch(() => filters.educTypeId, loadOffers);
</script>

