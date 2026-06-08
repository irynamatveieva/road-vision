package com.roadvision.agent.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Random;

/**
 * Генератор синтетичних даних для нових сенсорів (погода, вуличне освітлення).
 *
 * Дані генеруються не простою рівномірною випадковістю, а з відтворенням
 * статистичних властивостей реальних відкритих датасетів:
 *
 *  • Weather — температура й вологість моделюються КОРЕЛЬОВАНИМИ нормальними
 *    розподілами через декомпозицію Холецького (від'ємна кореляція -0.6, як у
 *    реальних метеоданих NOAA/OpenWeather); опади залежать від вологості.
 *  • StreetLight — освітленість моделюється ДОБОВИМ циклом (синус висоти сонця)
 *    з шумом; ліхтар вмикається, коли стає темно (lux нижче порогу).
 *
 * Запуск:  java -cp target/agent.jar com.roadvision.agent.tools.SyntheticDataGenerator
 */
public final class SyntheticDataGenerator {

    private static final int ROWS = 200;
    private static final Random RNG = new Random(42); // фіксований seed для відтворюваності

    private SyntheticDataGenerator() {
    }

    public static void main(String[] args) throws IOException {
        Path dataDir = Path.of(args.length > 0 ? args[0] : "data");
        Files.createDirectories(dataDir);
        generateWeather(dataDir.resolve("weather.csv"));
        generateStreetLight(dataDir.resolve("street_light.csv"));
        System.out.println("Done! Synthetic data generated based on open-dataset statistics.");
    }

    /** Погода: корельовані температура/вологість (Холецький) + опади від вологості. */
    private static void generateWeather(Path file) throws IOException {
        // Параметри з реальних метеоданих
        double tMean = 8.0, tStd = 7.0;     // температура, °C
        double hMean = 72.0, hStd = 14.0;   // вологість, %
        double corr = -0.6;                  // тепліше -> сухіше

        // Матриця Холецького для кореляційної матриці [[1, corr],[corr, 1]]
        double l11 = 1.0;
        double l21 = corr;
        double l22 = Math.sqrt(1 - corr * corr);

        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8))) {
            w.println("temperature,humidity,precipitation");
            for (int i = 0; i < ROWS; i++) {
                double z1 = RNG.nextGaussian();
                double z2 = RNG.nextGaussian();
                // корельовані стандартні нормальні
                double c1 = l11 * z1;
                double c2 = l21 * z1 + l22 * z2;

                double temperature = clip(tMean + c1 * tStd, -15, 35);
                double humidity = clip(hMean + c2 * hStd, 10, 100);

                // Опади: ймовірніші за високої вологості (експоненційний хвіст)
                double precipitation = 0.0;
                if (humidity > 80 && RNG.nextDouble() < (humidity - 80) / 20.0) {
                    precipitation = round(-Math.log(1 - RNG.nextDouble()) * 3.0, 2); // exp(mean=3мм)
                }

                w.printf(Locale.ROOT, "%.2f,%.2f,%.2f%n", round(temperature, 2), round(humidity, 2), precipitation);
            }
        }
        System.out.println("Generated " + ROWS + " weather records -> " + file);
    }

    /** Освітлення: добовий цикл (синус висоти сонця) + шум; ліхтар вмикається в темряві. */
    private static void generateStreetLight(Path file) throws IOException {
        int period = ROWS; // повний цикл доби на весь файл
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8))) {
            w.println("lux,is_on");
            for (int i = 0; i < ROWS; i++) {
                // висота сонця: синус, від'ємні значення (ніч) -> 0
                double sun = Math.sin(2 * Math.PI * i / period - Math.PI / 2); // мінімум на початку (ніч)
                double daylight = Math.max(0, sun);
                double lux = daylight * 80000 * (0.9 + 0.2 * RNG.nextDouble()); // макс ~80000 лк опівдні
                lux = round(clip(lux, 0, 100000), 1);
                boolean isOn = lux < 50; // ліхтар увімкнено, коли темно
                w.printf(Locale.ROOT, "%.1f,%b%n", lux, isOn);
            }
        }
        System.out.println("Generated " + ROWS + " street light records -> " + file);
    }

    private static double clip(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private static double round(double v, int digits) {
        double f = Math.pow(10, digits);
        return Math.round(v * f) / f;
    }
}
