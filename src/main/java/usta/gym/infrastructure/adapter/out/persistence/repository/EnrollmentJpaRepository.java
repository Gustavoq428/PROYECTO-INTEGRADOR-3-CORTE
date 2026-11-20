package usta.gym.infrastructure.adapter.out.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import usta.gym.infrastructure.adapter.out.persistence.entity.EnrollmentEntity;

// Repositorio Panache de la tabla de Enrollment
@ApplicationScoped
public class EnrollmentJpaRepository implements PanacheRepository<EnrollmentEntity> {
}
