# ---- Етап збірки ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package

# ---- Етап запуску ----
FROM eclipse-temurin:25-jre
WORKDIR /usr/edge
COPY --from=build /build/target/edge.jar ./edge.jar
CMD ["java", "-jar", "edge.jar"]
