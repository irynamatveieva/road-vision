package com.roadvision.edge;

import com.roadvision.edge.adapter.AgentMqttAdapter;
import com.roadvision.edge.adapter.HubMqttAdapter;
import com.roadvision.edge.gateway.AgentGateway;
import com.roadvision.edge.gateway.HubGateway;

/**
 * Точка входу Edge Data Logic. Приймає дані від агента через MQTT, аналізує
 * стан дороги та відправляє проаналізовані дані на Hub.
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        // Шлюз до Hub — відправка проаналізованих даних через MQTT.
        // (альтернатива: new HubHttpAdapter(Config.HUB_URL))
        HubGateway hubGateway = new HubMqttAdapter(
                Config.HUB_MQTT_BROKER_HOST,
                Config.HUB_MQTT_BROKER_PORT,
                Config.HUB_MQTT_TOPIC);

        // Шлюз до Agent — прийом даних із топіка агента.
        AgentGateway agentGateway = new AgentMqttAdapter(
                Config.MQTT_BROKER_HOST,
                Config.MQTT_BROKER_PORT,
                Config.MQTT_TOPIC,
                hubGateway);

        agentGateway.connect();
        agentGateway.start();

        Runtime.getRuntime().addShutdownHook(new Thread(agentGateway::stop));

        // Тримаємо процес активним
        Thread.currentThread().join();
    }
}
