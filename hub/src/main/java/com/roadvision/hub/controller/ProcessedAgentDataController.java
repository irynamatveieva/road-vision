package com.roadvision.hub.controller;

import com.roadvision.hub.dto.ProcessedAgentData;
import com.roadvision.hub.service.HubService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * HTTP-ендпоінт прийому проаналізованих даних (альтернатива MQTT).
 * Hub може отримувати дані з MQTT або через HTTP.
 */
@RestController
public class ProcessedAgentDataController {

    private final HubService hubService;

    public ProcessedAgentDataController(HubService hubService) {
        this.hubService = hubService;
    }

    @PostMapping("/processed_agent_data/")
    public Map<String, String> saveProcessedAgentData(@RequestBody ProcessedAgentData data) {
        hubService.process(data);
        return Map.of("status", "ok");
    }
}
