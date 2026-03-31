<template>
  <div class="space-y-6 animate-fade-in pb-12">
    <!-- Search Header & Filters -->
    <div class="glass-card p-6 md:p-8 rounded-3xl border-b-4 border-emerald-500 shadow-xl shadow-slate-200/50">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div>
          <h1 class="text-3xl font-extrabold text-slate-900">Catálogo de Oportunidades</h1>
          <p class="text-slate-500 mt-1 font-medium italic">Encuentra tu próximo destino profesional internacional.</p>
        </div>
        
        <div class="flex flex-wrap gap-3">
            <div class="relative">
                <select v-model="filters.country" class="appearance-none bg-slate-50 border border-slate-200 text-slate-700 py-3 px-4 pr-10 rounded-2xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 outline-none transition-all font-medium min-w-[140px]">
                    <option value="">Cualquier País</option>
                    <option value="DE">Alemania</option>
                    <option value="IE">Irlanda</option>
                    <option value="NL">Países Bajos</option>
                    <option value="ES">España</option>
                    <option value="IT">Italia</option>
                    <option value="FR">Francia</option>
                    <option value="AT">Austria</option>
                    <option value="BE">Bélgica</option>
                    <option value="PT">Portugal</option>
                    <option value="CH">Suiza</option>
                    <option value="GB">Reino Unido</option>
                </select>
                <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-slate-400">
                    <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>
                </div>
            </div>

            <div class="relative">
                <input 
                  type="text" 
                  v-model="filters.keyword" 
                  placeholder="Habilidades, tech, rol..."
                  @keyup.enter="performSearch"
                  class="bg-slate-50 border border-slate-200 text-slate-700 py-3 px-4 rounded-2xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 outline-none transition-all font-medium md:min-w-[200px]"
                >
            </div>

            <div class="relative">
                <select v-model="filters.educTypeId" class="appearance-none bg-slate-50 border border-slate-200 text-slate-700 py-3 px-4 pr-10 rounded-2xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 outline-none transition-all font-medium min-w-[140px]">
                    <option :value="null">Cualquier Título</option>
                    <option v-for="type in educTypes" :key="type.id" :value="type.id">{{ type.name }}</option>
                </select>
                <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-slate-400">
                    <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>
                </div>
            </div>

            <button @click="performSearch" class="btn-primary px-8 rounded-2xl shadow-lg shadow-primary-500/20 active:scale-95 transition-transform">
                Filtrar
            </button>
        </div>
      </div>
    </div>

    <!-- Results Grid -->
    <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
       <div v-for="n in 6" :key="n" class="h-64 bg-slate-100 animate-pulse rounded-3xl"></div>
    </div>

    <div v-else-if="offers.length === 0" class="text-center py-20 glass-card bg-white/50 rounded-3xl border border-dashed border-slate-300">
        <svg class="w-16 h-16 text-slate-300 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
        <p class="text-xl font-bold text-slate-400">No hemos encontrado ninguna oferta con esos filtros.</p>
        <button @click="resetFilters" class="text-primary-600 mt-2 font-medium">Ver todas las ofertas</button>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="offer in offers" :key="offer.id" 
             @click="goToOffer(offer.id)"
             class="group relative bg-white p-6 rounded-3xl border border-slate-100 hover:border-primary-300 hover:shadow-2xl hover:shadow-primary-900/5 transition-all cursor-pointer overflow-hidden transform hover:-translate-y-2">
            
            <!-- Float indicators -->
            <div class="flex justify-between items-start mb-4">
               <div class="w-12 h-12 bg-slate-50 rounded-xl border border-slate-100 flex items-center justify-center p-2 shadow-inner group-hover:bg-primary-50 group-hover:border-primary-100 transition-colors">
                  <span class="text-lg font-black text-slate-300 group-hover:text-primary-500">{{ offer.company?.name?.charAt(0) || 'B' }}</span>
               </div>
               <span class="text-xs font-black px-3 py-1 bg-slate-100 text-slate-500 rounded-full group-hover:bg-emerald-500 group-hover:text-white transition-colors uppercase tracking-widest">
                   {{ offer.country }}
               </span>
            </div>

            <h3 class="text-xl font-extrabold text-slate-800 leading-tight mb-2 group-hover:text-primary-700 transition-colors">
                {{ offer.title }}
            </h3>
            <p class="text-sm text-slate-500 mb-6 font-medium line-clamp-2">
                {{ offer.company?.name || 'Empresa Colaboradora' }}
            </p>

            <div class="flex items-center gap-2 mb-6">
                <span class="text-xs bg-slate-50 text-slate-400 px-2.5 py-1 rounded-lg border border-slate-100">Full Time</span>
                <span class="text-xs bg-slate-50 text-slate-400 px-2.5 py-1 rounded-lg border border-slate-100">{{ offer.spotsAvailable }} vacantes</span>
            </div>

            <div class="flex justify-between items-center pt-4 border-t border-slate-50">
               <span class="text-primary-600 font-bold text-sm flex items-center gap-1 group-hover:gap-2 transition-all">
                   Ver detalles
                   <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
               </span>
            </div>
            
            <!-- Background Accent -->
            <div class="absolute -right-12 -bottom-12 w-32 h-32 bg-primary-500/5 rounded-full blur-2xl group-hover:bg-primary-500/10 transition-colors"></div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch } from 'vue';
import { useRouter } from 'vue-router';
import api from '../../services/api';

const router = useRouter();
const offers = ref([]);
const loading = ref(true);

const filters = reactive({
    country: '',
    keyword: '',
    educTypeId: null
});

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
        // Flyway V2/V3 seed results should be here
        offers.value = res.content || [];
    } catch (e) {
        console.error("Error searching offers:", e);
    } finally {
        loading.value = false;
    }
};

const educTypes = ref([]);

const loadInitialData = async () => {
    try {
        const res = await api.get('/education-types');
        educTypes.value = res;
    } catch (e) {
        console.warn("Could not load education types:", e);
        // Fallback
        educTypes.value = [
            { id: 1, name: 'FP Básica' },
            { id: 2, name: 'Grado Medio' },
            { id: 3, name: 'Grado Superior (CFGS)' },
            { id: 4, name: 'Grado Universitario' }
        ];
    }
    loadOffers();
};

onMounted(loadInitialData);

const performSearch = () => {
    loadOffers();
};

const resetFilters = () => {
    filters.country = '';
    filters.keyword = '';
    loadOffers();
};

const goToOffer = (id) => {
    router.push(`/student/offers/${id}`);
};

// Automatical search when selects change
watch(() => filters.country, loadOffers);
watch(() => filters.educTypeId, loadOffers);
</script>
