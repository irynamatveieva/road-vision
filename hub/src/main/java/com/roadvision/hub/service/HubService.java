package com.roadvision.hub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadvision.hub.dto.ProcessedAgentData;
import com.roadvision.hub.gateway.StoreGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Логіка Hub: накопичення отриманих даних у Redis та пакетне збереження їх
 * у Store після досягнення BATCH_SIZE записів (так дані простіше аналізувати,
 * а запис множини даних у БД — швидший).
 */
@Service
public class HubService {

    private static final Logger log = LoggerFactory.getLogger(HubService.class);
    private static final String REDIS_KEY = "processed_agent_data";

    private final StringRedisTemplate redis;
    private final StoreGateway storeGateway;
    private final ObjectMapper objectMapper;
    private final int batchSize;

    public HubService(StringRedisTemplate redis,
                      StoreGateway storeGateway,
                      ObjectMapper objectMapper,
                      @Value("${hub.batch-size:10}") int batchSize) {
        this.redis = redis;
        this.storeGateway = storeGateway;
        this.objectMapper = objectMapper;
        this.batchSize = batchSize;
    }

    /**
     * Прийняти один запис: покласти в Redis-буфер і, якщо накопичилось
     * достатньо записів, зберегти пакет у Store.
     */
    public synchronized void process(ProcessedAgentData data) {
        try {
            redis.opsForList().leftPush(REDIS_KEY, objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            log.error("Failed to buffer data in Redis: {}", e.getMessage());
            return;
        }

        Long size = redis.opsForList().size(REDIS_KEY);
        if (size != null && size >= batchSize) {
            flush();
        }
    }

    /** Витягнути BATCH_SIZE записів з Redis та зберегти їх у Store. */
    private void flush() {
        List<ProcessedAgentData> batch = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            String json = redis.opsForList().rightPop(REDIS_KEY);
            if (json == null) {
                break;
            }
            try {
                batch.add(objectMapper.readValue(json, ProcessedAgentData.class));
            } catch (Exception e) {
                log.error("Failed to parse buffered record: {}", e.getMessage());
            }
        }
        if (!batch.isEmpty()) {
            log.info("Flushing batch of {} records to Store", batch.size());
            storeGateway.saveData(batch);
        }
    }
}
