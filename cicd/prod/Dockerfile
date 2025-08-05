FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy the built jar file
COPY ../../build/libs/*.jar app.jar

# Expose port
EXPOSE 8081

# Production configuration
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx1024m -Xms512m -server"

# Run with production settings
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]