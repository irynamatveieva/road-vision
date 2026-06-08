package com.roadvision.edge.gateway;

/**
 * Інтерфейс шлюзу до Agent (аналог AgentGateway у методичці).
 * Усі адаптери прийому даних від агента мають реалізувати ці методи.
 */
public interface AgentGateway {

    /** Встановити з'єднання з джерелом даних агента. */
    void connect();

    /** Почати слухати повідомлення від агента. */
    void start();

    /** Зупинити шлюз та звільнити ресурси. */
    void stop();
}
