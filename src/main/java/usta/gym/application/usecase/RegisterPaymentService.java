package usta.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import usta.gym.application.exception.ResourceNotFoundException;
import usta.gym.application.port.in.RegisterPaymentUseCase;
import usta.gym.application.port.out.EnrollmentRepository;
import usta.gym.application.port.out.PaymentRepository;
import usta.gym.domain.model.Enrollment;
import usta.gym.domain.model.Payment;
import usta.gym.domain.model.PaymentMethod;
import usta.gym.domain.model.PaymentReceipt;
import usta.gym.domain.service.EnrollmentRules;
import usta.gym.domain.service.PaymentRules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Caso de uso: registra pagos sobre una inscripción y arma los comprobantes
@ApplicationScoped
public class RegisterPaymentService implements RegisterPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentRules paymentRules = new PaymentRules();
    private final EnrollmentRules enrollmentRules = new EnrollmentRules();

    @Inject
    public RegisterPaymentService(PaymentRepository paymentRepository, EnrollmentRepository enrollmentRepository) {
        this.paymentRepository = paymentRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public Payment register(Long enrollmentId, BigDecimal amount, PaymentMethod method) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la inscripción con id " + enrollmentId));

        // Una inscripción vencida ya no recibe pagos: primero se actualiza su estado
        if (enrollmentRules.expireIfOverdue(enrollment, LocalDate.now())) {
            enrollmentRepository.save(enrollment);
        }

        BigDecimal paidSoFar = sum(paymentRepository.findByEnrollmentId(enrollmentId));
        Payment payment = paymentRules.register(enrollment, paidSoFar, amount, method, LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        // Si con este pago ya no hay saldo, la inscripción pasa a ACTIVE
        paymentRules.updateStatusAfterPayment(enrollment, paidSoFar.add(saved.getAmount()));
        enrollmentRepository.save(enrollment);
        return saved;
    }

    @Override
    public List<Payment> listByEnrollment(Long enrollmentId) {
        return paymentRepository.findByEnrollmentId(enrollmentId);
    }

    @Override
    public PaymentReceipt findReceipt(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el pago con id " + paymentId));
        Enrollment enrollment = payment.getEnrollment();
        // Lo pagado hasta este pago (incluido): los pagos anteriores y este
        BigDecimal paidToDate = sum(paymentRepository.findByEnrollmentId(enrollment.getId()).stream()
                .filter(p -> p.getId() <= payment.getId())
                .toList());
        return new PaymentReceipt(payment, paidToDate, paymentRules.balance(enrollment.getPlan(), paidToDate));
    }

    private BigDecimal sum(List<Payment> payments) {
        return payments.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
