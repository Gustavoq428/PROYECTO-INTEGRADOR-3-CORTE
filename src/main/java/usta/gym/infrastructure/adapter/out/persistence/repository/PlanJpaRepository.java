package usta.gym.infrastructure.adapter.out.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import usta.gym.infrastructure.adapter.out.persistence.entity.PlanEntity;

// Repositorio Panache de la tabla de Plan
@ApplicationScoped
public class PlanJpaRepository implements PanacheRepository<PlanEntity> {
}
