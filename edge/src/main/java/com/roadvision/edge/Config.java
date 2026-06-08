package com.roadvision.edge;

/**
 * Конфігурація Edge. Значення читаються зі змінних оточення (їх передає Docker),
 * за відсутності використовуються значення за замовчуванням.
 */
public final class Config {

    private Config() {
    }

    private static int parseInt(String value, int def) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return def;
        }
    }

    private static String env(String name, String def) {
        String v = System.getenv(name);
        return (v == null || v.isBlank()) ? def : v;
    }

    // MQTT-брокер, з якого приходять дані агента
    public static final String MQTT_BROKER_HOST = env("MQTT_BROKER_HOST", "localhost");
    public static final int MQTT_BROKER_PORT = parseInt(System.getenv("MQTT_BROKER_PORT"), 1883);
    public static final String MQTT_TOPIC = env("MQTT_TOPIC", "agent_data_topic");

    // MQTT-параметри для відправки проаналізованих даних на Hub
    public static final String HUB_MQTT_BROKER_HOST = env("HUB_MQTT_BROKER_HOST", "localhost");
    public static final int HUB_MQTT_BROKER_PORT = parseInt(System.getenv("HUB_MQTT_BROKER_PORT"), 1883);
    public static final String HUB_MQTT_TOPIC = env("HUB_MQTT_TOPIC", "processed_data_topic");

    // HTTP-параметри Hub (альтернативний спосіб відправки)
    public static final String HUB_HOST = env("HUB_HOST", "localhost");
    public static final int HUB_PORT = parseInt(System.getenv("HUB_PORT"), 8000);
    public static final String HUB_URL = "http://" + HUB_HOST + ":" + HUB_PORT;
}
