import { defineStore } from 'pinia';
import { useAuth0 } from '@auth0/auth0-vue';
import api from '../services/api';

// ============================================================
// Autenticador Pinia Store
// ============================================================
// Mantiene centralizado el Usuario, Rol, Estado logeado y sincronización con
// tanto Auth0 Web (Token) como con el Backend (Información Completa)
export const useAuthStore = defineStore('auth', {
    state: () => ({
        user: null, // User entity loaded from Backend (Profile, Company Data)
        auth0User: null, // JWT Payload base (email, sub)
        token: null,
        isAuthenticated: false,
        role: null, // SCHOOL | STUDENT | COMPANY | ADMIN
        loading: true
    }),

    actions: {
        /** 
         * Sincroniza Auth0 con el servicio Axios 
         * Este init lo llamaremos desde App.vue o Router guards 
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

                    // Descubrir Role (extraido de claims de la variable del tenant real Auth0 en producción)
                    this.role = this.auth0User['https://beducation.com/role'] || 'UNKNOWN';

                    // Solicitar al backend el perfil local de MySQL correspondiente al SUB del JWT.
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

        async fetchLocalUserProfile() {
            if (!this.isAuthenticated || this.role === 'UNKNOWN') return;

            try {
                let endpoint = "";

                // Dependiendo del rol, trae la data completa de los repositorios a UI
                if (this.role === 'STUDENT') endpoint = `/students/me`; // Simulación de alias /me en controller
                else if (this.role === 'SCHOOL') endpoint = `/schools/me`;
                else if (this.role === 'COMPANY') endpoint = `/companies/me`;
                else if (this.role === 'ADMIN') endpoint = `/admin/me`;

                // Si tenemos ya la ruta me o equivalente
                // Como no tenemos el alias "/me" en los endpoints, temporalmente 
                // podríamos abstraerlo via auth0Id en un nuevo Controller `UserController` si fuera necesario.
                // Simularemos una query o usar data almacenada del login success

            } catch (e) {
                console.warn('Usuario registrado en Auth0 pero no se halló perfil completado en Backend (registro por terminar o invitado recién entrado).');
            }
        },

        // ──────────────────────────────────────────────
        // MOCK LOGIN PARA PRUEBAS (Evita el paso real de Auth0)
        // ──────────────────────────────────────────────
        mockLogin(role) {
            this.isAuthenticated = true;
            this.role = role;
            this.token = "mock-jwt-token";
            sessionStorage.setItem('access_token', this.token);

            this.user = {
                name: `Usuario MOCK (${role})`,
                email: `test@${role.toLowerCase()}.com`
            };

            // Simular que terminó de cargar
            this.loading = false;
        },

        clearSession() {
            this.user = null;
            this.auth0User = null;
            this.token = null;
            this.isAuthenticated = false;
            this.role = null;
            sessionStorage.removeItem('access_token');
        }
    }
});
