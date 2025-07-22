# Use OpenJDK 17 como base
FROM openjdk:17-jdk-slim

# Definir diretório de trabalho
WORKDIR /app

# Copiar arquivos do Maven
COPY backend/pom.xml .
COPY backend/src ./src

# Instalar Maven
RUN apt-get update && apt-get install -y maven

# Compilar a aplicação
RUN mvn clean package -DskipTests

# Expor a porta
EXPOSE 8080

# Comando para executar a aplicação
CMD ["java", "-jar", "target/recorder-0.0.1-SNAPSHOT.jar"]

