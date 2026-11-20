package usta.gym.domain.model;

import java.math.BigDecimal;

// Comprobante de un pago: el pago y cómo quedó la cuenta después de hacerlo
public record PaymentReceipt(Payment payment, BigDecimal paidToDate, BigDecimal balance) {
}
