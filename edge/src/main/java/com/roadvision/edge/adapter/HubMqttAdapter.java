package com.roadvision.edge.adapter;

import com.roadvision.edge.Json;
import com.roadvision.edge.domain.ProcessedAgentData;
import com.roadvision.edge.gateway.HubGateway;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Реалізація HubGateway через MQTT: публікує проаналізовані дані у топік Hub
 * (processed_data_topic).
 */
public class HubMqttAdapter implements HubGateway {

    private final String topic;
    private final MqttClient client;

    public HubMqttAdapter(String broker, int port, String topic) {
        this.topic = topic;
        try {
            String serverUri = "tcp://" + broker + ":" + port;
            this.client = new MqttClient(serverUri, MqttClient.generateClientId(), new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            client.connect(options);
            System.out.printf("Hub MQTT adapter connected to %s, topic '%s'%n", serverUri, topic);
        } catch (MqttException e) {
            throw new RuntimeException("Failed to connect Hub MQTT adapter", e);
        }
    }

    @Override
    public boolean saveData(ProcessedAgentData processedData) {
        try {
            String payload = Json.MAPPER.writeValueAsString(processedData);
            client.publish(topic, new MqttMessage(payload.getBytes()));
            return true;
        } catch (Exception e) {
            System.out.printf("Failed to publish to Hub topic %s: %s%n", topic, e.getMessage());
            return false;
        }
    }
}
