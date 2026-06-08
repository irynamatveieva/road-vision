package com.roadvision.hub.gateway;

import com.roadvision.hub.dto.ProcessedAgentData;

import java.util.List;

/**
 * Інтерфейс шлюзу до Store (аналог StoreGateway у методичці).
 * Усі адаптери збереження мають реалізувати цей метод.
 */
public interface StoreGateway {

    /**
     * Зберегти пакет проаналізованих даних у сховищі.
     *
     * @param processedAgentDataBatch пакет даних для збереження
     * @return true, якщо дані успішно збережено, інакше false
     */
    boolean saveData(List<ProcessedAgentData> processedAgentDataBatch);
}
