# Store — API зберігання та доступу до даних (Лабораторна робота №2)

Java/Spring Boot реалізація Store-сервісу системи моніторингу стану дорожнього
покриття. Зберігає проаналізовані дані (`ProcessedAgentData`) у PostgreSQL,
надає CRUD-операції та WebSocket для оновлення даних на UI-клієнтах.

## Стек технологій

- **Java 25**, **Spring Boot 3.5**
- **Spring Web** — REST API
- **Spring Data JPA** — ORM (аналог SQLAlchemy)
- **Spring WebSocket** — підписка UI на оновлення
- **PostgreSQL** + **pgAdmin**
- **springdoc-openapi** — Swagger UI (аналог автодокументації FastAPI)
- **Docker / Docker Compose**

## Структура

```
store/
├── src/main/java/com/roadvision/store/
│   ├── StoreApplication.java          # точка входу
│   ├── dto/                           # моделі API
│   │   ├── AccelerometerData.java
│   │   ├── GpsData.java
│   │   ├── AgentData.java
│   │   ├── ProcessedAgentData.java        # вхідна (road_state + agent_data)
│   │   └── ProcessedAgentDataInDB.java    # вихідна (рядок БД)
│   ├── entity/ProcessedAgentDataEntity.java   # JPA-сутність
│   ├── repository/ProcessedAgentDataRepository.java
│   ├── controller/ProcessedAgentDataController.java  # CRUDL
│   ├── websocket/SubscriptionWebSocketHandler.java
│   └── config/WebSocketConfig.java
├── src/main/resources/application.properties
├── docker/
│   ├── docker-compose.yaml            # postgres + pgadmin + store
│   └── db/structure.sql              # створення таблиці
├── Dockerfile
└── pom.xml
```

## API (CRUDL)

| Метод | Шлях | Опис |
|---|---|---|
| POST | `/processed_agent_data/` | створити записи (приймає список) + розсилка в WebSocket |
| GET | `/processed_agent_data/{id}` | отримати запис за id |
| GET | `/processed_agent_data/` | список усіх записів |
| PUT | `/processed_agent_data/{id}` | оновити запис |
| DELETE | `/processed_agent_data/{id}` | видалити запис |
| WS | `/ws/{user_id}` | підписка UI на оновлення для user_id |

## Запуск

```bash
cd docker
docker compose up --build
```

## Перевірка

- **Swagger UI:** http://localhost:8000/docs
- **pgAdmin:** http://localhost:5050 (admin@admin.com / root) →
  підключення до сервера `postgres_db` (host: `postgres_db`, user: `user`, pass: `pass`, db: `test_db`)
