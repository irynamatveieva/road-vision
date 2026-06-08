package com.roadvision.edge.adapter;

import com.roadvision.edge.Json;
import com.roadvision.edge.domain.AgentData;
import com.roadvision.edge.domain.ProcessedAgentData;
import com.roadvision.edge.gateway.AgentGateway;
import com.roadvision.edge.gateway.HubGateway;
import com.roadvision.edge.usecase.DataProcessing;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Реалізація AgentGateway через MQTT: підписується на топік агента
 * (agent_data_topic), аналізує кожне отримане повідомлення (визначає стан
 * дороги) та передає результат на Hub через HubGateway.
 */
public class AgentMqttAdapter implements AgentGateway {

    private final String brokerHost;
    private final int brokerPort;
    private final String topic;
    private final HubGateway hubGateway;
    private final MqttClient client;

    public AgentMqttAdapter(String brokerHost, int brokerPort, String topic, HubGateway hubGateway) {
        this.brokerHost = brokerHost;
        this.brokerPort = brokerPort;
        this.topic = topic;
        this.hubGateway = hubGateway;
        try {
            String serverUri = "tcp://" + brokerHost + ":" + brokerPort;
            this.client = new MqttClient(serverUri, MqttClient.generateClientId(), new MemoryPersistence());
        } catch (MqttException e) {
            throw new RuntimeException("Failed to create Agent MQTT client", e);
        }
    }

    /** Обробка вхідного повідомлення від агента. */
    private void onMessage(MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            AgentData agentData = Json.MAPPER.readValue(payload, AgentData.class);
            ProcessedAgentData processed = DataProcessing.processAgentData(agentData);
            hubGateway.saveData(processed);
        } catch (Exception e) {
            System.out.printf("Error processing agent message: %s%n", e.getMessage());
        }
    }

    @Override
    public void connect() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.printf("Agent MQTT connection lost: %s%n", cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    onMessage(message);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });
            client.connect(options);
            System.out.printf("Agent MQTT adapter connected to tcp://%s:%d%n", brokerHost, brokerPort);
        } catch (MqttException e) {
            throw new RuntimeException("Failed to connect Agent MQTT adapter", e);
        }
    }

    @Override
    public void start() {
        try {
            client.subscribe(topic);
            System.out.printf("Subscribed to agent topic '%s'%n", topic);
        } catch (MqttException e) {
            throw new RuntimeException("Failed to subscribe to topic " + topic, e);
        }
    }

    @Override
    public void stop() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            System.out.printf("Error stopping Agent MQTT adapter: %s%n", e.getMessage());
        }
    }
}
