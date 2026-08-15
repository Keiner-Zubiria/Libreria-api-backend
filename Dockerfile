# Etapa 1: compilar el proyecto con Maven y Java 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline || true
COPY src ./src
COPY uploads ./uploads
RUN mvn -B clean package -DskipTests

# Etapa 2: imagen de ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/api-libreria-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/uploads ./uploads
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
