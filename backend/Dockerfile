# Build multi-stage do backend Game Tracker.
# Estágio 1: compila com o Maven Wrapper. Os testes rodam localmente/no CI com ./mvnw verify.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

# Estágio 2: imagem enxuta só com o JRE e o jar.
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --create-home gametracker \
    && mkdir -p uploads/catalogo uploads/forum uploads/perfil/avatar uploads/perfil/banner uploads/marketplace \
    && chown -R gametracker /app
COPY --from=build /workspace/target/*.jar app.jar
USER gametracker
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
