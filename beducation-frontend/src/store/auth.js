import { defineStore } from 'pinia';
import api from '../services/api';

/**
 * STORE: Auth (Pinia)
 * -------------------
 * Gestiona el estado global de autenticación, integrando Auth0 con el
 * backend propio de BeDucation.
 */
export const useAuthStore = defineStore('auth', {
    state: () => ({
        user: null,          // Datos completos del perfil local (Student, School, Company, Admin)
        auth0User: null,     // Datos básicos del usuario desde Auth0
        token: sessionStorage.getItem('access_token') || null,
        isAuthenticated: !!sessionStorage.getItem('access_token'),
        role: sessionStorage.getItem('user_role') || null,
        loading: false       // Estado de carga para bloqueos de UI
    }),

    actions: {
        /** 
         * Inicialización del flujo de autenticación.
         * Se encarga de recuperar el token de Auth0 y sincronizar el perfil con el backend.
         * @param {Object} auth0Client - El cliente de Auth0 proporcionado por el SDK de Vue.
         */
        async initialize(auth0Client) {
            this.loading = true;
            try {
                if (auth0Client.isAuthenticated.value) {
                    const accessToken = await auth0Client.getAccessTokenSilently();
                    this.token = accessToken;
                    sessionStorage.setItem('access_token', accessToken);

                    this.auth0User = auth0Client.user.value;
                    this.isAuthenticated = true;

                    // Extracción del Rol desde los Custom Claims de Auth0
                    // NOTA: 'https://beducation.com/role' debe coincidir con la configuración del Rule/Action en el Dashboard de Auth0.
                    this.role = this.auth0User['https://beducation.com/role'] || 'UNKNOWN';
                    sessionStorage.setItem('user_role', this.role);

                    // Recuperamos la información extendida desde nuestra base de datos SQL
                    await this.fetchLocalUserProfile();
                } else {
                    this.clearSession();
                }
            } catch (err) {
                console.error('Error initializing Auth:', err);
                this.clearSession();
            } finally {
                this.loading = false;
            }
        },

        /**
         * Recupera los datos de perfil específicos según el rol del usuario.
         * Se conecta a los endpoints '/me' del backend.
         */
        async fetchLocalUserProfile() {
            if (!this.isAuthenticated) return;
            
            if (!this.role || this.role === 'UNKNOWN') {
                console.warn('Rol no detectado. No se puede cargar el perfil local.');
                return;
            }

            try {
                let endpoint = "";

                // Mapeo de roles a sus respectivos endpoints de perfil
                switch(this.role) {
                    case 'STUDENT': endpoint = `/students/me`; break;
                    case 'SCHOOL':  endpoint = `/schools/me`; break;
                    case 'COMPANY': endpoint = `/companies/me`; break;
                    case 'ADMIN':   endpoint = `/admin/me`; break;
                }

                if (endpoint) {
                    const response = await api.get(endpoint);
                    this.user = response;
                }
            } catch (e) {
                // Si falla, es probable que el usuario esté en Auth0 pero aún no tenga el perfil creado en el Backend
                console.warn(`Aviso: Perfil local para ${this.role} no encontrado en BD.`, e.message);
            }
        },

        /**
         * LOGIN DE DESARROLLO (Debug)
         * --------------------------
         * ¡IMPORTANTE! Este método utiliza un "backdoor" en el backend para loguearse sin Auth0.
         * Solo debe usarse en entornos de desarrollo o pruebas locales.
         */
        async debugLogin(email) {
            this.loading = true;
            try {
                const response = await api.get(`/debug/login`, { params: { email } });
                
                this.token = response.token;
                this.role = response.role;
                
                sessionStorage.setItem('access_token', this.token);
                sessionStorage.setItem('user_role', this.role);
                this.isAuthenticated = true;
                
                // Creamos un objeto de usuario simulado para mantener la compatibilidad con la UI
                this.auth0User = {
                    sub: response.token.split('~')[1] || 'debug-id',
                    email: response.email,
                    name: response.email.split('@')[0]
                };

                await this.fetchLocalUserProfile();
                return true;
            } catch (err) {
                console.error('Debug login fallido:', err);
                throw err;
            } finally {
                this.loading = false;
            }
        },

        /**
         * Cierra la sesión localmente y limpia el almacenamiento del navegador.
         */
        clearSession() {
            this.user = null;
            this.auth0User = null;
            this.token = null;
            this.isAuthenticated = false;
            this.role = null;
            sessionStorage.removeItem('access_token');
            sessionStorage.removeItem('user_role');
        }
    }
});

