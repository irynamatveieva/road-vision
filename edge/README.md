# Edge — модуль первинної обробки даних (Лабораторна робота №4)

Java-реалізація Edge Data Logic. Приймає дані з датчиків від Agent через MQTT
(`agent_data_topic`), аналізує стан дорожнього покриття та відправляє
проаналізовані дані (`ProcessedAgentData`) на Hub через MQTT
(`processed_data_topic`) або HTTP.

## Стек технологій

- **Java 25**
- **Eclipse Paho** — MQTT-клієнт (прийом від Agent + відправка на Hub)
- **Jackson** — серіалізація JSON
- **Maven**, **Docker / Docker Compose**

## Архітектура (порти й адаптери)

```
Agent → MQTT(agent_data_topic) → [Edge] → MQTT(processed_data_topic) → Hub → Store → DB
```

- `AgentGateway` (інтерфейс) → `AgentMqttAdapter` — прийом даних від Agent
- `DataProcessing.processAgentData` — класифікація стану дороги
- `HubGateway` (інтерфейс) → `HubMqttAdapter` / `HubHttpAdapter` — відправка на Hub

## Логіка класифікації

За показниками акселерометра (вісь Z у спокої ≈ 16500):
- велике відхилення Z або сильний поштовх по Y → **pothole** (яма);
- помірне відхилення → **bump** (вибоїна);
- інакше → **smooth** (рівна дорога).

## Структура

```
edge/
├── src/main/java/com/roadvision/edge/
│   ├── Main.java
│   ├── Config.java
│   ├── Json.java                     # спільний ObjectMapper (snake_case)
│   ├── domain/                       # AccelerometerData, GpsData, AgentData, ProcessedAgentData
│   ├── usecase/DataProcessing.java   # process_agent_data
│   ├── gateway/                      # AgentGateway, HubGateway (інтерфейси)
│   └── adapter/                      # AgentMqttAdapter, HubMqttAdapter, HubHttpAdapter
├── docker/
│   ├── docker-compose.yaml           # ВЕСЬ бекенд: agent+edge+hub+store+redis+postgres+pgadmin+mqtt
│   ├── db/structure.sql
│   └── mosquitto/config/mosquitto.conf
├── Dockerfile
└── pom.xml
```

## Запуск (вся система)

```bash
cd docker
docker compose up --build
```

Піднімається повний конвеєр: **Agent → Edge → Hub → Store → PostgreSQL**.

## Перевірка

- **MQTT Explorer** (`localhost:1883`): топіки `agent_data_topic` (від агента)
  та `processed_data_topic` (від edge, з полем `road_state`).
- **pgAdmin** (`localhost:5050`): у таблиці `processed_agent_data` накопичуються
  записи з визначеним станом дороги.
- **Store API** (`localhost:8000/docs`), **Hub** (`localhost:9000/docs`).

## Порти

| Сервіс | Хост | Контейнер |
|---|---|---|
| store | 8000 | 8000 |
| hub | 9000 | 8000 |
| postgres | 5434 | 5432 |
| redis | 6380 | 6379 |
| pgadmin | 5050 | 80 |
| mqtt | 1883 | 1883 |
