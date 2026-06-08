package com.roadvision.store.controller;

import com.roadvision.store.dto.ProcessedAgentData;
import com.roadvision.store.dto.ProcessedAgentDataInDB;
import com.roadvision.store.entity.ProcessedAgentDataEntity;
import com.roadvision.store.repository.ProcessedAgentDataRepository;
import com.roadvision.store.websocket.SubscriptionWebSocketHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * CRUDL-ендпоінти для роботи з проаналізованими даними (ProcessedAgentData).
 */
@RestController
@RequestMapping("/processed_agent_data")
public class ProcessedAgentDataController {

    private final ProcessedAgentDataRepository repository;
    private final SubscriptionWebSocketHandler webSocketHandler;

    public ProcessedAgentDataController(ProcessedAgentDataRepository repository,
                                        SubscriptionWebSocketHandler webSocketHandler) {
        this.repository = repository;
        this.webSocketHandler = webSocketHandler;
    }

    // ---- Перетворення між DTO та сутністю ----

    private ProcessedAgentDataEntity toEntity(ProcessedAgentData data) {
        ProcessedAgentDataEntity entity = new ProcessedAgentDataEntity();
        entity.setRoadState(data.roadState());
        entity.setUserId(data.agentData().userId());
        entity.setX(data.agentData().accelerometer().x());
        entity.setY(data.agentData().accelerometer().y());
        entity.setZ(data.agentData().accelerometer().z());
        entity.setLatitude(data.agentData().gps().latitude());
        entity.setLongitude(data.agentData().gps().longitude());
        entity.setTimestamp(data.agentData().timestamp());
        return entity;
    }

    private ProcessedAgentDataInDB toDto(ProcessedAgentDataEntity e) {
        return new ProcessedAgentDataInDB(
                e.getId(), e.getRoadState(), e.getUserId(),
                e.getX(), e.getY(), e.getZ(),
                e.getLatitude(), e.getLongitude(), e.getTimestamp());
    }

    private ProcessedAgentDataEntity findOr404(int id) {
        return repository.findById((long) id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ProcessedAgentData with id " + id + " not found"));
    }

    // ---- CREATE ----
    @PostMapping("/")
    public List<ProcessedAgentDataInDB> create(@RequestBody List<ProcessedAgentData> data) {
        List<ProcessedAgentDataEntity> saved = repository.saveAll(data.stream().map(this::toEntity).toList());
        List<ProcessedAgentDataInDB> result = saved.stream().map(this::toDto).toList();
        // Розсилаємо нові дані підписникам через WebSocket
        for (ProcessedAgentDataInDB item : result) {
            webSocketHandler.sendDataToSubscribers(item.userId(), item);
        }
        return result;
    }

    // ---- READ (за id) ----
    @GetMapping("/{id}")
    public ProcessedAgentDataInDB read(@PathVariable int id) {
        return toDto(findOr404(id));
    }

    // ---- LIST ----
    @GetMapping("/")
    public List<ProcessedAgentDataInDB> list() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    // ---- UPDATE ----
    @PutMapping("/{id}")
    public ProcessedAgentDataInDB update(@PathVariable int id, @RequestBody ProcessedAgentData data) {
        ProcessedAgentDataEntity entity = findOr404(id);
        entity.setRoadState(data.roadState());
        entity.setUserId(data.agentData().userId());
        entity.setX(data.agentData().accelerometer().x());
        entity.setY(data.agentData().accelerometer().y());
        entity.setZ(data.agentData().accelerometer().z());
        entity.setLatitude(data.agentData().gps().latitude());
        entity.setLongitude(data.agentData().gps().longitude());
        entity.setTimestamp(data.agentData().timestamp());
        return toDto(repository.save(entity));
    }

    // ---- DELETE ----
    @DeleteMapping("/{id}")
    public ProcessedAgentDataInDB delete(@PathVariable int id) {
        ProcessedAgentDataEntity entity = findOr404(id);
        ProcessedAgentDataInDB dto = toDto(entity);
        repository.delete(entity);
        return dto;
    }
}
