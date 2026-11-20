package usta.gym.infrastructure.adapter.out.persistence.mapper;

import usta.gym.domain.model.Payment;
import usta.gym.infrastructure.adapter.out.persistence.entity.EnrollmentEntity;
import usta.gym.infrastructure.adapter.out.persistence.entity.PaymentEntity;

// Convierte entre el pago del dominio (Payment) y la entidad JPA (PaymentEntity)
public final class PaymentEntityMapper {

    private PaymentEntityMapper() {
    }

    // Dominio -> entidad. Recibe la entidad de la inscripción (relación ya resuelta).
    public static PaymentEntity toEntity(Payment payment, EnrollmentEntity enrollmentEntity) {
        if (payment == null) {
            return null;
        }
        PaymentEntity entity = new PaymentEntity();
        entity.setId(payment.getId());
        entity.setEnrollment(enrollmentEntity);
        entity.setAmount(payment.getAmount());
        entity.setPaymentDate(payment.getPaymentDate());
        entity.setMethod(payment.getMethod());
        return entity;
    }

    public static Payment toDomain(PaymentEntity entity) {
        if (entity == null) {
            return null;
        }
        Payment payment = new Payment();
        payment.setId(entity.getId());
        payment.setEnrollment(EnrollmentEntityMapper.toDomain(entity.getEnrollment()));
        payment.setAmount(entity.getAmount());
        payment.setPaymentDate(entity.getPaymentDate());
        payment.setMethod(entity.getMethod());
        return payment;
    }
}
