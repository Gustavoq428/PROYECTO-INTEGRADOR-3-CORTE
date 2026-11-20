package usta.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import usta.gym.application.exception.ResourceNotFoundException;
import usta.gym.application.port.in.ManagePlansUseCase;
import usta.gym.application.port.out.PlanRepository;
import usta.gym.domain.model.Plan;
import usta.gym.domain.service.PlanRules;

import java.math.BigDecimal;
import java.util.List;

// Caso de uso: administra los planes (las reglas viven en PlanRules, del dominio)
@ApplicationScoped
public class ManagePlansService implements ManagePlansUseCase {

    private final PlanRepository planRepository;
    private final PlanRules rules = new PlanRules();

    @Inject
    public ManagePlansService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public Plan create(String name, String description, BigDecimal price, int durationDays) {
        Plan plan = rules.create(name, description, price, durationDays, planRepository.findAll());
        return planRepository.save(plan);
    }

    @Override
    public List<Plan> findAll() {
        return planRepository.findAll();
    }

    @Override
    public List<Plan> findActive() {
        return planRepository.findAll().stream().filter(Plan::isActive).toList();
    }

    @Override
    public Plan findById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el plan con id " + id));
    }

    @Override
    public Plan toggleActive(Long id) {
        Plan plan = findById(id);
        plan.setActive(!plan.isActive());
        return planRepository.save(plan);
    }
}
