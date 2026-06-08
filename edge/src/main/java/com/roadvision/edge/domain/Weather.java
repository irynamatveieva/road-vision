package com.roadvision.edge.domain;

/** Дані погодного датчика: температура (°C), вологість (%), опади (мм). */
public record Weather(double temperature, double humidity, double precipitation) {
}
