# Agent — модуль збору даних (Лабораторна робота №1)

Java-реалізація агента системи моніторингу стану дорожнього покриття.
Агент імітує роботу апаратних датчиків (акселерометр, GPS, датчик паркінгу),
зчитуючи дані з CSV-файлів, і публікує їх на Edge через MQTT.

## Стек технологій

- **Java 25**
- **Eclipse Paho** — MQTT-клієнт (аналог `paho-mqtt`)
- **Jackson** — серіалізація об'єктів у JSON (аналог `marshmallow`)
- **Maven** — збірка проєкту
- **Docker / Docker Compose** — контейнеризація та локальний запуск

## Структура

```
agent/
├── data/                      # тестові дані датчиків (CSV)
│   ├── accelerometer.csv
│   ├── gps.csv
│   └── parking.csv
├── src/main/java/com/roadvision/agent/
│   ├── Main.java              # точка входу: MQTT + цикл публікації
│   ├── Config.java            # конфігурація зі змінних оточення
│   ├── FileDatasource.java    # читання CSV (поступове + нескінченний цикл)
│   └── domain/                # моделі даних
│       ├── Accelerometer.java
│       ├── Gps.java
│       ├── Parking.java
│       └── AggregatedData.java
├── docker/
│   ├── docker-compose.yaml
│   └── mosquitto/config/mosquitto.conf
├── Dockerfile
└── pom.xml
```

## Особливості реалізації

- **Поступове читання** — `read()` / `readParking()` повертають набір (батч) із
  `BATCH_SIZE` записів за одне звернення.
- **Нескінченний цикл** — після досягнення кінця CSV-файлу читання
  автоматично починається з початку (аналог `seek(0)`).
- **Додатковий датчик** — `parking` публікується в окремий MQTT-топік.

## Запуск

```bash
cd docker
docker compose up --build
```

Агент під'єднається до брокера `mqtt` і почне публікувати дані в топіки
`agent_data_topic` та `parking_data_topic`.

## Перевірка

Підключитися до брокера `localhost:1883` через **MQTT Explorer** і
підписатися на топіки `agent_data_topic` / `parking_data_topic`.

## Конфігурація (змінні оточення)

| Змінна | За замовчуванням | Опис |
|---|---|---|
| `MQTT_BROKER_HOST` | `mqtt` | хост брокера |
| `MQTT_BROKER_PORT` | `1883` | порт брокера |
| `MQTT_TOPIC` | `agent_data_topic` | топік для даних акселерометра+GPS |
| `MQTT_PARKING_TOPIC` | `parking_data_topic` | топік для даних паркінгу |
| `DELAY` | `1.0` | затримка між відправками (с) |
| `BATCH_SIZE` | `5` | кількість записів за одне читання |
| `USER_ID` | `1` | ідентифікатор пристрою |
