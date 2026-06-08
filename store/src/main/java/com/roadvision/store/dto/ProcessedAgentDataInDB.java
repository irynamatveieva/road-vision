package com.roadvision.store.dto;

import java.time.Instant;

/**
 * Проаналізовані дані у вигляді запису БД (вихідна модель API) —
 * «плоска» структура, що відповідає рядку таблиці processed_agent_data.
 */
public record ProcessedAgentDataInDB(
        Long id,
        String roadState,
        int userId,
        double x,
        double y,
        double z,
        double latitude,
        double longitude,
        Instant timestamp) {
}
