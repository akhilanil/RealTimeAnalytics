import path from "path"
import tailwindcss from "@tailwindcss/vite"
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'




// https://vite.dev/config/
export default defineConfig({

  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    proxy: {
      // Anything starting with /api will be proxied to the backend
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // If your backend is on HTTPS with self-signed certs, uncomment:
        // secure: false,
      },
    },
  },
})
