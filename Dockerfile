FROM openjdk:17-alpine
# Set working directory
WORKDIR /app
# Copy application jar file
COPY build/libs/hufsting-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "./app.jar"]