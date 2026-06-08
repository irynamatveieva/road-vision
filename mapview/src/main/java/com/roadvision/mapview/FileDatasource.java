package com.roadvision.mapview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Джерело даних з CSV-файлу для візуалізації на карті.
 * Читає файл data.csv (стовпці: road_state, latitude, longitude) та повертає
 * список точок маршруту з визначеним станом дороги.
 */
public class FileDatasource {

    /** Одна точка маршруту: стан дороги та координати. */
    public record Point(String roadState, double latitude, double longitude) {
    }

    private final String resourceName;

    public FileDatasource(String resourceName) {
        this.resourceName = resourceName;
    }

    /** Зчитати всі точки з CSV-файлу. */
    public List<Point> read() {
        List<Point> points = new ArrayList<>();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IllegalStateException("Ресурс не знайдено: " + resourceName);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            reader.readLine(); // пропускаємо заголовок
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",");
                points.add(new Point(
                        parts[0].trim(),
                        Double.parseDouble(parts[1].trim()),
                        Double.parseDouble(parts[2].trim())));
            }
        } catch (IOException e) {
            throw new RuntimeException("Помилка читання " + resourceName, e);
        }
        return points;
    }
}
