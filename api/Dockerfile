FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/target/api-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
VOLUME ["/app/data"]
ENTRYPOINT ["java","-jar","/app/app.jar"]


