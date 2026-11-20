package usta.gym.application.port.out;

import usta.gym.domain.model.Plan;

import java.util.List;
import java.util.Optional;

// Puerto de salida: guardar y consultar planes
public interface PlanRepository {

    Plan save(Plan plan);

    List<Plan> findAll();

    Optional<Plan> findById(Long id);
}
