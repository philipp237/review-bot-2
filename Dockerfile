FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} review-bot-2-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/review-bot-2-0.0.1-SNAPSHOT.jar"]