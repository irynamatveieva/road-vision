package com.roadvision.agent.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель даних датчика вуличного освітлення (новий сенсорний об'єкт).
 * lux — рівень освітленості (люкс), isOn — чи увімкнений ліхтар
 * (серіалізується у поле "is_on").
 */
public record StreetLight(double lux, @JsonProperty("is_on") boolean isOn) {
}
