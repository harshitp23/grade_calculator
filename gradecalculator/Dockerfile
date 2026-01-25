# 1. Use a Linux base image with Java 23
FROM eclipse-temurin:23-jdk-alpine

# 2. Install Maven manually (bypassing the missing wrapper)
RUN apk add --no-cache maven

# 3. Set work directory
WORKDIR /app

# 4. Copy project files
COPY . .

# 5. Build the project using 'mvn' (not ./mvnw)
RUN mvn clean package -DskipTests

# 6. Run the JAR file
CMD ["java", "-jar", "target/gradecalculator-0.0.1-SNAPSHOT.jar"]
