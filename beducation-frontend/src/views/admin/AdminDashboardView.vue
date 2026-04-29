<template>
  <div class="space-y-8">
    <!-- Header Admin Simplificado -->
    <div class="bg-white border border-slate-200 p-8 rounded-2xl flex flex-col sm:flex-row items-center justify-between gap-6">
      <div>
        <h2 class="text-2xl font-bold text-slate-900 tracking-tight">Panel de Control Global</h2>
        <p class="text-slate-500 mt-1 text-sm flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-emerald-500"></span>
            Estado del sistema: Operativo
        </p>
      </div>

      <div class="flex items-center gap-8 border-l border-slate-100 pl-8">
        <div class="text-center">
            <span class="block text-2xl font-bold text-slate-900">{{ stats.activeSchools }}</span>
            <span class="text-[10px] text-slate-400 font-bold uppercase tracking-widest">Escuelas</span>
        </div>
        <div class="text-center">
            <span class="block text-2xl font-bold text-slate-900">{{ stats.activeStudents }}</span>
            <span class="text-[10px] text-slate-400 font-bold uppercase tracking-widest">Alumnos</span>
        </div>
        <div class="border-l border-slate-100 pl-8">
            <button @click="$router.push('/gallery')" class="btn-primary text-xs flex items-center gap-2 bg-sky-600 hover:bg-sky-700">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                Galería Eventos
            </button>
        </div>
      </div>
    </div>

    <!-- Tabs Content -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 p-4">
      <!-- Custom simple tabs -->
      <div class="flex border-b border-slate-200 overflow-x-auto">
        <button v-for="tab in tabs" :key="tab.id" @click="currentTab = tab.id"
                class="px-6 py-4 font-medium text-sm transition-colors whitespace-nowrap border-b-2"
                :class="currentTab === tab.id ? 'border-primary-600 text-primary-600' : 'border-transparent text-slate-500 hover:text-slate-800 hover:border-slate-300'">
          {{ tab.label }}
          <span v-if="tab.badge > 0" class="ml-2 bg-rose-500 text-white text-xs px-2 py-0.5 rounded-full font-bold">
            {{ tab.badge }}
          </span>
        </button>
      </div>

      <!-- ANALYTICS FUNNEL VIEW -->
      <div v-show="currentTab === 'analiticas'" class="p-6">
        <h3 class="text-xl font-bold text-slate-800 mb-6">Pipeline Global</h3>
        
        <div class="max-w-4xl mx-auto space-y-4">
            <!-- Funnel Stages -->
            <div v-for="(stage, index) in funnelStages" :key="stage.key" 
                 class="relative flex items-center group">
                <!-- Label & Bar -->
                <div class="w-40 text-right pr-4 text-xs font-bold text-slate-400 uppercase tracking-widest">{{ stage.label }}</div>
                <div class="flex-1 bg-slate-50 rounded-xl h-14 border border-slate-100 relative overflow-hidden flex items-center px-4">
                    <div class="absolute inset-y-0 left-0 transition-all duration-1000 ease-out"
                         :style="{ width: getPercentage(stage.key) + '%', backgroundColor: stage.color }"></div>
                    <span class="relative z-10 font-black text-slate-800">{{ funnelData[stage.key] || 0 }}</span>
                    <span class="relative z-10 ml-auto font-bold text-[10px] text-slate-400 opacity-0 group-hover:opacity-100 transition-opacity">
                        {{ getPercentage(stage.key) }}% del total
                    </span>
                </div>
                <!-- Connector arrow (except last) -->
                <div v-if="index < funnelStages.length - 1" class="absolute left-60 -bottom-3 z-20 text-slate-200">
                    <svg class="h-4 w-4" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M14.707 12.707a1 1 0 01-1.414 0L10 9.414l-3.293 3.293a1 1 0 01-1.414-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 010 1.414z" clip-rule="evenodd" transform="rotate(180 10 10)"></path></svg>
                </div>
            </div>
        </div>

        <div class="mt-12 grid grid-cols-1 md:grid-cols-3 gap-6">
            <div class="p-6 bg-emerald-50 rounded-2xl border border-emerald-100">
                <p class="text-[10px] font-black text-emerald-600 uppercase mb-1">Confirmaciones Totales</p>
                <p class="text-3xl font-black text-emerald-800">{{ funnelData['CONFIRMED'] || 0 }}</p>
                <p class="text-xs text-emerald-600 mt-1">Acuerdos finalizados con éxito.</p>
            </div>
            <div class="p-6 bg-rose-50 rounded-2xl border border-rose-100">
                <p class="text-[10px] font-black text-rose-600 uppercase mb-1">Candidaturas Rechazadas</p>
                <p class="text-3xl font-black text-rose-800">{{ funnelData['REJECTED'] || 0 }}</p>
                <p class="text-xs text-rose-600 mt-1">Incluye descartes de empresas y admin.</p>
            </div>
        </div>
      </div>

      <!-- Panel VERIFICAR (Combinado) -->
      <div v-show="currentTab === 'escuelas'" class="p-6">
        <h3 class="text-lg font-bold text-slate-800 mb-4">Solicitudes de registro</h3>
        
        <!-- Escuelas Pendientes -->
        <div v-if="pendingSchools.length > 0" class="mb-8">
            <h4 class="text-sm font-semibold text-slate-500 uppercase mb-3 px-2">Instituciones Educativas</h4>
            <div class="overflow-x-auto rounded-xl border border-slate-200">
                <table class="w-full text-sm text-left text-slate-600">
                    <tbody class="bg-white">
                        <tr v-for="school in pendingSchools" :key="school.id" class="border-b border-slate-100 hover:bg-slate-50">
                            <td class="px-6 py-4">
                                <div class="font-medium text-slate-900">{{ school.name }}</div>
                                <div class="text-xs text-slate-400">{{ school.user?.email }}</div>
                            </td>
                            <td class="px-6 py-4">{{ school.city }}, {{ school.country }}</td>
                            <td class="px-6 py-4 text-right flex justify-end gap-2">
                                <button @click="processEntity('school', school.id, true)" class="bg-emerald-100 hover:bg-emerald-200 text-emerald-700 px-3 py-1.5 rounded-md font-medium text-xs transition-colors">Aprobar</button>
                                <button @click="processEntity('school', school.id, false)" class="bg-rose-100 hover:bg-rose-200 text-rose-700 px-3 py-1.5 rounded-md font-medium text-xs transition-colors">Rechazar</button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Empresas Pendientes -->
        <div v-if="pendingCompanies.length > 0">
            <h4 class="text-sm font-semibold text-slate-500 uppercase mb-3 px-2">Empresas Receptoras</h4>
            <div class="overflow-x-auto rounded-xl border border-slate-200">
                <table class="w-full text-sm text-left text-slate-600">
                    <tbody class="bg-white">
                        <tr v-for="company in pendingCompanies" :key="company.id" class="border-b border-slate-100 hover:bg-slate-50">
                            <td class="px-6 py-4">
                                <div class="font-medium text-slate-900">{{ company.name }}</div>
                                <div class="text-xs text-slate-400">{{ company.user?.email }}</div>
                            </td>
                            <td class="px-6 py-4">{{ company.sector }}</td>
                            <td class="px-6 py-4 text-right flex justify-end gap-2">
                                <button @click="processEntity('company', company.id, true)" class="bg-emerald-100 hover:bg-emerald-200 text-emerald-700 px-3 py-1.5 rounded-md font-medium text-xs transition-colors">Aprobar</button>
                                <button @click="processEntity('company', company.id, false)" class="bg-rose-100 hover:bg-rose-200 text-rose-700 px-3 py-1.5 rounded-md font-medium text-xs transition-colors">Rechazar</button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <div v-if="pendingSchools.length === 0 && pendingCompanies.length === 0" class="text-center py-12 text-slate-400">
            No hay solicitudes urgentes pendientes de validación.
        </div>
      </div>

      <!-- GESTIÓN GLOBAL: ESTUDIANTES -->
      <div v-show="currentTab === 'control_estudiantes'" class="p-6">
        <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-6">
            <h3 class="text-lg font-bold text-slate-800">Estudiantes</h3>
            
            <div class="flex flex-1 max-w-md w-full gap-2">
                <div class="relative flex-1">
                    <svg class="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
                    <input v-model="searchQueryStudents" type="text" placeholder="Buscar por nombre o email..." class="w-full bg-slate-50 border border-slate-200 rounded-lg py-2 pl-9 pr-4 text-sm focus:bg-white focus:ring-2 focus:ring-primary-500/20 transition-all">
                </div>
                <button @click="showInviteForm = !showInviteForm" class="bg-primary-600 hover:bg-primary-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-all flex items-center gap-2 shrink-0">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" /></svg>
                    Invitar
                </button>
            </div>
        </div>

        <!-- Formulario de Invitación -->
        <div v-if="showInviteForm" class="space-y-6 animate-in fade-in slide-in-from-top-4 duration-300">
            <!-- Manual Invitation -->
            <div class="bg-slate-50 p-6 rounded-xl border border-slate-200">
                <h4 class="text-sm font-bold text-slate-700 mb-4 flex items-center gap-2">
                    <svg class="w-4 h-4 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>
                    Ficha Individual
                </h4>
                <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
                    <input v-model="inviteForm.firstName" type="text" placeholder="Nombre" class="bg-white border text-sm rounded-lg p-2.5 focus:ring-primary-500">
                    <input v-model="inviteForm.lastName" type="text" placeholder="Apellidos" class="bg-white border text-sm rounded-lg p-2.5 focus:ring-primary-500">
                    <input v-model="inviteForm.email" type="email" placeholder="Email Invitación" class="bg-white border text-sm rounded-lg p-2.5 focus:ring-primary-500">
                    <select v-model="inviteForm.schoolId" class="bg-white border text-sm rounded-lg p-2.5">
                        <option value="">-- Seleccionar Escuela --</option>
                        <option v-for="s in allSchoolsList" :key="s.id" :value="s.id">{{ s.name }}</option>
                    </select>
                </div>
                <div class="mt-4 flex justify-end gap-3">
                    <button @click="showInviteForm = false" class="text-slate-500 text-sm font-medium">Cerrar Panel</button>
                    <button @click="submitInvitation" class="bg-primary-600 text-white px-6 py-2 rounded-lg text-sm font-bold">Enviar Invitación Manual</button>
                </div>
            </div>

                <!-- CSV IMPORT (NUEVO) -->
            <div class="bg-white p-6 rounded-xl border-2 border-dashed border-slate-200 relative group overflow-hidden">
                <div class="absolute inset-0 bg-primary-50 opacity-0 group-hover:opacity-5 transition-opacity pointer-events-none"></div>
                
                <div class="flex justify-between items-center mb-4">
                    <h4 class="text-sm font-bold text-slate-700 flex items-center gap-2">
                        <svg class="w-4 h-4 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" /></svg>
                        Importación Masiva (CSV)
                    </h4>
                    <button @click="downloadCsvTemplate" class="text-[10px] font-black uppercase tracking-widest text-primary-600 hover:text-primary-700 flex items-center gap-1.5 p-1 px-2 border border-primary-100 rounded-md bg-primary-50">
                        <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0L8 8m4-4v12" /></svg>
                        Descargar Plantilla
                    </button>
                </div>

                <div v-if="!csvPreview.length" class="flex flex-col md:flex-row gap-6 items-center">
                    <div class="flex-1 space-y-4">
                        <p class="text-sm text-slate-500">Sube un archivo <span class="font-mono bg-slate-100 px-1 rounded text-slate-700">.csv</span> con las columnas: <span class="font-medium text-slate-700 italic">firstName, lastName, email, educationTypeCode</span>.</p>
                        <select v-model="csvTargetSchoolId" class="w-full bg-slate-50 border text-sm rounded-lg p-3 font-medium active:border-primary-500 transition-all">
                            <option value="">-- PRIMERO SELECCIONE LA ESCUELA DESTINO --</option>
                            <option v-for="s in allSchoolsList" :key="s.id" :value="s.id">{{ s.name }} ({{ s.city }})</option>
                        </select>
                    </div>

                    <div 
                        class="w-full md:w-1/2 p-10 border-2 border-dashed border-slate-300 rounded-2xl flex flex-col items-center justify-center cursor-pointer hover:border-primary-400 hover:bg-slate-50 transition-all text-center"
                        @dragover.prevent="isDragging = true"
                        @dragleave.prevent="isDragging = false"
                        @drop.prevent="handleDrop"
                        @click="triggerFileInput"
                    >
                        <div class="w-12 h-12 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center mb-3">
                            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" /></svg>
                        </div>
                        <p class="text-sm font-bold text-slate-700">Añadir Archivo CSV</p>
                        <p class="text-xs text-slate-400 mt-1">Arrastra y suelta o haz clic</p>
                        <input type="file" ref="fileInput" @change="onFileChange" accept=".csv" class="hidden">
                    </div>
                </div>

                <!-- CSV Preview (If selected) -->
                <div v-else class="space-y-4">
                    <div class="flex items-center justify-between text-sm">
                        <span class="font-bold text-slate-700">Detectados {{ csvPreview.length }} alumnos para la escuela <span class="text-primary-600">{{ getSchoolName(csvTargetSchoolId) }}</span></span>
                        <button @click="resetCsv" class="text-rose-500 hover:underline font-medium text-xs">Cambiar archivo</button>
                    </div>
                    <div class="overflow-x-auto border border-slate-100 rounded-lg max-h-40 overflow-y-auto">
                        <table class="w-full text-xs text-left">
                            <thead class="bg-slate-50 text-slate-400 uppercase font-black tracking-widest sticky top-0">
                                <tr>
                                    <th class="px-3 py-2">Nombre</th>
                                    <th class="px-3 py-2">Apellidos</th>
                                    <th class="px-3 py-2">Email</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="(row, i) in csvPreview.slice(0, 10)" :key="i" class="border-t border-slate-50">
                                    <td class="px-3 py-2">{{ row.firstName }}</td>
                                    <td class="px-3 py-2">{{ row.lastName }}</td>
                                    <td class="px-3 py-2 text-slate-400 italic">{{ row.email }}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <div class="flex justify-end gap-3 pt-4 border-t border-slate-50">
                        <button @click="resetCsv" class="text-slate-400 text-xs font-bold uppercase tracking-widest">Limpiar</button>
                        <button @click="submitBulkImport" :disabled="!csvTargetSchoolId || isImporting" class="bg-emerald-600 hover:bg-emerald-700 text-white px-8 py-3 rounded-xl font-black text-sm uppercase tracking-widest transition-all shadow-lg hover:shadow-emerald-200 disabled:opacity-50">
                            {{ isImporting ? 'PROCESANDO...' : 'EJECUTAR IMPORTACIÓN MASIVA' }}
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <div class="overflow-x-auto rounded-xl border border-slate-200">
            <table class="w-full text-sm text-left text-slate-600">
                <thead class="bg-slate-50 text-slate-800 uppercase text-xs border-b">
                    <tr>
                        <th class="px-6 py-4">Estudiante</th>
                        <th class="px-6 py-4">Escuela / Origen</th>
                        <th class="px-6 py-4">Estado</th>
                        <th class="px-6 py-4 text-right">Acciones</th>
                    </tr>
                </thead>
                <tbody class="bg-white">
                    <tr v-for="std in filteredStudents" :key="std.id" class="border-b border-slate-100">
                        <td class="px-6 py-4">
                            <div class="font-medium text-slate-900">{{ std.firstName }} {{ std.lastName }}</div>
                            <div class="text-xs text-slate-400">{{ std.invitationEmail }}</div>
                        </td>
                        <td class="px-6 py-4 text-xs">{{ std.school?.name || '---' }}</td>
                        <td class="px-6 py-4">
                            <span :class="std.user?.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'" class="px-2 py-1 rounded text-[10px] font-bold">
                                {{ std.user?.status || 'SIN CUENTA' }}
                            </span>
                        </td>
                        <td class="px-6 py-4 text-right flex justify-end gap-2">
                             <button @click="updateUserStatus(std.user?.id, std.user?.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE')" class="text-slate-400 hover:text-rose-600 transition-colors" title="Baneo/Activar">
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728L5.636 5.636" /></svg>
                             </button>
                             <button @click="deleteUser(std.user?.id)" class="text-slate-400 hover:text-red-600" title="Eliminar definitivamente">
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                             </button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
      </div>

      <!-- GESTIÓN GLOBAL: ESCUELAS -->
      <div v-show="currentTab === 'control_escuelas'" class="p-6">
        <div class="flex justify-between items-center mb-4">
            <h3 class="text-lg font-bold text-slate-800">Instituciones</h3>
            <div class="relative w-64">
                <svg class="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
                <input v-model="searchQuerySchools" type="text" placeholder="Buscar escuela..." class="w-full bg-slate-50 border border-slate-200 rounded-lg py-1.5 pl-9 pr-4 text-sm focus:bg-white focus:ring-2 focus:ring-primary-500/20 transition-all">
            </div>
        </div>
        <div class="overflow-x-auto rounded-xl border border-slate-200">
            <table class="w-full text-sm text-left text-slate-600">
                <thead class="bg-slate-50 text-slate-800 uppercase text-xs border-b">
                    <tr>
                        <th class="px-6 py-4">Escuela</th>
                        <th class="px-6 py-4">Ubicación</th>
                        <th class="px-6 py-4 text-right">Acciones</th>
                    </tr>
                </thead>
                <tbody class="bg-white">
                    <tr v-for="item in filteredSchools" :key="item.id" class="border-b border-slate-100">
                        <td class="px-6 py-4">
                            <div class="font-medium text-slate-900">{{ item.name }}</div>
                            <div class="text-xs text-slate-400">{{ item.user?.email }}</div>
                        </td>
                        <td class="px-6 py-4 text-xs">{{ item.city }}, {{ item.country }}</td>
                        <td class="px-6 py-4 text-right flex justify-end gap-2">
                             <button @click="deleteUser(item.user?.id)" class="bg-rose-50 text-rose-600 p-2 rounded hover:bg-rose-100 transition-colors text-xs font-bold">Eliminar Inst.</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
      </div>

      <!-- GESTIÓN GLOBAL: EMPRESAS -->
      <div v-show="currentTab === 'control_empresas'" class="p-6">
        <div class="flex justify-between items-center mb-4">
            <h3 class="text-lg font-bold text-slate-800">Empresas</h3>
            <div class="relative w-64">
                <svg class="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
                <input v-model="searchQueryCompanies" type="text" placeholder="Buscar empresa..." class="w-full bg-slate-50 border border-slate-200 rounded-lg py-1.5 pl-9 pr-4 text-sm focus:bg-white focus:ring-2 focus:ring-primary-500/20 transition-all">
            </div>
        </div>
        <div class="overflow-x-auto rounded-xl border border-slate-200">
            <table class="w-full text-sm text-left text-slate-600">
                <thead class="bg-slate-50 text-slate-800 uppercase text-xs border-b">
                    <tr>
                        <th class="px-6 py-4">Empresa</th>
                        <th class="px-6 py-4">Categoría</th>
                        <th class="px-6 py-4 text-right">Acciones</th>
                    </tr>
                </thead>
                <tbody class="bg-white">
                    <tr v-for="item in filteredCompanies" :key="item.id" class="border-b border-slate-100">
                        <td class="px-6 py-4">
                            <div class="font-medium text-slate-900">{{ item.name }}</div>
                            <a :href="item.website" target="_blank" class="text-xs text-primary-600">{{ item.user?.email }}</a>
                        </td>
                        <td class="px-6 py-4 text-xs">{{ item.sector }}</td>
                        <td class="px-6 py-4 text-right flex justify-end gap-2">
                             <button @click="deleteUser(item.user?.id)" class="bg-rose-50 text-rose-600 p-2 rounded hover:bg-rose-100 transition-colors text-xs font-bold">Eliminar Empresa</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
      </div>

      <!-- PANEL GALLERY ACCESS -->
      <div v-show="currentTab === 'gallery_requests'" class="p-6">
        <h3 class="text-lg font-bold text-slate-800 mb-6">Solicitudes de Acceso a Galería Pendientes</h3>
        
        <div v-if="pendingGalleryRequests.length > 0" class="overflow-x-auto rounded-xl border border-slate-200">
            <table class="w-full text-sm text-left text-slate-600">
                <thead class="bg-slate-50 text-slate-800 uppercase text-xs border-b">
                    <tr>
                        <th class="px-6 py-4">Usuario</th>
                        <th class="px-6 py-4">Rol / Email</th>
                        <th class="px-6 py-4">Fecha Solicitud</th>
                        <th class="px-6 py-4 text-right">Acciones</th>
                    </tr>
                </thead>
                <tbody class="bg-white">
                    <tr v-for="req in pendingGalleryRequests" :key="req.id" class="border-b border-slate-100 hover:bg-slate-50">
                        <td class="px-6 py-4 font-bold text-slate-900">{{ req.user?.firstName || 'Usuario' }} {{ req.user?.lastName || '' }}</td>
                        <td class="px-6 py-4">
                            <div class="flex items-center gap-2">
                                <span class="bg-slate-100 px-2 py-1 rounded text-[10px] font-black uppercase">{{ req.user?.role }}</span>
                                <span class="text-xs text-slate-400">{{ req.user?.email }}</span>
                            </div>
                        </td>
                        <td class="px-6 py-4 text-xs text-slate-500">{{ new Date(req.requestedAt).toLocaleDateString() }}</td>
                        <td class="px-6 py-4 text-right flex justify-end gap-2">
                            <button @click="processGalleryAccess(req.id, true)" class="bg-emerald-500 hover:bg-emerald-600 text-white px-3 py-1.5 rounded-lg text-xs font-bold transition-all shadow-sm">Aprobar</button>
                            <button @click="processGalleryAccess(req.id, false)" class="text-rose-600 hover:bg-rose-50 px-3 py-1.5 rounded-lg text-xs font-medium border border-rose-100 transition-all">Rechazar</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
        <div v-else class="text-center py-20 bg-slate-50 border border-slate-200 border-dashed rounded-2xl">
            <div class="w-16 h-16 bg-white rounded-full flex items-center justify-center mx-auto mb-4 text-slate-200">
                <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
            </div>
            <p class="text-slate-400 font-medium">No hay peticiones de acceso pendientes.</p>
        </div>
      </div>

      <!-- PANEL PLACEMENTS (Stage 4) -->
      <div v-show="currentTab === 'placements'" class="p-6">
        <h3 class="text-lg font-bold text-slate-800 mb-4">Acuerdos Pendientes de Validación por el Admin</h3>
        
        <div v-if="pendingPlacements.length > 0" class="overflow-x-auto rounded-xl border border-slate-200">
            <table class="w-full text-sm text-left text-slate-600">
                <thead class="bg-slate-50 text-slate-800 uppercase text-xs border-b">
                    <tr>
                        <th class="px-6 py-4">Estudiante</th>
                        <th class="px-6 py-4">Oferta / Empresa</th>
                        <th class="px-6 py-4">Fase</th>
                        <th class="px-6 py-4 text-right">Acciones</th>
                    </tr>
                </thead>
                <tbody class="bg-white">
                    <tr v-for="app in pendingPlacements" :key="app.id" class="border-b border-slate-100 hover:bg-slate-50">
                        <td class="px-6 py-4">
                            <div class="font-medium text-slate-900">{{ app.student?.firstName }} {{ app.student?.lastName }}</div>
                            <div class="text-xs text-slate-400">{{ app.student?.invitationEmail }}</div>
                        </td>
                        <td class="px-6 py-4">
                            <div class="font-medium text-indigo-600">{{ app.opportunity?.title }}</div>
                            <div class="text-xs text-slate-500">{{ app.opportunity?.city }}, {{ app.opportunity?.country }}</div>
                        </td>
                        <td class="px-6 py-4">
                            <div class="flex items-center gap-2">
                                <span class="bg-indigo-100 text-indigo-700 px-2 py-1 rounded text-[10px] font-bold">ST-ACCEPTED</span>
                                <span v-if="app.compatibilityScore" class="bg-emerald-50 text-emerald-600 px-2 py-1 rounded text-[10px] font-black border border-emerald-100">{{ app.compatibilityScore }}% Match</span>
                            </div>
                            <div v-if="app.matchedKeywords" class="mt-1 text-[9px] text-slate-400 italic truncate max-w-[150px]">
                                Match en: {{ app.matchedKeywords }}
                            </div>
                        </td>
                        <td class="px-6 py-4 text-right flex justify-end gap-2">
                            <button @click="processPlacement(app.id, true)" class="bg-emerald-500 hover:bg-emerald-600 text-white px-3 py-1.5 rounded-md font-bold text-xs shadow-sm transition-all">Validar para Escuela</button>
                            <button @click="processPlacement(app.id, false)" class="text-rose-600 hover:bg-rose-50 px-3 py-1.5 rounded-md font-medium text-xs transition-all border border-rose-100">Rechazar</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
        <div v-else class="text-center py-12 text-slate-400">
            No hay convenios en espera de validación administrativa.
        </div>
      </div>

      <!-- Otros (Mantenimiento de secciones antiguas) -->
      <div v-show="currentTab === 'ofertas'" class="p-12 text-center text-slate-400">
          <svg class="w-16 h-16 mx-auto mb-4 opacity-20" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
          <p>Módulo de auditoría de {{ tabs.find(t=>t.id === currentTab).label }} activado en el Backend.</p>
          <p class="text-xs mt-2">Usa las nuevas pestañas de Gestión para controlar los perfiles actuales.</p>
      </div>
      
    </div>
  </div>
