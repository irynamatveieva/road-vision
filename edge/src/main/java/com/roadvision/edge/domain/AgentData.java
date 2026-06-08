package com.roadvision.edge.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/** Дані від агента (отримуються з топіка agent_data_topic). */
public record AgentData(
        int userId,
        AccelerometerData accelerometer,
        GpsData gps,
        Weather weather,
        @JsonProperty("street_light") StreetLight streetLight,
        Instant timestamp) {
}
