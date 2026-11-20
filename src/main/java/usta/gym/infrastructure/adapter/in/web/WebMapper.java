package usta.gym.infrastructure.adapter.in.web;

import usta.gym.domain.model.Enrollment;
import usta.gym.domain.model.EnrollmentAccount;
import usta.gym.domain.model.EnrollmentStatus;
import usta.gym.domain.model.Payment;
import usta.gym.domain.model.PaymentMethod;
import usta.gym.domain.model.Plan;
import usta.gym.domain.service.MoneyFormat;
import usta.gym.infrastructure.adapter.in.web.Views.EnrollmentView;
import usta.gym.infrastructure.adapter.in.web.Views.OptionView;
import usta.gym.infrastructure.adapter.in.web.Views.PaymentView;
import usta.gym.infrastructure.adapter.in.web.Views.PlanView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

// Convierte objetos del dominio a modelos para las vistas HTML
public final class WebMapper {

    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private WebMapper() {
    }

    public static String date(LocalDate value) {
        return value == null ? "" : DATE.format(value);
    }

    public static String dateTime(LocalDateTime value) {
        return value == null ? "" : DATE_TIME.format(value);
    }

    public static PlanView toView(Plan plan) {
        return new PlanView(plan.getId(), plan.getName(), plan.getDescription(),
                MoneyFormat.format(plan.getPrice()), plan.getDurationDays(), plan.isActive());
    }

    public static EnrollmentView toView(EnrollmentAccount account) {
        Enrollment e = account.enrollment();
        EnrollmentStatus status = e.getStatus();
        boolean open = status == EnrollmentStatus.PENDING || status == EnrollmentStatus.ACTIVE;
        boolean hasBalance = account.balance().signum() > 0;
        return new EnrollmentView(
                e.getId(), e.getMemberName(), e.getMemberDocument(), e.getMemberPhone(),
                e.getPlan().getName(), MoneyFormat.format(e.getPlan().getPrice()),
                date(e.getStartDate()), date(e.getEndDate()),
                status.name(), status.getLabel(), statusClass(status),
                MoneyFormat.format(account.paid()), MoneyFormat.format(account.balance()), hasBalance,
                open && hasBalance, open);
    }

    public static PaymentView toView(Payment payment) {
        return new PaymentView(payment.getId(), dateTime(payment.getPaymentDate()),
                MoneyFormat.format(payment.getAmount()), payment.getMethod().getLabel());
    }

    public static List<OptionView> paymentMethods() {
        return Arrays.stream(PaymentMethod.values())
                .map(m -> new OptionView(m.name(), m.getLabel())).toList();
    }

    // Clase de Bootstrap para el color de la etiqueta de estado
    public static String statusClass(EnrollmentStatus status) {
        return switch (status) {
            case ACTIVE -> "success";
            case PENDING -> "warning";
            case EXPIRED -> "secondary";
            case CANCELLED -> "danger";
        };
    }
}
