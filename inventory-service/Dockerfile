# Estágio 1: Compilação
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# Força o Maven a ignorar completamente a compilação e execução de testes no build
RUN mvn clean package -Dmaven.test.skip=true

# Estágio 2: Execução
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8083
CMD ["java", "-jar", "app.jar"]