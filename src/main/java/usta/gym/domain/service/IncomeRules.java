package usta.gym.domain.service;

import usta.gym.domain.exception.BusinessRuleException;
import usta.gym.domain.model.IncomeReport;
import usta.gym.domain.model.Payment;
import usta.gym.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// Reglas del reporte de ingresos por rango de fechas
public class IncomeRules {

    // Valida el rango de fechas
    public void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BusinessRuleException("Debes indicar la fecha inicial y la final");
        }
        if (from.isAfter(to)) {
            throw new BusinessRuleException("La fecha inicial no puede ser posterior a la final");
        }
    }

    // Suma los pagos y los agrupa por medio de pago
    public IncomeReport summarize(LocalDate from, LocalDate to, List<Payment> payments) {
        Map<PaymentMethod, BigDecimal> byMethod = new EnumMap<>(PaymentMethod.class);
        for (PaymentMethod method : PaymentMethod.values()) {
            byMethod.put(method, BigDecimal.ZERO);
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Payment payment : payments) {
            total = total.add(payment.getAmount());
            byMethod.merge(payment.getMethod(), payment.getAmount(), BigDecimal::add);
        }
        return new IncomeReport(from, to, payments, total, byMethod);
    }
}