</template>

<script setup>
/**
 * DASHBOARD DE ADMINISTRACIÓN (Control Global)
 * --------------------------------------------
 * Panel maestro para los administradores del sistema BeDucation.
 * Centraliza la auditoría de:
 * 1. Analíticas Globales: Seguimiento del pipeline desde aplicación hasta confirmación.
 * 2. Validación de Cuentas: Aprobación de nuevas escuelas y empresas registradas.
 * 3. Gestión de Estudiantes: Invitaciones individuales e importaciones masivas (CSV).
 * 4. Control de Placements: Validación de la Etapa 4 previa a la firma del centro.
 * 5. Accesos a Galería: Supervisión de permisos para fotos de eventos.
 */
import { ref, onMounted, watch, computed } from 'vue';
import api from '../../services/api';
import { useNotificationStore } from '../../store/notifications';

const notifications = useNotificationStore();



const currentTab = ref('escuelas');
const tabs = ref([
    { id: 'analiticas', label: 'Analíticas Globales', badge: 0 },
    { id: 'escuelas', label: 'Verificar Nuevos', badge: 0 },
    { id: 'control_estudiantes', label: 'Gestión Estudiantes', badge: 0 },
    { id: 'control_escuelas', label: 'Gestión Escuelas', badge: 0 },
    { id: 'control_empresas', label: 'Gestión Empresas', badge: 0 },
    { id: 'gallery_requests', label: 'Accesos Galería', badge: 0 },
    { id: 'placements', label: 'Validaciones Finales', badge: 0 }
]);

