package usta.gym.domain.model;

import java.math.BigDecimal;

// Estado de cuenta de una inscripción: cuánto se ha pagado y cuánto falta
public record EnrollmentAccount(Enrollment enrollment, BigDecimal paid, BigDecimal balance) {
}
