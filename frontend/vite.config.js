import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// In development the API calls go to the reverse proxy started with `docker compose up`.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: { '/api': process.env.VITE_GATEWAY || 'http://localhost' },
  },
});
