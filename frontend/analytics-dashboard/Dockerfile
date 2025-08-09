# syntax=docker/dockerfile:1

# ---------- deps (cache pnpm store) ----------
    FROM node:20-alpine AS deps
    WORKDIR /app
    RUN corepack enable
    COPY package.json pnpm-lock.yaml* ./
    # Pre-fetch deps to leverage cache nicely
    RUN pnpm fetch --prod=false
    
    # ---------- build ----------
    FROM node:20-alpine AS build
    WORKDIR /app
    RUN corepack enable
    # Reuse the fetched store
    COPY --from=deps /root/.local/share/pnpm/store /root/.local/share/pnpm/store
    COPY package.json pnpm-lock.yaml* ./
    COPY . .
    RUN pnpm install --frozen-lockfile
    RUN pnpm run build
    
    # ---------- runtime (nginx) ----------
    FROM nginx:1.27-alpine AS runner
    # Nginx will listen on 5173
    COPY nginx.conf /etc/nginx/conf.d/default.conf
    # Static files
    COPY --from=build /app/dist /usr/share/nginx/html
    EXPOSE 5173
    CMD ["nginx", "-g", "daemon off;"]
    