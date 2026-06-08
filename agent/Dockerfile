# ---- Етап збірки ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
# спочатку копіюємо pom.xml та підвантажуємо залежності (кешування шарів)
COPY pom.xml .
RUN mvn -q dependency:go-offline
# копіюємо код та збираємо fat-jar
COPY src ./src
RUN mvn -q clean package

# ---- Етап запуску ----
FROM eclipse-temurin:25-jre
WORKDIR /usr/agent
# копіюємо зібраний JAR та тестові дані датчиків
COPY --from=build /build/target/agent.jar ./agent.jar
COPY data ./data
# команда запуску при старті контейнера
CMD ["java", "-jar", "agent.jar"]
