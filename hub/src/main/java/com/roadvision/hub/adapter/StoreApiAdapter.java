package com.roadvision.hub.adapter;

import com.roadvision.hub.dto.ProcessedAgentData;
import com.roadvision.hub.gateway.StoreGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Реалізація StoreGateway через HTTP-запит до Store API.
 * Робить POST-запит на {store_base_url}/processed_agent_data/ зі списком
 * елементів ProcessedAgentData (аналог StoreApiAdapter у методичці).
 */
@Component
public class StoreApiAdapter implements StoreGateway {

    private static final Logger log = LoggerFactory.getLogger(StoreApiAdapter.class);

    private final RestClient restClient;

    public StoreApiAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${store.api.host:localhost}") String storeApiHost,
            @Value("${store.api.port:8000}") int storeApiPort) {
        String baseUrl = "http://" + storeApiHost + ":" + storeApiPort;
        // Використовуємо Spring-керований Builder — він налаштований з конвертерами
        // застосунку (включно з Jackson у форматі snake_case).
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        log.info("Store API base URL: {}", baseUrl);
    }

    @Override
    public boolean saveData(List<ProcessedAgentData> processedAgentDataBatch) {
        try {
            restClient.post()
                    .uri("/processed_agent_data/")
                    .body(processedAgentDataBatch)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Saved {} records to Store", processedAgentDataBatch.size());
            return true;
        } catch (Exception e) {
            log.error("Failed to save data to Store: {}", e.getMessage());
            return false;
        }
    }
}
