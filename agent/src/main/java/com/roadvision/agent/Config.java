package com.roadvision.agent;

/**
 * Конфігурація агента. Аналог config.py з методички.
 * Значення читаються зі змінних оточення (їх передає Docker),
 * за відсутності — використовуються значення за замовчуванням.
 */
public final class Config {

    private Config() {
    }

    /** Спроба розпарсити рядок у число; при помилці повертає значення за замовчуванням. */
    private static int tryParseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static double tryParseDouble(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    // Ідентифікатор користувача/пристрою
    public static final int USER_ID = tryParseInt(System.getenv("USER_ID"), 1);

    // MQTT config
    public static final String MQTT_BROKER_HOST = env("MQTT_BROKER_HOST", "mqtt");
    public static final int MQTT_BROKER_PORT = tryParseInt(System.getenv("MQTT_BROKER_PORT"), 1883);
    public static final String MQTT_TOPIC = env("MQTT_TOPIC", "agent_data_topic");
    public static final String MQTT_PARKING_TOPIC = env("MQTT_PARKING_TOPIC", "parking_data_topic");

    // Шляхи до файлів з тестовими даними датчиків
    public static final String ACCELEROMETER_FILE = env("ACCELEROMETER_FILE", "data/accelerometer.csv");
    public static final String GPS_FILE = env("GPS_FILE", "data/gps.csv");
    public static final String PARKING_FILE = env("PARKING_FILE", "data/parking.csv");

    // Затримка між відправками даних у MQTT (секунди)
    public static final double DELAY = tryParseDouble(System.getenv("DELAY"), 1.0);

    // Скільки записів повертає read() за одне звернення (поступове читання)
    public static final int BATCH_SIZE = tryParseInt(System.getenv("BATCH_SIZE"), 5);
}
