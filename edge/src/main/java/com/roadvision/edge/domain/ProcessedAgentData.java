package com.roadvision.edge.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Проаналізовані дані: визначені стани (дорога, погода, освітлення) +
 * вихідні дані агента.
 */
public record ProcessedAgentData(
        String roadState,
        @JsonProperty("weather_state") String weatherState,
        @JsonProperty("light_state") String lightState,
        AgentData agentData) {
}
