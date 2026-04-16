<template>
  <div class="space-y-6">
    <div class="flex flex-col md:flex-row justify-between items-start md:items-center p-6 bg-white rounded-2xl shadow-sm border border-slate-200">
      <div>
        <h1 class="text-2xl font-bold text-slate-800">Mi Panel Institucional</h1>
        <p class="text-slate-500">Gestione alumnos, invitaciones y aprobaciones de prácticas en el extranjero.</p>
      </div>
      <div class="mt-4 md:mt-0 space-x-3 flex items-center">
        <button @click="$router.push('/gallery')" class="btn-secondary text-sm px-4 py-2 flex items-center gap-2">
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
            Fotos Eventos
        </button>
        <button @click="$refs.csvInput.click()" class="px-4 py-2 text-slate-500 hover:text-slate-800 font-medium text-sm transition-colors flex items-center gap-2">
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path></svg>
          Importar CSV
        </button>
        <input type="file" ref="csvInput" class="hidden" accept=".csv" @change="handleCsvUpload" />
        <button @click="openInviteModal" class="btn-primary text-sm px-4 py-2 flex items-center gap-2">
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
          Registrar Alumno
        </button>
      </div>
    </div>

    <!-- Pestañas & Funnel Analytics Simplificado -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <!-- Resumen Rápido -->
        <div class="md:col-span-1 space-y-4">
            <div @click="openValidationModal" class="section-card border-l-4 border-l-rose-500 cursor-pointer hover:bg-rose-50/10 transition-colors">
                <span class="label-caps">Validaciones Pendientes</span>
                <div class="flex items-baseline gap-2">
                    <span class="text-4xl font-bold text-slate-900">{{ pendingValidationsCount }}</span>
                    <span class="text-[10px] text-rose-600 font-bold uppercase">Urgente</span>
                </div>
            </div>

            <div class="section-card border-l-4 border-l-slate-200">
                <span class="label-caps">Alumnos Registrados</span>
                <div class="flex items-baseline gap-2">
                    <span class="text-4xl font-bold text-slate-900">{{ stats.registeredStudents }}</span>
                    <span class="text-slate-400 text-xs font-medium">/ {{ stats.totalStudents }} totales</span>
                </div>
            </div>
        </div>

        <!-- Funnel Global Simplificado -->
        <div class="section-card md:col-span-3">
            <div class="flex justify-between items-center mb-6">
                <div>
                    <span class="label-caps">Estado del Alumnado</span>
                    <h4 class="text-xs font-medium text-slate-500">Pipeline de candidaturas internacionales</h4>
                </div>
            </div>

            <div class="flex items-end gap-2 h-24 px-4">
                <div v-for="(count, status) in stats.funnel" :key="status" 
                     class="flex-1 group relative flex flex-col items-center justify-end h-full">
                    
                    <span class="text-[10px] font-bold text-slate-400 opacity-0 group-hover:opacity-100 transition-opacity mb-1">{{ count }}</span>
                    
                    <div class="w-full rounded-t bg-slate-100 group-hover:bg-slate-200 transition-colors" 
                         :style="{ height: (stats.totalApplications > 0 ? (count / stats.totalApplications * 100) : 0) + '%' }">
                    </div>

                    <div class="mt-2 text-center w-full">
                        <span class="text-[8px] font-bold text-slate-400 uppercase tracking-tighter block leading-none">
                            {{ status }}
                        </span>
                    </div>
                </div>
            </div>
            
            <div class="mt-6 pt-4 border-t border-slate-50 flex justify-between items-center">
                <p class="text-[9px] text-slate-400 font-medium">Distribución por estados (Stage 1 a 5)</p>
                <div class="flex items-center gap-3">
                    <div class="flex items-center gap-1.5">
                        <div class="w-1.5 h-1.5 rounded-full bg-slate-200"></div>
                        <span class="text-[9px] font-bold text-slate-400 uppercase">Proceso</span>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Lista de Estudiantes -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
        <div class="p-6 border-b border-slate-200 bg-slate-50/50 flex justify-between items-center">
            <h3 class="text-lg font-bold text-slate-800">Directorio de Alumnos</h3>
        </div>
        <div v-if="students.length === 0" class="p-12 text-center">
            <p class="text-slate-400 italic text-sm">No hay alumnos registrados en su institución.</p>
        </div>
        <div v-else class="overflow-x-auto text-left">
            <table class="w-full text-sm text-left text-slate-600">
                <thead class="bg-slate-50 text-slate-800 uppercase text-[10px] sm:text-xs">
                    <tr>
                        <th class="px-6 py-4 font-bold border-b border-slate-200">Nombre / Email</th>
                        <th class="px-6 py-4 font-bold border-b border-slate-200">Tipo Edu.</th>
                        <th class="px-6 py-4 font-bold border-b border-slate-200 text-center">Registro</th>
                        <th class="px-6 py-4 font-bold border-b border-slate-200 text-center">Perfil Completo</th>
                        <th class="px-6 py-4 font-bold text-right border-b border-slate-200">Acciones</th>
                    </tr>
                </thead>
                <tbody>
                     <tr v-for="std in students" :key="std.id" class="border-b border-slate-100 hover:bg-sky-50 transition-colors">
                         <td class="px-6 py-4 flex flex-col">
                             <span class="font-bold text-slate-800">{{ std.firstName }} {{ std.lastName }}</span>
                             <span class="text-xs text-slate-400 font-medium">{{ std.invitationEmail }}</span>
                         </td>
                         <td class="px-6 py-4">
                             {{ std.educationType?.code || 'Pte. Onboarding' }}
                         </td>
                         <td class="px-6 py-4 text-center">
                              <span v-if="std.registeredAt" class="text-emerald-600 bg-emerald-50 px-2 py-1 rounded text-xs font-medium border border-emerald-100">Registrado</span>
                              <span v-else class="text-amber-600 bg-amber-50 px-2 py-1 rounded text-xs font-medium border border-amber-100">Invitado (Pte)</span>
                         </td>
                         <td class="px-6 py-4 text-center">
                              <span v-if="std.profileComplete" class="inline-flex items-center gap-1 text-emerald-600 bg-emerald-50 px-2 py-1 rounded text-xs font-bold border border-emerald-200">
                                  <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"></path></svg> Sí
                              </span>
                              <span v-else class="text-slate-400 bg-slate-100 px-2 py-1 rounded text-xs">Falta CV/Datos</span>
                         </td>
                         <td class="px-6 py-4 text-right">
                             <button @click="openFunnelModal(std)" class="text-sky-600 font-medium hover:underline text-xs mr-3">Ver Progreso (Funnel)</button>
                             <button @click="deleteStudent(std.id)" class="text-slate-400 hover:text-rose-500 font-medium text-xs">Eliminar</button>
                         </td>
                     </tr>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Modal Invitación 1 a 1 -->
    <div v-if="showInviteModal" class="fixed inset-0 z-50 bg-slate-900/50 flex items-center justify-center p-4">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden">
        <div class="px-6 py-4 border-b border-slate-100 flex justify-between items-center">
          <h3 class="text-lg font-bold text-slate-800">Invitar Alumno (1 a 1)</h3>
          <button @click="closeInviteModal" class="text-slate-400 hover:text-slate-600 transition-colors">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
          </button>
        </div>
        <form @submit.prevent="submitInvite" class="p-6 space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Nombre</label>
              <input v-model="newStudent.firstName" type="text" required class="input-field" placeholder="Ej: Laura" />
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Apellidos</label>
              <input v-model="newStudent.lastName" type="text" required class="input-field" placeholder="Ej: Gómez" />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Correo Electrónico</label>
            <input v-model="newStudent.email" type="email" required class="input-field" placeholder="alumno@institucion.es" />
          </div>
          <div>
             <label class="block text-sm font-medium text-slate-700 mb-1">Tipo de Educación (Opcional)</label>
             <select v-model="newStudent.educationType" class="input-field">
                 <option value="">Seleccionar (Se define en onboarding)</option>
                 <option value="FP_HIGH">FP Grado Superior</option>
                 <option value="UNIVERSITY">Grado Universitario</option>
                 <option value="BOOTCAMP">Bootcamp</option>
             </select>
          </div>
          <div class="pt-4 flex justify-end gap-3">
            <button type="button" @click="closeInviteModal" class="px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50 rounded-lg transition-colors border border-slate-200">Cancelar</button>
            <button type="submit" :disabled="isInviting" class="btn-primary text-sm px-6 py-2 flex items-center gap-2">
              <span v-if="isInviting" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
              {{ isInviting ? 'Enviando...' : 'Enviar Invitación' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Progreso (Funnel) -->
    <div v-if="showFunnelModal" class="fixed inset-0 z-50 bg-slate-900/50 flex items-center justify-center p-4">
       <div class="bg-white rounded-2xl shadow-xl w-full max-w-2xl overflow-hidden">
         <div class="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-slate-50">
            <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full bg-sky-100 flex items-center justify-center text-sky-600 font-bold text-lg">
                    {{ (selectedStudent?.firstName?.charAt(0) || '') + (selectedStudent?.lastName?.charAt(0) || '') }}
                </div>
                <div>
                   <h3 class="text-lg font-bold text-slate-800">{{ selectedStudent?.firstName }} {{ selectedStudent?.lastName }}</h3>
                   <p class="text-xs text-slate-500">{{ selectedStudent?.invitationEmail }} · {{ selectedStudent?.educationType?.code || 'Pte. Onboarding' }}</p>
                </div>
            </div>
            <button @click="showFunnelModal = false" class="text-slate-400 hover:text-slate-600 bg-white shadow-sm border border-slate-200 rounded-full p-1 transition-colors">
                 <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
            </button>
         </div>
         <div class="p-8">
             <h4 class="text-sm font-bold text-slate-700 uppercase mb-6 tracking-wide">Funnel del Estudiante</h4>
             
             <!-- Si no tiene aplicaciones -->
             <div v-if="!selectedStudent?.latestApplication" class="text-center py-6 bg-slate-50 rounded-xl border border-dashed border-slate-200">
                  <p class="text-sm text-slate-500">Este alumno aún no ha postulado a ninguna oferta.</p>
                  <p class="text-[10px] text-slate-400 mt-1 uppercase font-bold">Estado: {{ selectedStudent?.profileComplete ? 'Buscando' : 'Completando Perfil' }}</p>
             </div>

             <div v-else class="relative">
                 <!-- Tracking Line -->
                 <div class="absolute left-[19px] top-6 bottom-4 w-0.5 bg-slate-100"></div>
                 
                 <div class="space-y-6 relative">
                     <!-- Step 1: Registro -->
                     <div class="flex items-start gap-4">
                         <div class="flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center relative z-10 bg-emerald-500 text-white shadow-sm">
                             <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                         </div>
                         <div>
                             <h4 class="text-md font-bold text-slate-800">Registro & Onboarding</h4>
                             <p class="text-sm text-slate-500">Perfil completado y validado.</p>
                         </div>
                     </div>

                     <!-- Step 2: Interés / Entrevista -->
                     <div class="flex items-start gap-4">
                         <div class="flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center relative z-10" 
                              :class="isAtLeast(selectedStudent.latestApplication.status, 'INTERESTED') ? 'bg-sky-500 text-white shadow-md' : 'bg-slate-100 text-slate-400'">
                             <svg v-if="isAtLeast(selectedStudent.latestApplication.status, 'INTERESTED')" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                             <span v-else class="text-sm font-bold">2</span>
                         </div>
                         <div>
                             <h4 class="text-md font-bold" :class="isAtLeast(selectedStudent.latestApplication.status, 'INTERESTED') ? 'text-slate-800' : 'text-slate-400'">Candidaturas / Entrevistas</h4>
                             <p class="text-sm" :class="isAtLeast(selectedStudent.latestApplication.status, 'INTERESTED') ? 'text-slate-500' : 'text-slate-400'">
                                {{ getStep2Text(selectedStudent.latestApplication.status) }}
                             </p>
                             <span v-if="selectedStudent.latestApplication.status === 'INTERESTED'" class="mt-2 inline-block px-2 py-1 bg-sky-100 text-sky-700 text-[10px] font-bold rounded-md border border-sky-200">INTERÉS EXPRESADO</span>
                             <span v-if="selectedStudent.latestApplication.status === 'INTERVIEW_SCHEDULED'" class="mt-2 inline-block px-2 py-1 bg-amber-100 text-amber-700 text-[10px] font-bold rounded-md border border-amber-200">ENTREVISTA AGENDADA</span>
                         </div>
                     </div>

                     <!-- Step 3: Oferta -->
                     <div class="flex items-start gap-4">
                         <div class="flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center relative z-10"
                              :class="isAtLeast(selectedStudent.latestApplication.status, 'OFFERED') ? 'bg-amber-400 text-white shadow-md ring-4 ring-amber-50' : 'bg-slate-100 text-slate-400'">
                             <svg v-if="isAtLeast(selectedStudent.latestApplication.status, 'OFFERED')" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                             <span v-else class="text-sm font-bold">3</span>
                         </div>
                         <div>
                             <h4 class="text-md font-bold" :class="isAtLeast(selectedStudent.latestApplication.status, 'OFFERED') ? 'text-slate-800' : 'text-slate-400'">Oferta Realizada</h4>
                             <p class="text-sm" :class="isAtLeast(selectedStudent.latestApplication.status, 'OFFERED') ? 'text-slate-500' : 'text-slate-400'">
                                {{ getStep3Text(selectedStudent.latestApplication.status) }}
                             </p>
                             <span v-if="selectedStudent.latestApplication.status === 'OFFERED'" class="mt-2 inline-block px-2 py-1 bg-amber-100 text-amber-700 text-[10px] font-bold rounded-md border border-amber-200 uppercase">Esperando aceptación del alumno</span>
                         </div>
                     </div>

                     <!-- Step 4: Validación & Confirmación -->
                     <div class="flex items-start gap-4">
                         <div class="flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center relative z-10"
                              :class="isAtLeast(selectedStudent.latestApplication.status, 'CONFIRMED') ? 'bg-emerald-500 text-white shadow-lg' : 'bg-slate-100 text-slate-400'">
                             <svg v-if="isAtLeast(selectedStudent.latestApplication.status, 'CONFIRMED')" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                             <span v-else class="text-sm font-bold">4</span>
                         </div>
                         <div>
                             <h4 class="text-md font-bold" :class="isAtLeast(selectedStudent.latestApplication.status, 'CONFIRMED') ? 'text-slate-800' : 'text-slate-400'">Acuerdo Confirmado</h4>
                             <p class="text-sm" :class="isAtLeast(selectedStudent.latestApplication.status, 'CONFIRMED') ? 'text-slate-500' : 'text-slate-400'">
                                {{ getStep4Text(selectedStudent.latestApplication.status) }}
                             </p>
                             <span v-if="selectedStudent.latestApplication.status === 'CONFIRMED'" class="mt-2 inline-block px-2 py-1 bg-emerald-100 text-emerald-800 text-[10px] font-bold rounded-md border border-emerald-200 uppercase tracking-tighter">PLACEMENT COMPLETADO</span>
                         </div>
                     </div>
                 </div>
             </div>
         </div>
       </div>
    </div>

    <!-- Modal Validación Final (Aprobar/Rechazar) -->
    <div v-if="showValidationModal" class="fixed inset-0 z-50 bg-slate-900/50 flex items-center justify-center p-4">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-4xl overflow-hidden flex flex-col max-h-[90vh]">
        <div class="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-slate-50 shrink-0">
          <div>
            <h3 class="text-lg font-bold text-slate-800">Validación Final de Prácticas</h3>
            <p class="text-xs text-slate-500">Alumnos esperando firma del Learning Agreement (Etapa 4)</p>
          </div>
          <button @click="showValidationModal = false" class="text-slate-400 hover:text-slate-600 bg-white shadow-sm border border-slate-200 rounded-full p-1 transition-colors">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
          </button>
        </div>
        
        <div class="p-6 overflow-y-auto">
          <div v-if="studentsToValidate.length === 0" class="text-center py-12">
            <div class="h-16 w-16 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="w-8 h-8 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            </div>
            <h4 class="text-slate-600 font-medium">No hay alumnos pendientes de validación en este momento.</h4>
          </div>

          <div v-else class="space-y-4">
            <div v-for="app in studentsToValidate" :key="app.id" class="bg-white border text-sm border-slate-200 rounded-xl p-5 shadow-sm">
              <div class="flex flex-col md:flex-row justify-between md:items-center gap-4">
                <div class="flex items-center gap-4">
                  <div class="w-10 h-10 rounded-full bg-sky-100 flex items-center justify-center text-sky-600 font-bold">
                    {{ (app.student?.firstName?.charAt(0) || '') + (app.student?.lastName?.charAt(0) || '') }}
                  </div>
                  <div>
                    <h4 class="font-bold text-slate-800">{{ app.student?.firstName }} {{ app.student?.lastName }}</h4>
                    <p class="text-xs text-slate-500">{{ app.student?.educationType?.code || 'CFGS' }} · {{ app.student?.invitationEmail }}</p>
                  </div>
                </div>
                
                <div class="bg-amber-50 rounded-lg p-3 border border-amber-100 flex-1 md:mx-4">
                  <p class="text-xs text-amber-800 font-medium flex items-center gap-1 mb-1">
                    <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z" clip-rule="evenodd"></path></svg>
                    Propuesta de Prácticas
                  </p>
                  <div class="text-[11px] text-slate-600 leading-tight">
                    <p><strong>Empresa:</strong> {{ app.opportunity?.company?.name || '---' }}</p>
                    <p><strong>Puesto:</strong> {{ app.opportunity?.title }}</p>
                    <p><strong>Fechas:</strong> {{ formatDate(app.opportunity?.startDate) }} - {{ formatDate(app.opportunity?.endDate) }}</p>
                  </div>
                </div>

                <div class="flex items-center gap-2 justify-end">
                  <button @click="previewAgreement(app)" class="px-3 py-1.5 text-xs font-bold text-slate-500 hover:text-slate-800 transition-colors">
                    Ver Detalles
                  </button>
                  <button @click="confirmAction(app, 'REJECT')" class="px-3 py-1.5 text-xs font-medium text-rose-600 hover:bg-rose-50 border border-slate-200 rounded-lg transition-colors">
                    Rechazar
                  </button>
                  <button @click="confirmAction(app, 'APPROVE')" class="bg-emerald-500 hover:bg-emerald-600 text-white px-4 py-1.5 text-xs font-medium rounded-lg transition-colors shadow-sm flex items-center gap-1">
                    <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                    Aprobar Acuerdo
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Confirmación de Acción (Validación) -->
    <div v-if="actionConfirm.show" class="fixed inset-0 z-[60] bg-slate-900/40 flex items-center justify-center p-4">
      <div class="bg-white rounded-xl shadow-2xl max-w-sm w-full p-6 text-center">
        <div class="mx-auto w-12 h-12 rounded-full flex items-center justify-center mb-4" :class="actionConfirm.type === 'APPROVE' ? 'bg-emerald-100 text-emerald-600' : 'bg-rose-100 text-rose-600'">
            <svg v-if="actionConfirm.type === 'APPROVE'" class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            <svg v-else class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
        </div>
        <h3 class="text-lg font-bold text-slate-800 mb-2">
          ¿{{ actionConfirm.type === 'APPROVE' ? 'Aprobar' : 'Rechazar' }} acuerdo?
        </h3>
        <p class="text-sm text-slate-500 mb-6">
          Estás a punto de {{ actionConfirm.type === 'APPROVE' ? 'aprobar las prácticas de' : 'rechazar la propuesta de' }} <strong>{{ actionConfirm.student?.firstName }}</strong>.
          <span v-if="actionConfirm.type === 'APPROVE'">Esto generará el Learning Agreement final.</span>
          <span v-else>El alumno deberá buscar otra vacante.</span>
        </p>

        <textarea v-if="actionConfirm.type === 'REJECT'" v-model="actionConfirm.reason" placeholder="Motivo del rechazo (obligatorio)..." class="w-full text-sm border-slate-200 rounded-lg p-3 mb-4 focus:ring-rose-500 focus:border-rose-500"></textarea>

        <div class="flex gap-3 justify-center">
          <button @click="actionConfirm.show = false" class="px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50 rounded-lg border border-slate-200">Cancelar</button>
          <button @click="executeAction" :disabled="isProcessingAction || (actionConfirm.type === 'REJECT' && !actionConfirm.reason)" :class="actionConfirm.type === 'APPROVE' ? 'bg-emerald-500 hover:bg-emerald-600 text-white' : 'bg-rose-500 hover:bg-rose-600 text-white'" class="px-4 py-2 text-sm font-medium rounded-lg disabled:opacity-50">
            {{ isProcessingAction ? 'Procesando...' : 'Confirmar' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Modal Previsualización de Acuerdo -->
    <div v-if="showPreview" class="fixed inset-0 z-[70] bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
        <div class="bg-white rounded-3xl shadow-2xl w-full max-w-2xl overflow-hidden border border-slate-200 animate-in fade-in zoom-in duration-300">
            <div class="p-8 border-b border-slate-50 flex justify-between items-center bg-slate-50/50">
                <div>
                    <h3 class="text-xl font-black text-slate-800 tracking-tight uppercase">Learning Agreement</h3>
                    <p class="text-[10px] text-slate-400 font-bold uppercase tracking-widest mt-1">Beducation International Placement</p>
                </div>
                <button @click="showPreview = false" class="p-2 hover:bg-white rounded-full transition-colors border border-transparent hover:border-slate-100">
                    <svg class="w-6 h-6 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                </button>
            </div>
            
            <div class="p-8 space-y-8">
                <!-- Secciones del contrato simuladas -->
                <div class="grid grid-cols-2 gap-8 text-left">
                    <div>
                        <label class="text-[9px] font-black text-slate-400 uppercase tracking-widest block mb-2">Alumno Participante</label>
                        <p class="text-sm font-bold text-slate-800">{{ selectedAppForPreview?.student?.firstName }} {{ selectedAppForPreview?.student?.lastName }}</p>
                        <p class="text-xs text-slate-500">{{ selectedAppForPreview?.student?.invitationEmail }}</p>
                    </div>
                    <div>
                        <label class="text-[9px] font-black text-slate-400 uppercase tracking-widest block mb-2">Empresa de Acogida</label>
                        <p class="text-sm font-bold text-slate-800">{{ selectedAppForPreview?.opportunity?.company?.name }}</p>
                        <p class="text-xs text-slate-500">{{ selectedAppForPreview?.opportunity?.city }}, {{ selectedAppForPreview?.opportunity?.country }}</p>
                    </div>
                </div>

                <div class="bg-slate-50 rounded-2xl p-6 border border-slate-100 text-left">
                    <label class="text-[9px] font-black text-slate-400 uppercase tracking-widest block mb-3">Detalles de las Prácticas</label>
                    <div class="space-y-3">
                        <div class="flex justify-between border-b border-slate-200 pb-2">
                            <span class="text-xs text-slate-500">Puesto:</span>
                            <span class="text-xs font-bold text-slate-800">{{ selectedAppForPreview?.opportunity?.title }}</span>
                        </div>
                        <div class="flex justify-between border-b border-slate-200 pb-2">
                            <span class="text-xs text-slate-500">Periodo:</span>
                            <span class="text-xs font-bold text-slate-800">{{ formatDate(selectedAppForPreview?.opportunity?.startDate) }} - {{ formatDate(selectedAppForPreview?.opportunity?.endDate) }}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="text-xs text-slate-500">Estado Validación:</span>
                            <span class="text-[9px] font-black px-2 py-0.5 bg-amber-100 text-amber-700 rounded uppercase">Pendiente Firma Centro</span>
                        </div>
                    </div>
                </div>

                <div class="pt-4 flex gap-3">
                    <button @click="showPreview = false" class="btn-secondary flex-1">Cerrar</button>
                    <button @click="confirmAction(selectedAppForPreview, 'APPROVE'); showPreview = false" class="btn-primary flex-1">Aprobar Ahora</button>
                </div>
            </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useAuthStore } from '../../store/auth';
import api from '../../services/api';

const authStore = useAuthStore();
const students = ref([]);
const showInviteModal = ref(false);
const isInviting = ref(false);

const newStudent = ref({
    firstName: '',
    lastName: '',
    email: '',
    educationType: ''
});

// CSV Upload
const csvInput = ref(null);
const isUploadingCsv = ref(false);

// Modal Funnel
const showFunnelModal = ref(false);
const selectedStudent = ref(null);

// Modales Validation & Preview
const showValidationModal = ref(false);
const showPreview = ref(false);
const selectedAppForPreview = ref(null);
const actionConfirm = ref({ show: false, student: null, type: '', reason: '' });
const isProcessingAction = ref(false);

const schoolId = computed(() => authStore.user?.id);

const pendingValidationsCount = computed(() => {
    return studentsToValidate.value.length;
});

const studentsToValidate = ref([]);

const stats = ref({
    funnel: {},
    totalApplications: 0,
    totalStudents: 0,
    registeredStudents: 0
});

const fetchStats = async () => {
    if (!schoolId.value) return;
    try {
        const res = await api.get(`/schools/${schoolId.value}/stats`);
        stats.value = res;
    } catch (e) {
        console.error("Error fetching school stats:", e);
    }
}

const fetchStudents = async () => {
    if (!schoolId.value) {
        return;
    }
    try {
        const res = await api.get(`/schools/${schoolId.value}/students`);
        const studentList = (res && res.content) ? res.content : (Array.isArray(res) ? res : []);
        
        for (let std of studentList) {
            try {
                const appRes = await api.get(`/applications/student/${std.id}`);
                const apps = appRes.content || [];
                if (apps.length > 0) {
                    std.latestApplication = apps.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))[0];
                }
            } catch (err) {
                console.warn(`Could not fetch applications for student ${std.id}:`, err);
            }
        }
        students.value = studentList;
    } catch (e) {
        console.error("Error fetching students:", e);
    }
};

const fetchPendingApplications = async () => {
    if (!schoolId.value) {
        console.warn("SchoolDashboard: Aborting fetchPendingApplications due to missing schoolId");
        return;
    }
    try {
        const res = await api.get(`/schools/${schoolId.value}/pending-applications`);
        studentsToValidate.value = (res && res.content) ? res.content : (Array.isArray(res) ? res : []);
    } catch (e) {
        console.error("Error fetching pending applications:", e);
    }
};

onMounted(async () => {
    if (authStore.user) {
        await Promise.all([fetchStudents(), fetchPendingApplications(), fetchStats()]);
    }
});

const openInviteModal = () => {
    showInviteModal.value = true;
};

const closeInviteModal = () => {
    showInviteModal.value = false;
    newStudent.value = { firstName: '', lastName: '', email: '', educationType: '' };
};

const submitInvite = async () => {
    console.log("Submit clicked. schoolId is:", schoolId.value, "authStore user is:", authStore.user);
    if (!schoolId.value) {
        alert(`Error interno del frontal: El ID de la escuela no está definido. store.user = ${JSON.stringify(authStore.user)}`);
        return;
    }
    isInviting.value = true;
    try {
        await api.post(`/schools/${schoolId.value}/invite-student`, null, {
            params: {
                firstName: newStudent.value.firstName,
                lastName: newStudent.value.lastName,
                email: newStudent.value.email
            }
        });
        
        await fetchStudents();
        closeInviteModal();
    } catch (e) {
        alert(`Error al invitar alumno: ${e.response?.data?.message || e.message}`);
    } finally {
        isInviting.value = false;
    }
};

const handleCsvUpload = async (event) => {
    const file = event.target.files[0];
    if (!file || !schoolId.value) return;

    const formData = new FormData();
    formData.append('file', file);

    isUploadingCsv.value = true;
    try {
        await api.post(`/schools/${schoolId.value}/import-students`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
        alert('Importación completada con éxito.');
        await fetchStudents();
        await fetchStats();
    } catch (e) {
        alert("Error en la importación: " + (e.response?.data?.message || e.message));
    } finally {
        isUploadingCsv.value = false;
        event.target.value = ''; // Reset input
    }
};

const deleteStudent = async (studentId) => {
    if (!confirm('¿Estás seguro de que deseas eliminar a este alumno del directorio? Esta acción no se puede deshacer.')) return;
    if (!schoolId.value) return;
    try {
        await api.delete(`/schools/${schoolId.value}/students/${studentId}`);
        students.value = students.value.filter(s => s.id !== studentId);
    } catch (e) {
        alert(`Error al eliminar alumno: ${e.response?.data?.message || e.message}`);
    }
};

const openFunnelModal = (student) => {
    selectedStudent.value = student;
    showFunnelModal.value = true;
};


const openValidationModal = () => {
    showValidationModal.value = true;
};

const previewAgreement = (app) => {
    selectedAppForPreview.value = app;
    showPreview.value = true;
};

const confirmAction = (app, type) => {
    actionConfirm.value = {
        show: true,
        app,
        type,
        reason: ''
    };
};

const executeAction = async () => {
    if (!schoolId.value) return;
    isProcessingAction.value = true;
    try {
        const type = actionConfirm.value.type.toLowerCase(); // 'approve' or 'reject'
        const endpoint = `/applications/${actionConfirm.value.app.id}/school-${type}`;
        
        await api.patch(endpoint, null, {
            params: {
                schoolId: schoolId.value,
                reason: actionConfirm.value.reason || ''
            }
        });
        
        await fetchPendingApplications();
        actionConfirm.value.show = false;
    } catch (e) {
        alert("Error al procesar la acción: " + (e.response?.data?.message || e.message));
    } finally {
        isProcessingAction.value = false;
    }
};

const isAtLeast = (status, target) => {
    const order = ['APPLIED', 'INTERESTED', 'INTERVIEW_SCHEDULED', 'OFFERED', 'ADMIN_APPROVED', 'CONFIRMED'];
    return order.indexOf(status) >= order.indexOf(target);
};

const getStep2Text = (status) => {
    if (status === 'APPLIED') return 'Esperando revisión de la empresa.';
    if (status === 'INTERESTED') return 'La empresa ha mostrado interés.';
    if (status === 'INTERVIEW_SCHEDULED') return 'Entrevista concertarda por videollamada.';
    if (isAtLeast(status, 'OFFERED')) return 'Fase de evaluación completada.';
    return 'Pendiente de inicio.';
};

const getStep3Text = (status) => {
    if (isAtLeast(status, 'OFFERED')) return 'La empresa ha enviado una propuesta formal.';
    return 'A la espera de propuesta tras entrevistas.';
};

const getStep4Text = (status) => {
    if (status === 'CONFIRMED') return 'Convenio firmado por todas las partes.';
    if (status === 'ADMIN_APPROVED') return 'Validado por Admin, esperando firma Escuela.';
    return 'El acuerdo se generará tras la aceptación.';
};

const formatDate = (date) => {
    if (!date) return '---';
    return new Date(date).toLocaleDateString('es-ES', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
};

const downloadAgreement = (app) => {
    alert(`Generando documento para: ${app.student.firstName} ${app.student.lastName}\nEmpresa: ${app.opportunity.company.name}\n\nEn un entorno real, esto descargaría el PDF del Learning Agreement firmado.`);
};

// Re-intentar carga si el user/id aparece después (útil en mocks o refrescos)
watch(schoolId, (newId) => {
    if (newId) {
        fetchStudents();
        fetchPendingApplications();
    }
});
</script>
