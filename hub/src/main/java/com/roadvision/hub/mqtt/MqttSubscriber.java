package com.roadvision.hub.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadvision.hub.dto.ProcessedAgentData;
import com.roadvision.hub.service.HubService;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * MQTT-підписник: отримує проаналізовані дані від Edge з топіка
 * processed_data_topic і передає їх у HubService для накопичення.
 */
@Component
public class MqttSubscriber {

    private static final Logger log = LoggerFactory.getLogger(MqttSubscriber.class);

    private final HubService hubService;
    private final ObjectMapper objectMapper;

    private final String brokerHost;
    private final int brokerPort;
    private final String topic;

    public MqttSubscriber(HubService hubService,
                          ObjectMapper objectMapper,
                          @Value("${mqtt.broker.host:localhost}") String brokerHost,
                          @Value("${mqtt.broker.port:1883}") int brokerPort,
                          @Value("${mqtt.topic:processed_data_topic}") String topic) {
        this.hubService = hubService;
        this.objectMapper = objectMapper;
        this.brokerHost = brokerHost;
        this.brokerPort = brokerPort;
        this.topic = topic;
    }

    @PostConstruct
    public void connect() throws Exception {
        String serverUri = "tcp://" + brokerHost + ":" + brokerPort;
        MqttClient client = new MqttClient(serverUri, MqttClient.generateClientId(), new MemoryPersistence());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        client.setCallback(new org.eclipse.paho.client.mqttv3.MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.warn("MQTT connection lost: {}", cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) {
                try {
                    String payload = new String(message.getPayload());
                    ProcessedAgentData data = objectMapper.readValue(payload, ProcessedAgentData.class);
                    hubService.process(data);
                } catch (Exception e) {
                    log.error("Error processing MQTT message: {}", e.getMessage());
                }
            }

            @Override
            public void deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken token) {
            }
        });

        client.connect(options);
        client.subscribe(topic);
        log.info("Connected to MQTT broker {} and subscribed to topic '{}'", serverUri, topic);
    }
}
