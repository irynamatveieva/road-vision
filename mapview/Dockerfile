# ---- Етап збірки ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package

# ---- Етап запуску ----
FROM eclipse-temurin:25-jre
WORKDIR /usr/mapview
COPY --from=build /build/target/mapview.jar ./mapview.jar
EXPOSE 8050
CMD ["java", "-jar", "mapview.jar"]
