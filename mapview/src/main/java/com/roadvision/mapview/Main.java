package com.roadvision.mapview;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * MapView — вебсервіс візуалізації стану дороги на карті.
 * Віддає статичний фронтенд (Leaflet) та реалізує FileDatasource через
 * ендпоінт /api/file-data. Фронтенд також підписується на дані зі Store
 * через WebSocket для відображення в реальному часі.
 */
public class Main {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static int env(String name, int def) {
        try {
            return Integer.parseInt(System.getenv(name));
        } catch (Exception e) {
            return def;
        }
    }

    private static String env(String name, String def) {
        String v = System.getenv(name);
        return (v == null || v.isBlank()) ? def : v;
    }

    public static void main(String[] args) throws IOException {
        int port = env("PORT", 8050);
        FileDatasource datasource = new FileDatasource("static/data.csv");

        // Хост/порт Store для WebSocket-підписки (фронтенд читає з /api/config)
        String storeWsHost = env("STORE_WS_HOST", "localhost");
        int storeWsPort = env("STORE_WS_PORT", 8000);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // FileDatasource API — дані з CSV-файлу у форматі JSON
        server.createContext("/api/file-data", exchange -> {
            byte[] body = MAPPER.writeValueAsBytes(datasource.read());
            send(exchange, 200, "application/json", body);
        });

        // Конфігурація для фронтенду (де шукати Store WebSocket)
        server.createContext("/api/config", exchange -> {
            String json = "{\"storeWsHost\":\"" + storeWsHost + "\",\"storeWsPort\":" + storeWsPort + "}";
            send(exchange, 200, "application/json", json.getBytes(StandardCharsets.UTF_8));
        });

        // Статичні файли фронтенду
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }
            String resource = "static" + path;
            try (InputStream in = Main.class.getClassLoader().getResourceAsStream(resource)) {
                if (in == null) {
                    send(exchange, 404, "text/plain", "Not Found".getBytes(StandardCharsets.UTF_8));
                    return;
                }
                send(exchange, 200, contentType(path), in.readAllBytes());
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.printf("MapView started on http://localhost:%d (Store WS: %s:%d)%n",
                port, storeWsHost, storeWsPort);
    }

    private static void send(HttpExchange exchange, int status, String contentType, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private static String contentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".csv")) return "text/csv";
        return "application/octet-stream";
    }
}
