FROM openjdk:21-jdk-slim
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test
CMD ["java", "-Xmx400m", "-Xms400m", "-jar", "build/libs/demo-0.0.1-SNAPSHOT.jar"]