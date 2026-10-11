package org.example.servinet.core.application.usecase;

import org.example.servinet.core.domain.entities.ClientPlan;

import java.util.ArrayList;
import java.util.List;

public class PlanUseCase {
    private static List<ClientPlan> plans = new ArrayList<>();

    public static boolean existPlans(ClientPlan plan ) {
        return plans.contains(plan);
    }
}
