# Usa a imagem do OpenJDK
FROM openjdk:21-jdk

# Define o diretório de trabalho dentro do container
WORKDIR /app

COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

# Copia o arquivo JAR para dentro do container
COPY target/redesocial-0.0.1-SNAPSHOT.jar app.jar

# Exposição da porta usada pelo Spring Boot
EXPOSE 8080

# Comando para rodar a aplicação
CMD ["/bin/sh", "-c", "/wait-for-it.sh postgres:5432 --timeout=60 --strict -- /wait-for-it.sh kafka:9092 --timeout=60 --strict -- java -jar app.jar"]
