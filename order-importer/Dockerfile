FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
RUN chmod +x mvnw && ./mvnw -B -pl order-importer -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/order-importer/target/order-importer-*.jar /app/app.jar
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60"
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
