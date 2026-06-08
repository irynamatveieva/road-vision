package com.roadvision.agent;

import com.roadvision.agent.domain.Accelerometer;
import com.roadvision.agent.domain.AggregatedData;
import com.roadvision.agent.domain.Gps;
import com.roadvision.agent.domain.Parking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Джерело даних, що імітує роботу реальних апаратних датчиків шляхом
 * зчитування заздалегідь підготовлених CSV-файлів.
 *
 * Реалізовано:
 *  - поступове читання: read()/readParking() повертають набір (батч) записів;
 *  - нескінченний цикл: після досягнення кінця файлу читання починається спочатку.
 */
public class FileDatasource {

    /**
     * Обгортка над одним CSV-файлом, яка вміє безперервно (по колу) видавати рядки даних.
     * При досягненні кінця файлу автоматично перевідкриває його з початку (аналог seek(0)).
     */
    private static class CsvCursor {
        private final Path file;
        private BufferedReader reader;

        CsvCursor(String filename) {
            this.file = Path.of(filename);
        }

        void open() {
            try {
                reader = Files.newBufferedReader(file);
                reader.readLine(); // пропускаємо рядок-заголовок
            } catch (IOException e) {
                throw new UncheckedIOException("Не вдалося відкрити файл: " + file, e);
            }
        }

        /** Повертає наступний рядок даних; при кінці файлу перезапускає читання з початку. */
        String nextLine() {
            try {
                String line = reader.readLine();
                if (line == null) {
                    // Кінець файлу — повертаємось на початок (нескінченний цикл)
                    close();
                    open();
                    line = reader.readLine();
                }
                return line;
            } catch (IOException e) {
                throw new UncheckedIOException("Помилка читання файлу: " + file, e);
            }
        }

        void close() {
            try {
                if (reader != null) {
                    reader.close();
                    reader = null;
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Помилка закриття файлу: " + file, e);
            }
        }
    }

    private final CsvCursor accelerometer;
    private final CsvCursor gps;
    private final CsvCursor parking;

    public FileDatasource(String accelerometerFilename, String gpsFilename, String parkingFilename) {
        this.accelerometer = new CsvCursor(accelerometerFilename);
        this.gps = new CsvCursor(gpsFilename);
        this.parking = new CsvCursor(parkingFilename);
    }

    /** Викликається перед початком читання — відкриває всі файли. */
    public void startReading() {
        accelerometer.open();
        gps.open();
        parking.open();
    }

    /**
     * Повертає набір (батч) агрегованих даних акселерометра + GPS.
     * Кожен запис отримує власну позначку часу (момент зчитування).
     */
    public List<AggregatedData> read() {
        List<AggregatedData> batch = new ArrayList<>(Config.BATCH_SIZE);
        for (int i = 0; i < Config.BATCH_SIZE; i++) {
            String[] acc = accelerometer.nextLine().split(",");
            String[] coords = gps.nextLine().split(",");

            Accelerometer accelerometerData = new Accelerometer(
                    Integer.parseInt(acc[0].trim()),
                    Integer.parseInt(acc[1].trim()),
                    Integer.parseInt(acc[2].trim()));
            Gps gpsData = new Gps(
                    Double.parseDouble(coords[0].trim()),
                    Double.parseDouble(coords[1].trim()));

            batch.add(new AggregatedData(accelerometerData, gpsData, Instant.now(), Config.USER_ID));
        }
        return batch;
    }

    /** Повертає набір (батч) даних датчика паркінгу. */
    public List<Parking> readParking() {
        List<Parking> batch = new ArrayList<>(Config.BATCH_SIZE);
        for (int i = 0; i < Config.BATCH_SIZE; i++) {
            String[] row = parking.nextLine().split(",");

            int emptyCount = Integer.parseInt(row[0].trim());
            Gps gpsData = new Gps(
                    Double.parseDouble(row[1].trim()),
                    Double.parseDouble(row[2].trim()));

            batch.add(new Parking(emptyCount, gpsData));
        }
        return batch;
    }

    /** Викликається для завершення читання — закриває всі файли. */
    public void stopReading() {
        accelerometer.close();
        gps.close();
        parking.close();
    }
}
