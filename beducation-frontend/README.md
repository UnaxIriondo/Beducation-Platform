# 🎨 BeDucation Frontend

The frontend for BeDucation Platform is a modern SPA (Single Page Application) built with Vue 3, Vite, and Tailwind CSS. It provides an intuitive interface for students, schools, and administrators.

## 🚀 Key Features

- **Vue 3 Composition API**: Clean and maintainable component logic.
- **Vite**: Ultra-fast build and development experience.
- **Tailwind CSS**: Utility-first styling with custom themes.
- **Pinia**: Global state management for authentication and user sessions.
- **Auth0**: Integrated login/logout and token management.
- **Responsive Design**: Mobile-friendly dashboards and forms.

## 🛠️ Requirements

- **Node.js**: v18 or later
- **NPM / PNPM / Yarn**: Any package manager

## ⚙️ Setup Instructions

1. **Install Dependencies**:
   ```bash
   npm install
   ```

2. **Run Development Server**:
   ```bash
   npm run dev
   ```

3. **Build for Production**:
   ```bash
   npm run build
   ```

## 📂 Architecture

- **`src/components/`**: Reusable UI components (buttons, modals, cards).
- **`src/views/`**: Main page components (Dashboard, Profile, Login).
- **`src/stores/`**: Pinia stores for global state.
- **`src/router/`**: Vue Router configuration and navigation guards.
- **`src/services/`**: API client using Axios for backend communication.

---

## 🎨 Styling

The application uses **Tailwind CSS**. Custom configurations can be found in `tailwind.config.js`.

To extend the theme or add custom colors:
1. Modify `tailwind.config.js`.
2. Add global styles in `src/assets/main.css` (or equivalent).
