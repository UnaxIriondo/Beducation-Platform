import { createApp } from 'vue';
import { createPinia } from 'pinia';
import { createAuth0 } from '@auth0/auth0-vue';
import App from './App.vue';
import router from './router';
import './style.css'; // Tailwind config imports

// ============================================================
// MAIN VUE ENTRY: Inicializador
// ============================================================
const app = createApp(App);

app.use(createPinia());
app.use(router);

// Configuración recomendada para Auth0 Vue SDK
// Extrae de las variables del .env de desarrollo simulando el spec VITE
app.use(
    createAuth0({
        domain: import.meta.env.VITE_AUTH0_DOMAIN || "dev-beducation.eu.auth0.com",
        clientId: import.meta.env.VITE_AUTH0_CLIENT_ID || "12345clientIdMocked",
        authorizationParams: {
            redirect_uri: window.location.origin,
            audience: import.meta.env.VITE_AUTH0_AUDIENCE || "https://api.beducation.com",
        }
    })
);

app.mount('#app');
