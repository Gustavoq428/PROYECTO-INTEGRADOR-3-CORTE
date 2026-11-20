package usta.gym.application.port.in;

import usta.gym.domain.model.Plan;

import java.math.BigDecimal;
import java.util.List;

// Puerto de entrada: administrar el catálogo de planes
public interface ManagePlansUseCase {

    Plan create(String name, String description, BigDecimal price, int durationDays);

    List<Plan> findAll();

    // Solo los planes disponibles para nuevas inscripciones
    List<Plan> findActive();

    Plan findById(Long id);

    // Activa o desactiva un plan (los desactivados no se pueden elegir al inscribir)
    Plan toggleActive(Long id);
}