const stats = ref({ activeSchools: 0, activeStudents: 0 });
const funnelData = ref({});
const funnelStages = [
    { key: 'APPLIED', label: 'Aplicaciones', color: '#f1f5f9' },
    { key: 'INTERESTED', label: 'Interesados', color: '#e0f2fe' },
    { key: 'OFFERED', label: 'Ofertas Realizadas', color: '#fef3c7' },
    { key: 'STUDENT_ACCEPTED', label: 'Aceptado Alumno', color: '#dcfce7' },
    { key: 'ADMIN_APPROVED', label: 'Validado Admin', color: '#ecfdf5' },
    { key: 'CONFIRMED', label: 'Confirmados', color: '#10b981' }
];

const pendingSchools = ref([]);
const pendingCompanies = ref([]);
const pendingPlacements = ref([]);

const allSchoolsList = ref([]);
const allSchools = ref([]);
const allCompanies = ref([]);
const allStudents = ref([]);

const showInviteForm = ref(false);
const inviteForm = ref({ firstName: '', lastName: '', email: '', schoolId: '' });

// CSV State
const fileInput = ref(null);
const isDragging = ref(false);
const isImporting = ref(false);
const csvTargetSchoolId = ref('');
const selectedCsvFile = ref(null);
const csvPreview = ref([]);
const pendingGalleryRequests = ref([]);

