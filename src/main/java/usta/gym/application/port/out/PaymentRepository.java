package usta.gym.application.port.out;

import usta.gym.domain.model.Payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Puerto de salida: guardar y consultar pagos
public interface PaymentRepository {

    Payment save(Payment payment);

    List<Payment> findAll();

    Optional<Payment> findById(Long id);

    List<Payment> findByEnrollmentId(Long enrollmentId);

    // Pagos con fecha entre "from" (incluida) y "to" (excluida)
    List<Payment> findBetween(LocalDateTime from, LocalDateTime to);
}
