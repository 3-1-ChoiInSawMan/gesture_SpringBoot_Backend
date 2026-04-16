FROM eclipse-temurin:21-jdk-slim

COPY build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]