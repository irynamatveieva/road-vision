package com.roadvision.agent.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Агреговані дані, які агент відправляє на Edge через MQTT.
 * Аналог dataclass AggregatedData з методички.
 *
 * timestamp серіалізується у форматі ISO-8601, user_id — у поле "user_id"
 * (snake_case), щоб дані були сумісні з Edge/Hub частинами системи.
 */
public record AggregatedData(
        Accelerometer accelerometer,
        Gps gps,
        Weather weather,
        @JsonProperty("street_light") StreetLight streetLight,
        Instant timestamp,
        @JsonProperty("user_id") int userId) {
}
