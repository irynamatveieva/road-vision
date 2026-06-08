package com.roadvision.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.roadvision.agent.domain.AggregatedData;
import com.roadvision.agent.domain.Parking;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.List;

/**
 * Точка входу агента. Аналог main.py з методички.
 * Підключається до MQTT-брокера та безперервно публікує дані датчиків.
 */
public class Main {

    // ObjectMapper з підтримкою java.time.Instant у форматі ISO-8601
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /** Створення та підключення MQTT-клієнта. */
    private static MqttClient connectMqtt(String broker, int port) throws MqttException {
        String serverUri = "tcp://" + broker + ":" + port;
        System.out.printf("CONNECT TO %s%n", serverUri);

        MqttClient client = new MqttClient(serverUri, MqttClient.generateClientId(), new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        client.connect(options);

        if (client.isConnected()) {
            System.out.printf("Connected to MQTT Broker (%s)!%n", serverUri);
        } else {
            System.out.printf("Failed to connect %s%n", serverUri);
            System.exit(1);
        }
        return client;
    }

    /** Публікація одного повідомлення в топік. */
    private static void publish(MqttClient client, String topic, String payload) {
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(0);
            client.publish(topic, message);
        } catch (MqttException e) {
            System.out.printf("Failed to send message to topic %s%n", topic);
        }
    }

    /** Нескінченний цикл читання даних з датчиків та публікації їх у MQTT. */
    private static void publishLoop(MqttClient client, FileDatasource datasource, double delay) throws Exception {
        datasource.startReading();
        long delayMillis = (long) (delay * 1000);
        while (true) {
            Thread.sleep(delayMillis);

            // Дані акселерометра + GPS -> топік agent
            List<AggregatedData> batch = datasource.read();
            for (AggregatedData data : batch) {
                publish(client, Config.MQTT_TOPIC, MAPPER.writeValueAsString(data));
            }

            // Дані паркінгу -> окремий топік
            List<Parking> parkingBatch = datasource.readParking();
            for (Parking parking : parkingBatch) {
                publish(client, Config.MQTT_PARKING_TOPIC, MAPPER.writeValueAsString(parking));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        MqttClient client = connectMqtt(Config.MQTT_BROKER_HOST, Config.MQTT_BROKER_PORT);
        FileDatasource datasource = new FileDatasource(
                Config.ACCELEROMETER_FILE,
                Config.GPS_FILE,
                Config.PARKING_FILE,
                Config.WEATHER_FILE,
                Config.STREET_LIGHT_FILE);
        try {
            publishLoop(client, datasource, Config.DELAY);
        } finally {
            datasource.stopReading();
            client.disconnect();
        }
    }
}
