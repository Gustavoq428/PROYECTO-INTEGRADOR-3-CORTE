package usta.gym.infrastructure.adapter.out.persistence.adapter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import usta.gym.application.port.out.EnrollmentRepository;
import usta.gym.domain.model.Enrollment;
import usta.gym.infrastructure.adapter.out.persistence.entity.EnrollmentEntity;
import usta.gym.infrastructure.adapter.out.persistence.entity.PlanEntity;
import usta.gym.infrastructure.adapter.out.persistence.mapper.EnrollmentEntityMapper;
import usta.gym.infrastructure.adapter.out.persistence.repository.EnrollmentJpaRepository;
import usta.gym.infrastructure.adapter.out.persistence.repository.PlanJpaRepository;

import java.util.List;
import java.util.Optional;

// Adaptador JPA del puerto EnrollmentRepository: guarda/lee inscripciones en MySQL
@ApplicationScoped
public class JpaEnrollmentRepository implements EnrollmentRepository {

    @Inject
    EnrollmentJpaRepository repository;

    @Inject
    PlanJpaRepository planRepository;

    // Guarda la inscripción (inserta o actualiza) resolviendo el plan por su id
    @Override
    @Transactional
    public Enrollment save(Enrollment enrollment) {
        PlanEntity planEntity = planRepository.findByIdOptional(enrollment.getPlan().getId())
                .orElseThrow(() -> new IllegalStateException("No existe el plan con id " + enrollment.getPlan().getId()));
        EnrollmentEntity saved = repository.getEntityManager()
                .merge(EnrollmentEntityMapper.toEntity(enrollment, planEntity));
        return EnrollmentEntityMapper.toDomain(saved);
    }

    @Override
    public List<Enrollment> findAll() {
        return repository.listAll().stream().map(EnrollmentEntityMapper::toDomain).toList();
    }

    @Override
    public Optional<Enrollment> findById(Long id) {
        return repository.findByIdOptional(id).map(EnrollmentEntityMapper::toDomain);
    }

    @Override
    public List<Enrollment> findByMemberDocument(String document) {
        return repository.list("memberDocument = ?1", document).stream()
                .map(EnrollmentEntityMapper::toDomain).toList();
    }
}
