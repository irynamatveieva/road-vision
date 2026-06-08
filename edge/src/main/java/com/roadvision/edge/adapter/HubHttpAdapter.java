package com.roadvision.edge.adapter;

import com.roadvision.edge.Json;
import com.roadvision.edge.domain.ProcessedAgentData;
import com.roadvision.edge.gateway.HubGateway;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Реалізація HubGateway через HTTP: робить POST-запит на
 * {hub_url}/processed_agent_data/ (альтернатива MQTT).
 */
public class HubHttpAdapter implements HubGateway {

    private final String url;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public HubHttpAdapter(String hubBaseUrl) {
        this.url = hubBaseUrl + "/processed_agent_data/";
    }

    @Override
    public boolean saveData(ProcessedAgentData processedData) {
        try {
            String body = Json.MAPPER.writeValueAsString(processedData);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            System.out.printf("Failed to POST to Hub %s: %s%n", url, e.getMessage());
            return false;
        }
    }
}
