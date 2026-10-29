# ── Stage 1: Build ───────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Run ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache netcat-openbsd
COPY --from=builder /build/target/billing-*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["sh", "-c", \
  "until nc -z ${DB_HOST:-mysql} 3306; do echo 'Waiting for MySQL...'; sleep 2; done; \
   java -Xms256m -Xmx512m -jar app.jar"]
