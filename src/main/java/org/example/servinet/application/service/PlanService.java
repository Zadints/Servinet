package org.example.servinet.application.service;

import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.sql.ClientRepository;
import org.example.servinet.domain.entities.ClientPlan;

import java.util.List;

public class PlanService {
    private final InMemoryRepository<ClientPlan> inMemoryPlansRepository;
    private final ClientRepository sqlClientAuthRepository;

    public PlanService(InMemoryRepository<ClientPlan> inMemoryPlansRepository, ClientRepository sqlClientAuthRepository) {
        this.inMemoryPlansRepository = inMemoryPlansRepository;
        this.sqlClientAuthRepository = sqlClientAuthRepository;
    }

    public ClientPlan getPlanById(String planId) {

        List<ClientPlan> plans = inMemoryPlansRepository.getAll();
        if (plans.isEmpty()) {
            plans = sqlClientAuthRepository.getAllPlansDatabase();
        }

        if (plans.isEmpty()){
            return null;
        }

        for (ClientPlan p: plans){
            System.out.println("======================== BUCANDO...");
            System.out.println(p.toString());
        }

        return plans.stream()
                .filter(p -> p.getId().equalsIgnoreCase(planId))
                .findFirst()
                .orElse(null);
    }
}
