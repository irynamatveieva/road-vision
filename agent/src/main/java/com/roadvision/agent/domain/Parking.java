package com.roadvision.agent.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель даних датчика паркінгу (додатковий датчик).
 * Аналог dataclass Parking з методички: кількість вільних місць + координати.
 * emptyCount серіалізується у поле "empty_count" (як у ParkingSchema).
 */
public record Parking(@JsonProperty("empty_count") int emptyCount, Gps gps) {
}
