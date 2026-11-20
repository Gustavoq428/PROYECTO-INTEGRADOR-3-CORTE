package usta.gym.infrastructure.adapter.out.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import usta.gym.infrastructure.adapter.out.persistence.entity.PaymentEntity;

// Repositorio Panache de la tabla de Payment
@ApplicationScoped
public class PaymentJpaRepository implements PanacheRepository<PaymentEntity> {
}
