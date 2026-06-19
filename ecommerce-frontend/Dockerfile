# ecommerce-frontend/Dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build:prod

FROM alpine:latest
WORKDIR /app
COPY --from=build /app/dist/marketplace ./dist/marketplace
# mantém o container vivo para o volume ser preenchido
CMD ["sh", "-c", "cp -r /app/dist/marketplace/* /dist"]