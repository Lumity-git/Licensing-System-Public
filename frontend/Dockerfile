# Runtime stage for Next.js frontend
FROM node:18-alpine AS runner

# Install curl for health checks (updated)
RUN apk add --no-cache curl

# Set working directory
WORKDIR /app

# Create non-root user
RUN addgroup --system --gid 1001 nodejs && \
    adduser --system --uid 1001 nextjs

# Copy package.json and server.js from standalone build
COPY .next/standalone/package.json ./
COPY .next/standalone/server.js ./

# Copy the Next.js build files
COPY .next/standalone/.next ./.next
COPY .next/static ./.next/static

# Copy node_modules from standalone build
COPY .next/standalone/node_modules ./node_modules

# Change ownership
RUN chown -R nextjs:nodejs /app

# Switch to non-root user
USER nextjs

# Expose port
EXPOSE 3000

# Set environment variable
ENV PORT 3000
ENV HOSTNAME "0.0.0.0"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:3000/api/health || exit 1

# Run the application
CMD ["node", "server.js"]
