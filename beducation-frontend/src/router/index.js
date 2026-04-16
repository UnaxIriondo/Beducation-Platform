import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../store/auth';

/**
 * ============================================================
 * Vue Router Configuración y Guardas (Navigation Guards)
 * ============================================================
 * Protege las rutas por rol de usuario y gestiona la redirección
 * cuando no se está autenticado (Auth0).
 * ============================================================
 */
const routes = [
    // Rutas Públicas
    {
        path: '/register',
        name: 'RegisterInvitation',
        component: () => import('../views/HomeView.vue'),
        meta: { public: true }
    },
    {
        path: '/',
        name: 'Home',
        component: () => import('../views/HomeView.vue'),
        meta: { public: true }
    },
    {
        path: '/register/school',
        name: 'RegisterSchool',
        component: () => import('../views/school/RegisterSchoolView.vue'),
        meta: { public: true }
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/LoginView.vue'),
        meta: { public: true }
    },
    {
        path: '/register/company',
        name: 'RegisterCompany',
        component: () => import('../views/company/RegisterCompanyView.vue'),
        meta: { public: true }
    },
    // Portal Estudiante (requiere perfil completo o forzará redirección interna)
    {
        path: '/student/dashboard',
        name: 'StudentDashboard',
        component: () => import('../views/student/StudentDashboardView.vue'),
        meta: { requiresAuth: true, role: 'STUDENT' }
    },
    {
        path: '/student/onboarding',
        name: 'StudentOnboarding',
        component: () => import('../views/student/OnboardingView.vue'),
        meta: { requiresAuth: true, role: 'STUDENT' } // Constructor del CV y Profile
    },
    {
        path: '/student/search',
        name: 'StudentSearch',
        component: () => import('../views/student/StudentSearchView.vue'),
        meta: { requiresAuth: true, role: 'STUDENT' }
    },
    {
        path: '/student/offers/:id',
        name: 'OfferDetail',
        component: () => import('../views/student/OfferDetailView.vue'),
        meta: { requiresAuth: true, role: 'STUDENT' }
    },
    // Portal Escuela
    {
        path: '/school/dashboard',
        name: 'SchoolDashboard',
        component: () => import('../views/school/SchoolDashboardView.vue'),
        meta: { requiresAuth: true, role: 'SCHOOL' }
    },
    {
        path: '/school/profile/edit',
        name: 'SchoolProfileEdit',
        component: () => import('../views/school/SchoolProfileEditView.vue'),
        meta: { requiresAuth: true, role: 'SCHOOL' }
    },
    // Portal Empresa
    {
        path: '/company/dashboard',
        name: 'CompanyDashboard',
        component: () => import('../views/company/CompanyDashboardView.vue'),
        meta: { requiresAuth: true, role: 'COMPANY' }
    },
    {
        path: '/company/opportunities/:id/candidates',
        name: 'CompanyOpportunityCandidates',
        component: () => import('../views/company/OpportunityCandidatesView.vue'),
        meta: { requiresAuth: true, role: 'COMPANY' }
    },
    // Portal Admin
    {
        path: '/admin/dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/AdminDashboardView.vue'),
        meta: { requiresAuth: true, role: 'ADMIN' }
    },
    // Galería (Compartida)
    {
        path: '/gallery',
        name: 'Gallery',
        component: () => import('../views/shared/GalleryView.vue'),
        meta: { requiresAuth: true } // No restringido por rol aquí, la lógica de acceso está dentro
    },
    // Fallbacks
    {
        path: '/unauthorized',
        name: 'Unauthorized',
        component: () => import('../views/UnauthorizedView.vue'),
        meta: { public: true }
    },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/'
    }
];

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
});

// Guardia Global de Rutas
router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore();

    // Si la ruta requiere Auth y el usuario no está logeado (Token vacío o no cargado en Auth0 local)
    const isPublic = to.matched.some(record => record.meta.public);

    // Como auth0 es asíncrono, normalmente se delega el guardado a cuando terminó de arrancar
    // pero ya que en main.js montamos Vue al instante, authStore debería actualizar rápido.

    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        // Para simplificar, redirigimos a home o disparamos loginWithRedirect (manejado en layout)
        return next('/');
    }

    // Verificación de Acceso de Roles (Authorization)
    if (to.meta.role) {
        if (authStore.role !== to.meta.role) {
            console.warn(`Role mismatch. Esperaba ${to.meta.role}, pero tiene ${authStore.role}`);
            return next('/unauthorized');
        }
    }

    next();
});

export default router;