// Search Queries
const searchQueryStudents = ref('');
const searchQuerySchools = ref('');
const searchQueryCompanies = ref('');

const filteredStudents = computed(() => {
    const list = allStudents.value || [];
    if (!searchQueryStudents.value) return list;
    const query = searchQueryStudents.value.toLowerCase();
    return list.filter(s => 
        (s.firstName && s.firstName.toLowerCase().includes(query)) || 
        (s.lastName && s.lastName.toLowerCase().includes(query)) ||
        (s.invitationEmail && s.invitationEmail.toLowerCase().includes(query))
    );
});

const filteredSchools = computed(() => {
    const list = allSchools.value || [];
    if (!searchQuerySchools.value) return list;
    const query = searchQuerySchools.value.toLowerCase();
    return list.filter(s => 
        (s.name && s.name.toLowerCase().includes(query)) || 
        (s.city && s.city.toLowerCase().includes(query))
    );
});

const filteredCompanies = computed(() => {
    const list = allCompanies.value || [];
    if (!searchQueryCompanies.value) return list;
    const query = searchQueryCompanies.value.toLowerCase();
    return list.filter(c => 
        (c.name && c.name.toLowerCase().includes(query)) || 
        (c.sector && c.sector.toLowerCase().includes(query))
    );
});

const fetchDashboardData = async () => {
    try {
        const statsRes = await api.get('/admin/stats');
        stats.value = {
            activeSchools: statsRes.totalSchools || 0,
            activeStudents: statsRes.totalStudents || 0
        };
        funnelData.value = statsRes.funnel || {};
        
        tabs.value[1].badge = (statsRes.pendingSchoolsCount || 0) + (statsRes.pendingCompaniesCount || 0);
        tabs.value[5].badge = statsRes.pendingGalleryRequestsCount || 0;
        tabs.value[6].badge = statsRes.pendingStage4Count || 0;

        if (currentTab.value === 'escuelas') {
            const schoolsRes = await api.get('/admin/pending-schools');
            pendingSchools.value = schoolsRes.content || [];
            const compRes = await api.get('/admin/pending-companies');
            pendingCompanies.value = compRes.content || [];
        } else if (currentTab.value === 'control_escuelas') {
            const res = await api.get('/admin/all-schools');
            allSchools.value = res.content || [];
        } else if (currentTab.value === 'control_empresas') {
            const res = await api.get('/admin/all-companies');
            allCompanies.value = res.content || [];
        } else if (currentTab.value === 'control_estudiantes') {
            const res = await api.get('/admin/all-students');
            allStudents.value = res.content || [];
            if (allSchoolsList.value.length === 0) {
                const schoolsRes = await api.get('/admin/all-schools', { params: { size: 1000 } });
                allSchoolsList.value = schoolsRes.content || [];
            }
        } else if (currentTab.value === 'placements') {
            const res = await api.get('/admin/pending-placements');
            pendingPlacements.value = res.content || [];
        } else if (currentTab.value === 'gallery_requests') {
            const res = await api.get('/gallery/access/admin/pending');
            pendingGalleryRequests.value = res.content || [];
        }
    } catch (err) {
        console.error("Error cargando Dashboard:", err);
    }
};

