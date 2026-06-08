package com.roadvision.store.dto;

/**
 * Проаналізовані дані (вхідна модель API).
 * road_state — визначений стан дороги, agent_data — вихідні дані агента.
 */
public record ProcessedAgentData(
        String roadState,
        AgentData agentData) {
}
