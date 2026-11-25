# Une seule image : l'interface Angular compilée est servie par Spring Boot (même origine, donc cookie et WebSocket simples).

# 1. Interface Angular
FROM node:24-alpine AS interface
WORKDIR /interface
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY frontend/ ./
RUN npx ng build --configuration production

# 2. Serveur Spring Boot (l'interface rejoint ses fichiers statiques)
FROM eclipse-temurin:25-jdk AS serveur
WORKDIR /serveur
COPY backend/.mvn .mvn
COPY backend/mvnw backend/pom.xml ./
RUN chmod +x mvnw
COPY backend/src src
COPY --from=interface /interface/dist/frontend/browser src/main/resources/static
RUN ./mvnw -B -ntp -q -DskipTests package

# 3. Exécution : JRE seul, utilisateur sans privilèges
FROM eclipse-temurin:25-jre
RUN useradd --system --no-create-home lovematch
WORKDIR /app
COPY --from=serveur /serveur/target/lovematch-1.0.0.jar lovematch.jar
USER lovematch
# offre gratuite de Render : 512 Mo de mémoire
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"
EXPOSE 8090
CMD ["java", "-jar", "lovematch.jar"]
