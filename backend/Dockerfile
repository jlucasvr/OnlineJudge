# =============================================================================
# Multi-stage build — API Spring Boot
# =============================================================================

# Stage 1: build com Maven
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copia dependências primeiro para aproveitar cache do Docker
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q

# Copia código e compila
COPY src/ src/
RUN ./mvnw package -DskipTests -q

# Stage 2: runtime mínimo
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Cria estrutura de diretórios padrão (sobrescrita pelo volume)
RUN apk add --no-cache curl \
    && mkdir -p /data/submissions /data/testcases /data/compile-errors /data/temp

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
