FROM node:22-alpine AS build

WORKDIR /app

RUN npm install -g pnpm@10.20.0

COPY package.json pnpm-lock.yaml* ./
RUN pnpm install

COPY . .

ARG EC2_HOST
RUN sed -i "s|EC2_HOST_PLACEHOLDER|http://$EC2_HOST:8080|g" \
    src/environments/environment.prod.ts

RUN pnpm run build --configuration=production

FROM nginx:alpine

COPY --from=build /app/dist/dropp/browser /usr/share/nginx/html

RUN echo 'server { \
    listen 80; \
    location / { \
        root /usr/share/nginx/html; \
        index index.html index.htm; \
        try_files $uri $uri/ /index.html; \
    } \
}' > /etc/nginx/conf.d/default.conf

CMD ["nginx", "-g", "daemon off;"]