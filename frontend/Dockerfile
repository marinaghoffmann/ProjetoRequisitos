# Build multi-stage do frontend Angular.
# Estágio 1: gera o build de produção.
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

# Estágio 2: o Nginx serve os arquivos estáticos e repassa /api e /uploads ao backend.
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
EXPOSE 80
