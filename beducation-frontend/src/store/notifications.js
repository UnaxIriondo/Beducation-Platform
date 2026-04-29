import { defineStore } from 'pinia';

/**
 * STORE: Notifications
 * --------------------
 * Sistema centralizado de mensajes (Toasts) para la aplicación.
 * Permite mostrar alertas de éxito, error o información sin usar alert() del navegador.
 */
export const useNotificationStore = defineStore('notifications', {
  state: () => ({
    notifications: []
  }),
  actions: {
    /**
     * Añade una notificación a la cola.
     * @param {string} message - El texto a mostrar.
     * @param {string} type - 'success', 'error', 'info', 'warning'.
     * @param {number} duration - Tiempo en ms antes de desaparecer.
     */
    add(message, type = 'info', duration = 5000) {
      const id = Date.now() + Math.random();
      this.notifications.push({ id, message, type });

      if (duration > 0) {
        setTimeout(() => {
          this.remove(id);
        }, duration);
      }
    },

    remove(id) {
      this.notifications = this.notifications.filter(n => n.id !== id);
    },

    success(message) { this.add(message, 'success'); },
    error(message) { this.add(message, 'error'); },
    info(message) { this.add(message, 'info'); },
    warning(message) { this.add(message, 'warning'); }
  }
});
