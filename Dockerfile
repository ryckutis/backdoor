FROM openjdk:21-jdk-slim
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean build
CMD ["./gradlew", "bootRun"]