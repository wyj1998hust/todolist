# Convenience build entrypoint for the Vue frontend. Production manifests use frontend/Dockerfile directly.
FROM node:20-alpine AS build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend ./
RUN npm run build

FROM node:20-alpine
WORKDIR /app
ENV NODE_ENV=production PORT=8080 STATIC_DIR=/app/dist
COPY --from=build /app/dist ./dist
COPY frontend/server.mjs ./server.mjs
EXPOSE 8080
CMD ["node", "server.mjs"]
