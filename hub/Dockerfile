# ---- Етап збірки ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

# ---- Етап запуску ----
FROM eclipse-temurin:25-jre
WORKDIR /usr/hub
COPY --from=build /build/target/hub.jar ./hub.jar
EXPOSE 8000
CMD ["java", "-jar", "hub.jar"]