const getPercentage = (key) => {
    const total = funnelData.value['APPLIED'] || 1;
    const value = funnelData.value[key] || 0;
    return Math.round((value / total) * 100);
};

onMounted(fetchDashboardData);
watch(currentTab, fetchDashboardData);

const submitInvitation = async () => {
    if (!inviteForm.value.email || !inviteForm.value.schoolId) {
        notifications.warning("Email y Escuela son obligatorios.");
        return;
    }
    try {
        await api.post('/admin/students/invite', null, { params: inviteForm.value });
        notifications.success("Invitación enviada con éxito.");
        showInviteForm.value = false;
        inviteForm.value = { firstName: '', lastName: '', email: '', schoolId: '' };
        fetchDashboardData();
    } catch (err) {
        notifications.error(err.message || "Error al invitar al alumno.");
    }
};

// CSV HANDLERS
const triggerFileInput = () => {
    if (!csvTargetSchoolId.value) {
        alert("Por favor, selecciona primero la escuela destino antes de subir el archivo.");
        return;
    }
    fileInput.value.click();
};

const onFileChange = (e) => {
    const file = e.target.files[0];
    if (file) processCsvFile(file);
};

const handleDrop = (e) => {
    isDragging.value = false;
    if (!csvTargetSchoolId.value) {
        alert("Por favor, selecciona primero la escuela destino antes de subir el archivo.");
        return;
    }
    const file = e.dataTransfer.files[0];
    if (file) processCsvFile(file);
};

