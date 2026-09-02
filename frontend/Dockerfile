# ============================================
# TASKMASTER FRONTEND - MULTI-STAGE BUILD
# ============================================
# Stage 1: Build Angular app
# Stage 2: Serve with Nginx

# ============================================
# STAGE 1: BUILD
# ============================================
FROM node:18-alpine AS builder

# Set working directory
WORKDIR /app

# Copy package files first (for layer caching)
COPY package*.json ./

# Install dependencies
RUN npm ci --only=production

# Copy source code
COPY . .

# Build for production
RUN npm run build -- --configuration production

# ============================================
# STAGE 2: RUNTIME (Nginx)
# ============================================
FROM nginx:1.25-alpine

# Metadata
LABEL maintainer="devops@taskmaster.com"
LABEL version="1.0.0"
LABEL description="TaskMaster Frontend - Angular + Nginx"

# Copy custom Nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built Angular app from builder
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost/ || exit 1

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
