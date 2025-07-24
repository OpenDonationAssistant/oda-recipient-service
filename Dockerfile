FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY target/oda-*.jar /app/app.jar

CMD ["java","--add-opens","java.base/java.time=ALL-UNNAMED","-jar","app.jar"]
