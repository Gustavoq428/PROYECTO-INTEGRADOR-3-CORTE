package usta.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import usta.gym.application.port.in.IncomeReportUseCase;
import usta.gym.application.port.out.PaymentRepository;
import usta.gym.domain.model.IncomeReport;
import usta.gym.domain.service.IncomeRules;

import java.time.LocalDate;

// Caso de uso: ingresos del gimnasio entre dos fechas (ambas incluidas)
@ApplicationScoped
public class IncomeReportService implements IncomeReportUseCase {

    private final PaymentRepository paymentRepository;
    private final IncomeRules rules = new IncomeRules();

    @Inject
    public IncomeReportService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public IncomeReport report(LocalDate from, LocalDate to) {
        rules.validateRange(from, to);
        var payments = paymentRepository.findBetween(from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        return rules.summarize(from, to, payments);
    }
}
