# Paso 1: Compilar la aplicación usando Maven Wrapper
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiar los archivos necesarios para compilar
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Descargar dependencias (permite usar la caché de Docker)
RUN ./mvnw dependency:go-offline

# Copiar el código fuente y compilar el archivo .jar omitiendo los tests
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Paso 2: Crear la imagen ligera de ejecución
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el archivo .jar generado en el paso anterior
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto 8080 que usa Tomcat por defecto
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]