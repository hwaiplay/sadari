# syntax=docker/dockerfile:1

FROM node:24-alpine AS frontend-build
WORKDIR /workspace/src/main/frontend
COPY src/main/frontend/package*.json ./
RUN npm ci
COPY src/main/frontend ./
RUN npm run build

FROM eclipse-temurin:17-jdk AS backend-build
WORKDIR /workspace
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY src ./src
COPY --from=frontend-build /workspace/src/main/frontend/dist ./src/main/resources/static
RUN chmod +x ./gradlew
RUN ./gradlew clean bootWar -PskipFrontendBuild=true -PfrontendInstallCommand=ci --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
COPY --from=backend-build /workspace/build/libs/*.war /app/sadari.war
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/sadari.war"]
