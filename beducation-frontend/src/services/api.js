import axios from 'axios';
import { useAuth0 } from '@auth0/auth0-vue';

// ============================================================
// Axios Instancia Base "api"
// ============================================================
// Realizará las peticiones al backend en Java Spring Boot (puerto 8080)
const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json'
    }
});

// Interceptor para inyectar token Auth0 automáticamente en cada request protegida
api.interceptors.request.use(async (config) => {
    // Rutas exentas de JWT (solo el registro inicial de escuela/empresa - endpoints públicos exactos)
    const url = config.url;
    const method = config.method;
    const isPublicRoute = (url.match(/^\/schools\/?$/) && method === 'post') ||
        (url.match(/^\/companies\/?$/) && method === 'post') ||
        url.includes('/debug/') ||
        url.includes('/auth/');

    if (!isPublicRoute) {
        try {
            // Necesitamos una función getAuth0() o directamente importar si está instanciado globalmente
            // En Vue3 Composition API solemos pasarlo o sacar el accessTokenSilently a nivel capa pinia/UI
            // Temporalmente definiremos esta abstracción asíncrona pero la inyección real 
            // suele resolverse con un middleware pinia o en el componente.

            // authStore manejará el token local cacheado con Pinia para más eficiencia.
            const token = sessionStorage.getItem('access_token');
            if (token) {
                console.log(`API DEBUG: Enmascarando token en cabecera: Bearer ${token.substring(0, 10)}...`);
                config.headers.Authorization = `Bearer ${token}`;
            } else {
                console.warn('API DEBUG: No se encontró token en sessionStorage');
            }
        } catch (error) {
            console.warn('Axios intercepción: Error obteniendo token', error);
        }
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});

// Interceptor global de errores
api.interceptors.response.use(
    (response) => {
        return response.data; // Retorna directamente la data, ignorando meta cabeceras axios (2xx)
    },
    (error) => {
        // Aquí implementamos el "Manejo estandarizado de respuestas de error" que pedía el contrato (Semana 15-16)
        const errMessage = error.response?.data?.message || error.response?.data?.error || error.message || 'Error desconocido';

        // Si obtenemos 401 Unauthorized -> borrar token y obligar a autenticador
        if (error.response?.status === 401 || error.response?.status === 403) {
            console.error('Session expired or unauthorized. Redirecting to login.');
            // Lógica de descarte gestionada desde Pinia o el App principal
        }

        console.error('Error desde API:', errMessage);
        return Promise.reject({
            status: error.response?.status || 500,
            message: errMessage,
            data: error.response?.data
        });
    }
);

export default api;
