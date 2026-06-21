import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const BACKEND_URL = process.env.NODE_ENV === 'production'
  ? 'https://nearpharmaviaspringboot.onrender.com'
  : 'http://localhost:8081';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: BACKEND_URL,
        changeOrigin: true,
        rewrite: (path) => path,
      },
    },
  },
  define: {
    __BACKEND_URL__: JSON.stringify(BACKEND_URL),
  },
});
