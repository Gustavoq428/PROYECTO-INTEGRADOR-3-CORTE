package usta.gym.infrastructure.adapter.out.persistence.adapter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import usta.gym.application.port.out.PlanRepository;
import usta.gym.domain.model.Plan;
import usta.gym.infrastructure.adapter.out.persistence.entity.PlanEntity;
import usta.gym.infrastructure.adapter.out.persistence.mapper.PlanEntityMapper;
import usta.gym.infrastructure.adapter.out.persistence.repository.PlanJpaRepository;

import java.util.List;
import java.util.Optional;

// Adaptador JPA del puerto PlanRepository: guarda/lee planes en MySQL
@ApplicationScoped
public class JpaPlanRepository implements PlanRepository {

    @Inject
    PlanJpaRepository repository;

    // Inserta o actualiza el plan
    @Override
    @Transactional
    public Plan save(Plan plan) {
        PlanEntity saved = repository.getEntityManager().merge(PlanEntityMapper.toEntity(plan));
        return PlanEntityMapper.toDomain(saved);
    }

    @Override
    public List<Plan> findAll() {
        return repository.listAll().stream().map(PlanEntityMapper::toDomain).toList();
    }

    @Override
    public Optional<Plan> findById(Long id) {
        return repository.findByIdOptional(id).map(PlanEntityMapper::toDomain);
    }
}
