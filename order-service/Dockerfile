FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
RUN chmod +x mvnw && ./mvnw -B -pl order-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/order-service/target/order-service-*.jar /app/app.jar
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60"
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
