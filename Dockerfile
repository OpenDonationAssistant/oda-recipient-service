FROM eclipse-temurin:17-jdk-jammy
LABEL org.opencontainers.image.source=https://github.com/opendonationasssistant/oda-recipient-service
WORKDIR /app
COPY target/oda-recipient-service-0.1.jar /app

CMD ["java","--add-opens","java.base/java.time=ALL-UNNAMED","-jar","oda-recipient-service-0.1.jar"]
