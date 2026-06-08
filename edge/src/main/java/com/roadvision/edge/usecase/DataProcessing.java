package com.roadvision.edge.usecase;

import com.roadvision.edge.domain.AccelerometerData;
import com.roadvision.edge.domain.AgentData;
import com.roadvision.edge.domain.ProcessedAgentData;
import com.roadvision.edge.domain.StreetLight;
import com.roadvision.edge.domain.Weather;

/**
 * Первинна обробка даних: аналіз показників датчиків та класифікація станів
 * (дорожнього покриття, погодних умов дороги та рівня освітлення).
 */
public final class DataProcessing {

    // У стані спокою вісь Z акселерометра показує ~16500 (прискорення вільного
    // падіння 1g у "сирих" одиницях). Відхилення вказують на нерівності дороги.
    private static final double Z_BASELINE = 16500.0;

    private DataProcessing() {
    }

    /**
     * Аналізує дані агента та класифікує стани дороги, погоди й освітлення.
     */
    public static ProcessedAgentData processAgentData(AgentData agentData) {
        String roadState = classifyRoad(agentData.accelerometer());
        String weatherState = classifyWeather(agentData.weather());
        String lightState = classifyLight(agentData.streetLight());
        return new ProcessedAgentData(roadState, weatherState, lightState, agentData);
    }

    /** Класифікація стану дорожнього покриття за акселерометром. */
    private static String classifyRoad(AccelerometerData acc) {
        double dz = Math.abs(acc.z() - Z_BASELINE);   // відхилення по вертикалі
        double ay = Math.abs(acc.y());                // поштовх по осі Y
        if (dz > 5000 || ay > 8000) {
            return "pothole";   // глибока яма / сильний поштовх
        } else if (dz > 2000 || ay > 3000) {
            return "bump";      // вибоїна / нерівність
        }
        return "smooth";        // рівна дорога
    }

    /** Класифікація стану дорожнього покриття за погодою: dry / wet / icy. */
    private static String classifyWeather(Weather weather) {
        if (weather.precipitation() > 0) {
            return weather.temperature() <= 0 ? "icy" : "wet"; // ожеледиця за мінусової t
        }
        return "dry";
    }

    /** Класифікація рівня освітлення за освітленістю: dark / dim / bright. */
    private static String classifyLight(StreetLight light) {
        double lux = light.lux();
        if (lux < 10) {
            return "dark";
        } else if (lux < 1000) {
            return "dim";
        }
        return "bright";
    }
}
