# Hub — модуль накопичення даних (Лабораторна робота №3)

Java/Spring Boot реалізація Hub-сервісу. Приймає проаналізовані дані
(`ProcessedAgentData`) від Edge через **MQTT** або **HTTP**, накопичує їх у
**Redis** і пакетами (по `BATCH_SIZE` записів) зберігає у **Store API**.

## Стек технологій

- **Java 25**, **Spring Boot 3.5**
- **Spring Web** — HTTP-прийом даних + HTTP-клієнт до Store (RestClient)
- **Spring Data Redis** — буфер накопичення
- **Eclipse Paho** — MQTT-клієнт (прийом даних від Edge)
- **springdoc-openapi** — Swagger UI
- **Docker / Docker Compose**

## Як працює

1. Дані надходять через MQTT (топік `processed_data_topic`) або HTTP
   (`POST /processed_agent_data/`).
2. Кожен запис додається у Redis-буфер.
3. Коли в буфері накопичується `BATCH_SIZE` записів — пакет витягується
   і зберігається у Store через `StoreApiAdapter` (HTTP POST).

## Структура

```
hub/
├── src/main/java/com/roadvision/hub/
│   ├── HubApplication.java
│   ├── dto/                          # моделі даних (з попередніх лаб)
│   ├── gateway/StoreGateway.java     # інтерфейс шлюзу до Store
│   ├── adapter/StoreApiAdapter.java  # реалізація: HTTP POST до Store
│   ├── service/HubService.java       # накопичення в Redis + пакетне збереження
│   ├── controller/ProcessedAgentDataController.java  # HTTP-прийом
│   └── mqtt/MqttSubscriber.java      # MQTT-прийом
├── src/main/resources/application.properties
├── docker/
│   ├── docker-compose.yaml           # mqtt + postgres + pgadmin + store + redis + hub
│   ├── db/structure.sql
│   └── mosquitto/config/mosquitto.conf
├── Dockerfile
└── pom.xml
```

## Запуск

```bash
cd docker
docker compose up --build
```

Піднімається весь бекенд: MQTT-брокер, PostgreSQL, pgAdmin, Store, Redis, Hub.

## Перевірка

- Надіслати дані в Hub через MQTT (топік `processed_data_topic`) або HTTP
  (`POST http://localhost:9000/processed_agent_data/`).
- Після накопичення `BATCH_SIZE` записів вони з'являться у Store:
  `GET http://localhost:8000/processed_agent_data/`.
- **Swagger Hub:** http://localhost:9000/docs
- **Swagger Store:** http://localhost:8000/docs
- **pgAdmin:** http://localhost:5050 (admin@admin.com / root)

## Порти

| Сервіс | Хост | Контейнер |
|---|---|---|
| hub | 9000 | 8000 |
| store | 8000 | 8000 |
| postgres | 5434 | 5432 |
| redis | 6380 | 6379 |
| pgadmin | 5050 | 80 |
| mqtt | 1883 | 1883 |
