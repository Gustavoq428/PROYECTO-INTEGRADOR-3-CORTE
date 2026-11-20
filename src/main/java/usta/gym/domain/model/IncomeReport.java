package usta.gym.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// Resumen de ingresos entre dos fechas: pagos, total y total por medio de pago
public record IncomeReport(LocalDate from, LocalDate to, List<Payment> payments,
                           BigDecimal total, Map<PaymentMethod, BigDecimal> totalByMethod) {
}
