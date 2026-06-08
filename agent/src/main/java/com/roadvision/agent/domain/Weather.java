package com.roadvision.agent.domain;

/**
 * Модель даних погодного датчика (новий сенсорний об'єкт).
 * temperature — температура повітря (°C), humidity — вологість (%),
 * precipitation — опади (мм).
 */
public record Weather(double temperature, double humidity, double precipitation) {
}
