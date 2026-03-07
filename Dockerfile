# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jdk

# Set the working directory in the container
WORKDIR /app
# Declare the VERSION argument with a default value (optional)
ARG VERSION=1.0.0
# Add a LABEL with the version
LABEL version=$VERSION
#to pass the jar location
ARG JAR_FILE
# Copy the built JAR file into the container
COPY $JAR_FILE app.jar

# Expose the port your application runs on (optional, but good practice)
EXPOSE 9000

# Run the JAR file when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]
