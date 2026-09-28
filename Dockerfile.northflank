FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
# Compile all modules in parallel to save time
RUN mvn clean package -Dmaven.test.skip=true -T 1C

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install bash and redis (required for order-service)
RUN apk add --no-cache bash redis

# Copy the built jars
COPY --from=build /workspace/api-gateway/target/api-gateway-1.0.0.jar api-gateway.jar
COPY --from=build /workspace/product-service/target/product-service-1.0.0.jar product-service.jar
COPY --from=build /workspace/auth-service/target/auth-service-1.0.0.jar auth-service.jar
COPY --from=build /workspace/order-service/target/order-service-1.0.0.jar order-service.jar

# Create entrypoint script
RUN echo '#!/bin/bash' > entrypoint.sh && \
    echo 'echo "Starting Redis..."' >> entrypoint.sh && \
    echo 'redis-server --daemonize yes' >> entrypoint.sh && \
    echo 'sleep 2' >> entrypoint.sh && \
    echo 'echo "Starting Microservices..."' >> entrypoint.sh && \
    echo 'export PRODUCT_SERVICE_URL=http://localhost:8081' >> entrypoint.sh && \
    echo 'export AUTH_SERVICE_URL=http://localhost:8082' >> entrypoint.sh && \
    echo 'export ORDER_SERVICE_URL=http://localhost:8083' >> entrypoint.sh && \
    echo 'export SPRING_DATA_REDIS_HOST=localhost' >> entrypoint.sh && \
    echo 'export SPRING_DATA_REDIS_PORT=6379' >> entrypoint.sh && \
    echo 'export PORT=8080' >> entrypoint.sh && \
    echo 'java -Xmx128m -Dserver.port=8081 -jar product-service.jar &' >> entrypoint.sh && \
    echo 'java -Xmx128m -Dserver.port=8082 -jar auth-service.jar &' >> entrypoint.sh && \
    echo 'java -Xmx128m -Dserver.port=8083 -jar order-service.jar &' >> entrypoint.sh && \
    echo 'sleep 15' >> entrypoint.sh && \
    echo 'echo "Starting API Gateway..."' >> entrypoint.sh && \
    echo 'java -Xmx128m -Dserver.port=8080 -jar api-gateway.jar &' >> entrypoint.sh && \
    echo 'echo "All services started!"' >> entrypoint.sh && \
    echo 'wait -n' >> entrypoint.sh && \
    chmod +x entrypoint.sh

# API Gateway port
EXPOSE 8080

ENTRYPOINT ["/app/entrypoint.sh"]
