FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
RUN chmod +x mvnw && ./mvnw -B -pl mock-marketplace -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/mock-marketplace/target/mock-marketplace-*.jar /app/app.jar
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60"
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
