# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-21 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the root pom.xml and all modules pom.xml files to cache dependencies
COPY pom.xml .
COPY settings-domain/pom.xml settings-domain/
COPY settings-service/pom.xml settings-service/
COPY settings-client/pom.xml settings-client/
COPY settings-client-spring-boot-starter/pom.xml settings-client-spring-boot-starter/
COPY settings-webflux-app-example/pom.xml settings-webflux-app-example/

# Download dependencies (this step will be cached unless poms change)
RUN mvn dependency:go-offline -B

# Copy the source code for all required modules
COPY settings-domain/src settings-domain/src
COPY settings-client/src settings-client/src
COPY settings-webflux-app-example/src settings-webflux-app-example/src

# Build the application and its local dependencies
# We only need to install settings-domain and settings-client to the local repo
# so that settings-webflux-app-example can find them.
RUN mvn clean install -DskipTests -pl settings-domain,settings-client,settings-webflux-app-example -am

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre

# Set the working directory
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/settings-webflux-app-example/target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
