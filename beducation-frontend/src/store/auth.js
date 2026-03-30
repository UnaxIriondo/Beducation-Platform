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
        user: null,
        auth0User: null,
        token: sessionStorage.getItem('access_token') || null,
        isAuthenticated: !!sessionStorage.getItem('access_token'),
        role: sessionStorage.getItem('user_role') || null,
        loading: false
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
            if (!this.isAuthenticated) return;
            
            console.log('Fetching profile for role:', this.role);
            if (!this.role || this.role === 'UNKNOWN') {
                console.warn('Role is missing, cannot fetch profile yet.');
                return;
            }

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
                if (endpoint) {
                    const response = await api.get(endpoint);
                    // El interceptor de axios ya devuelve response.data directamente
                    this.user = response;
                }
            } catch (e) {
                console.warn('Usuario registrado en Auth0 pero no se halló perfil completado en Backend (registro por terminar o invitado recién entrado).');
            }
        },

        async loginByEmail(email) {
            this.loading = true;
            try {
                // El endpoint /api/debug/login devuelve {token, role, email}
                const response = await api.get(`/debug/login`, { params: { email } });
                
                this.token = response.token;
                this.role = response.role;
                sessionStorage.setItem('access_token', this.token);
                sessionStorage.setItem('user_role', this.role);
                console.log('Login success. Token and Role saved:', this.role);
                this.isAuthenticated = true;
                
                this.auth0User = {
                    sub: response.token.split('~')[1],
                    email: response.email,
                    name: response.email.split('@')[0]
                };

                await this.fetchLocalUserProfile();
                return true;
            } catch (err) {
                console.error('Debug login failed:', err);
                throw err;
            } finally {
                this.loading = false;
            }
        },

        // ──────────────────────────────────────────────
        // MOCK LOGIN PARA PRUEBAS (Evita el paso real de Auth0)
        // ──────────────────────────────────────────────
        async mockLogin(role) {
            this.loading = true;
            try {
                if (role === 'STUDENT') {
                    // Ahora usamos el login de desarrollo real para que la data en backend coincida con la de tu BD local
                    await this.loginByEmail('unax.iriondo.51345@ikasle.egibide.org');
                } else if (role === 'SCHOOL') {
                    await this.loginByEmail('director@ikasle.egibide.org');
                } else {
                    // Fallback para otros si no están migrados
                    this.isAuthenticated = true;
                    this.role = role;
                    this.token = "mock-jwt-token";
                    sessionStorage.setItem('access_token', this.token);
                    this.auth0User = {
                        sub: `mock-auth0-id-${role.toLowerCase()}`,
                        name: `Usuario MOCK (${role})`,
                        email: `test@${role.toLowerCase()}.com`
                    };
                    this.user = { id: 1, name: this.auth0User.name, email: this.auth0User.email };
                }
            } catch (err) {
                console.error("Error en mockLogin:", err);
            } finally {
                this.loading = false;
            }
        },

        // ──────────────────────────────────────────────

        clearSession() {
            this.user = null;
            this.auth0User = null;
            this.token = null;
            this.isAuthenticated = false;
            this.role = null;
            sessionStorage.removeItem('access_token');
            sessionStorage.removeItem('user_role');
            console.log('Session cleared.');
        }
    }
});
