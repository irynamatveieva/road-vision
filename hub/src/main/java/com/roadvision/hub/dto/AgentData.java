package com.roadvision.hub.dto;

import java.time.Instant;

/**
 * Дані від агента (аналог AgentData у методичці).
 * timestamp у форматі ISO-8601; userId серіалізується як "user_id".
 */
public record AgentData(
        int userId,
        AccelerometerData accelerometer,
        GpsData gps,
        Weather weather,
        StreetLight streetLight,
        Instant timestamp) {
}
