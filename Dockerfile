# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copier le pom.xml et télécharger les dépendances (cache Docker)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN ./mvnw dependency:go-offline -B
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copier le code source et builder
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ---- Stage 2: Run ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copier uniquement le jar final depuis le stage build
COPY --from=build /app/target/*.jar app.jar

# Port exposé
EXPOSE 8081

# Variables d'environnement par défaut (surchargées au déploiement)
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "app.jar"]