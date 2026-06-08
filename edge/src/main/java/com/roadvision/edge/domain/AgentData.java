package com.roadvision.edge.domain;

import java.time.Instant;

/** Дані від агента (отримуються з топіка agent_data_topic). */
public record AgentData(
        int userId,
        AccelerometerData accelerometer,
        GpsData gps,
        Instant timestamp) {
}
