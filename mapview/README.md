# MapView — модуль візуалізації (Лабораторна робота №5)

Вебдодаток для візуалізації стану дорожнього покриття на карті. Відображає дані
двома способами:
1. **FileDatasource** — з CSV-файлу (`data.csv`);
2. **наживо зі Store** — підписка через WebSocket (`/ws/{user_id}`).

Точки маршруту фарбуються за станом дороги: 🟢 smooth, 🟠 bump, 🔴 pothole.

## Стек технологій

- **Java 25** — вбудований `com.sun.net.httpserver` (без фреймворків)
- **Jackson** — серіалізація даних FileDatasource у JSON
- **Leaflet.js** + **OpenStreetMap** — карта (фронтенд)
- **Maven**, **Docker**

## Структура

```
mapview/
├── src/main/java/com/roadvision/mapview/
│   ├── Main.java            # HTTP-сервер: статика + /api/file-data + /api/config
│   └── FileDatasource.java  # читання data.csv
├── src/main/resources/static/
│   ├── index.html           # карта Leaflet + логіка (файл / Store WebSocket)
│   └── data.csv             # дані маршруту (road_state, latitude, longitude)
├── Dockerfile
└── pom.xml
```

## Запуск

Окремо:
```bash
mvn clean package
PORT=8050 java -jar target/mapview.jar
```
Потім відкрити http://localhost:8050

У складі всієї системи — через docker-compose модуля edge (див. нижче).

## Використання

- **«Завантажити з файлу»** — візуалізує маршрут із `data.csv` (FileDatasource).
- **«Підключитись до Store (наживо)»** — підписується на WebSocket Store
  (`ws://localhost:8000/ws/1`) і відображає нові точки в реальному часі.
  Потрібно, щоб працювала вся система (Agent → Edge → Hub → Store).

## Примітка про координати

У вихідних даних широта й довгота підписані навпаки (Київ — це 50.45 широти,
30.52 довготи). MapView містить автокорекцію `normalize()`, тож точки коректно
відображаються на Києві незалежно від порядку полів.
