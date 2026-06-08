package com.roadvision.edge.usecase;

import com.roadvision.edge.domain.AccelerometerData;
import com.roadvision.edge.domain.AgentData;
import com.roadvision.edge.domain.ProcessedAgentData;

/**
 * Первинна обробка даних: аналіз показників акселерометра та класифікація
 * стану дорожнього покриття (аналог process_agent_data у методичці).
 */
public final class DataProcessing {

    // У стані спокою вісь Z акселерометра показує ~16500 (прискорення вільного
    // падіння 1g у "сирих" одиницях). Відхилення вказують на нерівності дороги.
    private static final double Z_BASELINE = 16500.0;

    private DataProcessing() {
    }

    /**
     * Аналізує дані агента та класифікує стан дороги.
     *
     * @param agentData дані з акселерометра, GPS та позначкою часу
     * @return проаналізовані дані з визначеним станом дороги
     */
    public static ProcessedAgentData processAgentData(AgentData agentData) {
        AccelerometerData acc = agentData.accelerometer();
        double dz = Math.abs(acc.z() - Z_BASELINE);   // відхилення по вертикалі
        double ay = Math.abs(acc.y());                // поштовх по осі Y

        String roadState;
        if (dz > 5000 || ay > 8000) {
            roadState = "pothole";       // глибока яма / сильний поштовх
        } else if (dz > 2000 || ay > 3000) {
            roadState = "bump";          // вибоїна / нерівність
        } else {
            roadState = "smooth";        // рівна дорога
        }

        return new ProcessedAgentData(roadState, agentData);
    }
}