const processCsvFile = (file) => {
    if (file.name.split('.').pop() !== 'csv') {
        alert("Por favor, sube un archivo .csv");
        return;
    }
    selectedCsvFile.value = file;
    const reader = new FileReader();
    reader.onload = (e) => {
        const text = e.target.result;
        const lines = text.split('\n').filter(l => l.trim());
        if (lines.length > 1) {
            csvPreview.value = lines.slice(1).map(l => {
                const parts = l.split(/[;,]/);
                return { firstName: parts[0], lastName: parts[1], email: parts[2] };
            });
        }
    };
    reader.readAsText(file);
};

const resetCsv = () => {
    selectedCsvFile.value = null;
    csvPreview.value = [];
    if (fileInput.value) fileInput.value.value = '';
};

const submitBulkImport = async () => {
    if (!selectedCsvFile.value || !csvTargetSchoolId.value) {
        alert("Selecciona un archivo y una escuela destino.");
        return;
    }

    isImporting.value = true;
    const formData = new FormData();
    formData.append('file', selectedCsvFile.value);
    
    try {
        await api.post('/admin/students/import-csv', formData, {
            params: { schoolId: csvTargetSchoolId.value },
            headers: { 'Content-Type': 'multipart/form-data' }
        });
        notifications.success(`Éxito: ${csvPreview.value.length} estudiantes invitados.`);
        resetCsv();
        fetchDashboardData();
    } catch (err) {
        notifications.error(err.response?.data?.message || "Error en la importación masiva.");
    } finally {
        isImporting.value = false;
    }
};

