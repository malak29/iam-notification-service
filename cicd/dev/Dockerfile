FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy the built jar file (relative to build context)
COPY ../../build/libs/*.jar app.jar

# Expose port
EXPOSE 8081

# Development configuration
ENV SPRING_PROFILES_ACTIVE=dev
ENV JAVA_OPTS="-Xmx512m -Xms256m -Ddebug=true"

# Run with development settings
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]