package com.roadvision.agent.domain;

/**
 * Модель даних акселерометра.
 * Аналог dataclass Accelerometer з методички (поля x, y, z).
 */
public record Accelerometer(int x, int y, int z) {
}
