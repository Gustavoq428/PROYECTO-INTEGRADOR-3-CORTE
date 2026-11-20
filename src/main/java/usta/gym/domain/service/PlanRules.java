package usta.gym.domain.service;

import usta.gym.domain.exception.BusinessRuleException;
import usta.gym.domain.model.Plan;

import java.math.BigDecimal;
import java.util.List;

// Reglas de negocio de los planes de membresía
public class PlanRules {

    public static final int MAX_DURATION_DAYS = 730;

    // Crea un plan nuevo (activo) validando sus datos y que el nombre no se repita
    public Plan create(String name, String description, BigDecimal price, int durationDays, List<Plan> existing) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("El nombre del plan es obligatorio");
        }
        if (price == null || price.signum() <= 0) {
            throw new BusinessRuleException("El precio del plan debe ser mayor a 0");
        }
        if (durationDays < 1 || durationDays > MAX_DURATION_DAYS) {
            throw new BusinessRuleException("La duración debe estar entre 1 y " + MAX_DURATION_DAYS + " días");
        }
        String cleanName = name.trim();
        boolean repeated = existing.stream().anyMatch(p -> p.getName().equalsIgnoreCase(cleanName));
        if (repeated) {
            throw new BusinessRuleException("Ya existe un plan llamado \"" + cleanName + "\"");
        }
        return new Plan(null, cleanName, description == null ? "" : description.trim(), price, durationDays, true);
    }
}
