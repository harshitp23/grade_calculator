FROM eclipse-temurin:23-jdk-alpine
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests
CMD ["java", "-jar", "target/gradecalculator-0.0.1-SNAPSHOT.jar"]
