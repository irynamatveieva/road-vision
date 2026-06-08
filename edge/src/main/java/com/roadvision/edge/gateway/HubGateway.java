package com.roadvision.edge.gateway;

import com.roadvision.edge.domain.ProcessedAgentData;

/**
 * Інтерфейс шлюзу до Hub (аналог HubGateway у методичці).
 * Усі адаптери відправки даних на Hub мають реалізувати цей метод.
 */
public interface HubGateway {

    /**
     * Надіслати проаналізовані дані на Hub.
     *
     * @param processedData проаналізовані дані для збереження
     * @return true, якщо дані успішно надіслано, інакше false
     */
    boolean saveData(ProcessedAgentData processedData);
}
