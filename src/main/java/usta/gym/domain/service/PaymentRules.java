package usta.gym.domain.service;

import usta.gym.domain.exception.BusinessRuleException;
import usta.gym.domain.model.Enrollment;
import usta.gym.domain.model.EnrollmentStatus;
import usta.gym.domain.model.Payment;
import usta.gym.domain.model.PaymentMethod;
import usta.gym.domain.model.Plan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

// Reglas de negocio de los pagos y del saldo de cada inscripción
public class PaymentRules {

    // El primer abono debe cubrir al menos el 30% del valor del plan
    public static final BigDecimal MIN_FIRST_PAYMENT_RATE = new BigDecimal("0.30");

    // Saldo pendiente = precio del plan - total pagado
    public BigDecimal balance(Plan plan, BigDecimal paid) {
        return plan.getPrice().subtract(paid);
    }

    // Valida y crea el pago sobre una inscripción
    public Payment register(Enrollment enrollment, BigDecimal paidSoFar, BigDecimal amount,
                            PaymentMethod method, LocalDateTime now) {
        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            throw new BusinessRuleException("No se pueden registrar pagos en una inscripción cancelada");
        }
        if (enrollment.getStatus() == EnrollmentStatus.EXPIRED) {
            throw new BusinessRuleException("No se pueden registrar pagos en una inscripción vencida");
        }
        if (method == null) {
            throw new BusinessRuleException("El medio de pago es obligatorio");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessRuleException("El valor del pago debe ser mayor a 0");
        }
        BigDecimal balance = balance(enrollment.getPlan(), paidSoFar);
        if (balance.signum() <= 0) {
            throw new BusinessRuleException("La inscripción ya está pagada por completo");
        }
        if (amount.compareTo(balance) > 0) {
            throw new BusinessRuleException("El pago (" + MoneyFormat.format(amount)
                    + ") supera el saldo pendiente (" + MoneyFormat.format(balance) + ")");
        }
        if (paidSoFar.signum() == 0) {
            BigDecimal minimum = enrollment.getPlan().getPrice()
                    .multiply(MIN_FIRST_PAYMENT_RATE).setScale(0, RoundingMode.CEILING);
            if (amount.compareTo(minimum) < 0) {
                throw new BusinessRuleException("El primer pago debe ser al menos el 30% del plan ("
                        + MoneyFormat.format(minimum) + ")");
            }
        }
        Payment payment = new Payment();
        payment.setEnrollment(enrollment);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setPaymentDate(now);
        return payment;
    }

    // Después de un pago: si ya no hay saldo la inscripción queda ACTIVE; si no, sigue PENDING
    public void updateStatusAfterPayment(Enrollment enrollment, BigDecimal paidTotal) {
        boolean fullyPaid = balance(enrollment.getPlan(), paidTotal).signum() <= 0;
        enrollment.setStatus(fullyPaid ? EnrollmentStatus.ACTIVE : EnrollmentStatus.PENDING);
    }
}
