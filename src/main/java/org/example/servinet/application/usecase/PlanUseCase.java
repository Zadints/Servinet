package org.example.servinet.application.usecase;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.sql.ClientRepository;
import org.example.servinet.application.service.PlanService;
import org.example.servinet.domain.entities.client.ClientPlan;
import org.example.servinet.domain.exception.InvalidValueException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class PlanUseCase {

    private final ClientRepository sqlClientAuthRepository;
    private final InMemoryRepository<ClientPlan> inMemoryPlansRepository;
    private final PlanService planService;

    public PlanUseCase(ClientRepository sqlClientAuthRepository,
                       InMemoryRepository<ClientPlan> inMemoryPlansRepository, PlanService planService) {
        this.sqlClientAuthRepository = sqlClientAuthRepository;
        this.inMemoryPlansRepository = inMemoryPlansRepository;
        this.planService = planService;
    }

    public ObservableList<ClientPlan> getAll() {

        System.out.println("Cargando los planes disponibles...");
        List<ClientPlan> plans = inMemoryPlansRepository.getAll();

        if (plans != null && !plans.isEmpty()) {
            return FXCollections.observableArrayList(plans);
        }
        System.out.println("CaObteniendo los planes de sql");
        plans = sqlClientAuthRepository.getAllPlansDatabase();
        System.out.println("CaObteniendo los planes de ses nulo?ql");

        if (plans == null) {
            return null;
        }

        inMemoryPlansRepository.setAll(plans);

        return  FXCollections.observableArrayList(plans);
    }

    // Valida por objeto ClientPlan
    public boolean existPlans(ClientPlan plan) {
        if (plan == null || plan.getId() == null) {
            return false;
        }
        return existPlans(plan.getId());
    }



    public ClientPlan getPlan(String uuid){
        return planService.getPlanById(uuid);
    }

    // Sobrecarga: valida por ID de plan
    public boolean existPlans(String planId) {

        List<ClientPlan> plans = inMemoryPlansRepository.getAll();

        if (planId == null || planId.isBlank() || plans.isEmpty()) {
            return false;
        }
        return plans.stream()
                .anyMatch(p -> p.getId().equalsIgnoreCase(planId));
    }

    public void addPlan(ClientPlan plan) {

        if (plan == null) {
            throw new InvalidValueException("Debes ingresar datos válidos");
        }

        validatePlan(plan);

        if (existPlans(plan.getId())) {
            throw new InvalidValueException("El plan ingresado ya existe");
        }

        sqlClientAuthRepository.addPlan(plan);

        inMemoryPlansRepository.addEntity(plan);
    }
    private void validatePlan(ClientPlan plan) {

        if (plan.getId() == null || plan.getId().isBlank()) {
            throw new InvalidValueException("El identificador del plan es obligatorio");
        }

        try {
            UUID.fromString(plan.getId());
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException("El identificador del plan no es un UUID válido");
        }

        if (plan.getName() == null || plan.getName().isBlank()) {
            throw new InvalidValueException("El nombre del plan es obligatorio");
        }

        String name = plan.getName().trim().toUpperCase(Locale.ROOT);

        if (name.length() > 15 || !name.matches("[A-Z0-9_]+")) {
            throw new InvalidValueException(
                    "El nombre debe tener máximo 8 caracteres y solo contener letras, números o _"
            );
        }

        if (plan.getCreatedAt() == null) {
            throw new InvalidValueException("La fecha de creación es obligatoria");
        }

        if (plan.getSpeed() == null || plan.getSpeed() <= 0) {
            throw new InvalidValueException("La velocidad debe ser mayor que cero");
        }

        if (plan.getPrice() == null ||
                !Double.isFinite(plan.getPrice()) ||
                plan.getPrice() <= 0 ||
                plan.getPrice() > 99999999.99) {
            throw new InvalidValueException("El precio debe ser válido y mayor que cero");
        }

        BigDecimal price = BigDecimal.valueOf(plan.getPrice());

        if (price.stripTrailingZeros().scale() > 2) {
            throw new InvalidValueException("El precio solo puede tener dos decimales");
        }

        if (plan.getPromo() == null) {
            throw new InvalidValueException("Debes indicar si el plan es promocional");
        }

        if (plan.getActive() == null) {
            throw new InvalidValueException("Debes indicar el estado del plan");
        }

        plan.setName(name);
    }

    public void deletePlan(String planId) {

        if (planId == null || planId.isBlank()) {
            throw new InvalidValueException("El identificador del plan no es válido");
        }

        if (!existPlans(planId)) {
            throw new InvalidValueException("El plan no existe");
        }

        sqlClientAuthRepository.deletePlanDatabase(planId);

        inMemoryPlansRepository.getAll().stream()
                .filter(plan -> plan.getId().equalsIgnoreCase(planId))
                .findFirst()
                .ifPresent(plan -> plan.setActive(false));
    }

}
