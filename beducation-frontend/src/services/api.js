import axios from 'axios';

/**
 * CONFIGURACIÓN DE API (Axios)
 * ----------------------------
 * Cliente centralizado para todas las peticiones al backend de BeDucation.
 * Maneja automáticamente la inyección de tokens de seguridad y el 
 * formateo de errores para la UI.
 */
const api = axios.create({
    // La URL base se toma de variables de entorno (.env) o cae a localhost por defecto
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json'
    }
});

/**
 * INTERCEPTOR DE PETICIONES
 * -------------------------
 * Antes de enviar cada petición, verificamos si tenemos un token en sesión
 * para adjuntarlo como cabecera 'Authorization: Bearer <JWT>'.
 */
api.interceptors.request.use(async (config) => {
    const url = config.url;
    const method = config.method;

    // Listado de rutas que no requieren token (endpoints públicos)
    const isPublicRoute = (url.match(/^\/schools\/?$/) && method === 'post') ||
        (url.match(/^\/companies\/?$/) && method === 'post') ||
        url.includes('/debug/') ||
        url.includes('/auth/');

    if (!isPublicRoute) {
        const token = sessionStorage.getItem('access_token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});

/**
 * INTERCEPTOR DE RESPUESTAS
 * -------------------------
 * Simplifica la respuesta devolviendo solo el cuerpo 'data'.
 * Centraliza el manejo de errores HTTP (401, 500, etc.)
 */
api.interceptors.response.use(
    (response) => {
        return response.data; 
    },
    (error) => {
        // Normalización del mensaje de error del backend
        const errMessage = error.response?.data?.message || 
                          error.response?.data?.error || 
                          error.message || 'Error desconocido';

        // Gestión de caducidad de sesión
        if (error.response?.status === 401 || error.response?.status === 403) {
            console.warn('Acceso denegado o sesión expirada. El usuario debería ser redirigido.');
        }

        return Promise.reject({
            status: error.response?.status || 500,
            message: errMessage,
            data: error.response?.data
        });
    }
);

export default api;

