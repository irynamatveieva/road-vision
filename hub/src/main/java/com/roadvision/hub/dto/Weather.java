package com.roadvision.hub.dto;

/** Дані погодного датчика: температура (°C), вологість (%), опади (мм). */
public record Weather(double temperature, double humidity, double precipitation) {
}
