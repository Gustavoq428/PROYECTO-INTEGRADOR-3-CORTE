package usta.gym.infrastructure.adapter.out.persistence.adapter;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import usta.gym.application.port.out.PaymentRepository;
import usta.gym.domain.model.Payment;
import usta.gym.infrastructure.adapter.out.persistence.entity.EnrollmentEntity;
import usta.gym.infrastructure.adapter.out.persistence.entity.PaymentEntity;
import usta.gym.infrastructure.adapter.out.persistence.mapper.PaymentEntityMapper;
import usta.gym.infrastructure.adapter.out.persistence.repository.EnrollmentJpaRepository;
import usta.gym.infrastructure.adapter.out.persistence.repository.PaymentJpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Adaptador JPA del puerto PaymentRepository: guarda/lee pagos en MySQL
@ApplicationScoped
public class JpaPaymentRepository implements PaymentRepository {

    @Inject
    PaymentJpaRepository repository;

    @Inject
    EnrollmentJpaRepository enrollmentRepository;

    // Guarda el pago resolviendo la inscripción por su id
    @Override
    @Transactional
    public Payment save(Payment payment) {
        EnrollmentEntity enrollmentEntity = enrollmentRepository.findByIdOptional(payment.getEnrollment().getId())
                .orElseThrow(() -> new IllegalStateException(
                        "No existe la inscripción con id " + payment.getEnrollment().getId()));
        PaymentEntity saved = repository.getEntityManager()
                .merge(PaymentEntityMapper.toEntity(payment, enrollmentEntity));
        return PaymentEntityMapper.toDomain(saved);
    }

    @Override
    public List<Payment> findAll() {
        return repository.listAll(Sort.by("paymentDate")).stream().map(PaymentEntityMapper::toDomain).toList();
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return repository.findByIdOptional(id).map(PaymentEntityMapper::toDomain);
    }

    @Override
    public List<Payment> findByEnrollmentId(Long enrollmentId) {
        return repository.list("enrollment.id = ?1", Sort.by("paymentDate").and("id"), enrollmentId).stream()
                .map(PaymentEntityMapper::toDomain).toList();
    }

    @Override
    public List<Payment> findBetween(LocalDateTime from, LocalDateTime to) {
        return repository.list("paymentDate >= ?1 and paymentDate < ?2", Sort.by("paymentDate").and("id"), from, to)
                .stream().map(PaymentEntityMapper::toDomain).toList();
    }
}