const getSchoolName = (id) => {
    const s = allSchoolsList.value.find(s => s.id == id);
    return s ? s.name : '...';
};

const updateUserStatus = async (userId, status) => {
    if (!userId) return;
    if (!confirm(`¿Seguro que quieres cambiar el estado de este usuario a ${status}?`)) return;
    try {
        await api.patch(`/admin/users/${userId}/status`, null, { params: { status } });
        fetchDashboardData();
    } catch (err) {
        alert("Error al actualizar el usuario.");
    }
};

const deleteUser = async (userId) => {
    if (!userId) return;
    if (!confirm("¿ESTÁS SEGURO? Esta acción es irreversible y borrará toda la ficha y accesos del usuario.")) return;
    try {
        await api.delete(`/admin/users/${userId}`);
        fetchDashboardData();
    } catch (err) {
        alert("Error al eliminar el usuario.");
    }
};

const processPlacement = async (appId, approve) => {
    try {
        await api.patch(`/admin/applications/${appId}/approve-stage4`, null, {
            params: { approve }
        });
        fetchDashboardData();
    } catch (err) {
        alert("Error al procesar el placement.");
    }
};

const processEntity = async (type, id, approve) => {
    try {
        const endpoint = type === 'company' ? 'companies' : `${type}s`;
        await api.patch(`/admin/${endpoint}/${id}/process`, null, {
            params: { approve }
        });
        fetchDashboardData();
    } catch (err) {
        alert("Error al procesar la solicitud.");
    }
};
const processGalleryAccess = async (requestId, approve) => {
    try {
        await api.patch(`/gallery/access/admin/process/${requestId}`, null, {
            params: { approve }
        });
        alert(approve ? "Acceso concedido con éxito." : "Solicitud de acceso rechazada.");
        fetchDashboardData();
    } catch (err) {
        alert("Error al procesar la solicitud de galería.");
    }
};

const downloadCsvTemplate = () => {
    const csvContent = "firstName,lastName,email,educationTypeCode\nJuan,Pérez,juan@ejemplo.com,FP_HIGH\nMaria,Lopez,maria@ejemplo.com,UNIVERSITY";
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.setAttribute("href", url);
    link.setAttribute("download", "plantilla_estudiantes.csv");
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
};
</script>
