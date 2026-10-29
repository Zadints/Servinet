package org.example.servinet.infrastructure.database.inmemory;

import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.domain.entities.ClientPlan;
import org.example.servinet.domain.entities.Role;

import java.util.ArrayList;
import java.util.List;

public class InMemoryPlanRepository implements InMemoryRepository<ClientPlan> {

    private List<ClientPlan> plan = new ArrayList<>();

    @Override
    public List<ClientPlan> getAll() {
        return plan;
    }
    @Override
    public void setAll(List<ClientPlan> listEntity) {
        plan = listEntity;
    }

    @Override
    public void addEntity(ClientPlan entity) {
        plan.add(entity);
    }

    @Override
    public void remove(String id) {
        plan.removeIf(s -> s.getId().equals(id));
    }

    @Override
    public void edit(ClientPlan entity) {
        for (int i = 0; i < plan.size(); i++) {
            if (plan.get(i).getId().equals(entity.getId())) {
                plan.set(i, entity);
                break;
            }
        }
    }

}
