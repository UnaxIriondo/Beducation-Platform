/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Paleta base de BeDucation (Aesthetic Erasmus modern, blue-ish / glassmorphism)
        primary: {
          50: '#eff6ff',
          100: '#dbeafe',
          500: '#3b82f6',
          600: '#2563eb', // Brand color
          700: '#1d4ed8',
          900: '#1e3a8a',
        },
        secondary: {
          500: '#10b981', // Emerald confirmation
        },
        dark: '#0f172a',
      },
      fontFamily: {
        outfit: ['Outfit', 'sans-serif'], // Tipografía moderna, redonda y muy legible
      }
    },
  },
  plugins: [],
}
