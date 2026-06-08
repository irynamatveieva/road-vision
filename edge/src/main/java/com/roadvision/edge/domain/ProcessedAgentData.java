package com.roadvision.edge.domain;

/** Проаналізовані дані: визначений стан дороги + вихідні дані агента. */
public record ProcessedAgentData(
        String roadState,
        AgentData agentData) {
}
