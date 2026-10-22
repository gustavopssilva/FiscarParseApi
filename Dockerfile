# Build do JAR fat (maven-shade-plugin)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q clean package -DskipTests

# Imagem oficial da AWS para Lambda Java 17 (já inclui o Runtime Interface Client)
FROM public.ecr.aws/lambda/java:17
COPY --from=build /app/target/ajudaqui-fiscal-lambda-1.0-SNAPSHOT.jar ${LAMBDA_TASK_ROOT}/lib/
CMD ["com.ajudaqui.handler.LambdaHandler::handleRequest"]
