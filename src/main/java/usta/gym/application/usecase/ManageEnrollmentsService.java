package usta.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import usta.gym.application.exception.ResourceNotFoundException;
import usta.gym.application.port.in.ManageEnrollmentsUseCase;
import usta.gym.application.port.out.EnrollmentRepository;
import usta.gym.application.port.out.PaymentRepository;
import usta.gym.application.port.out.PlanRepository;
import usta.gym.domain.model.Enrollment;
import usta.gym.domain.model.EnrollmentAccount;
import usta.gym.domain.model.Payment;
import usta.gym.domain.service.EnrollmentRules;
import usta.gym.domain.service.PaymentRules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Caso de uso: inscribe socios, arma el estado de cuenta y cancela inscripciones
@ApplicationScoped
public class ManageEnrollmentsService implements ManageEnrollmentsUseCase {

    private final EnrollmentRepository enrollmentRepository;
    private final PlanRepository planRepository;
    private final PaymentRepository paymentRepository;
    private final EnrollmentRules enrollmentRules = new EnrollmentRules();
    private final PaymentRules paymentRules = new PaymentRules();

    @Inject
    public ManageEnrollmentsService(EnrollmentRepository enrollmentRepository,
                                    PlanRepository planRepository,
                                    PaymentRepository paymentRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.planRepository = planRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Enrollment enroll(Long planId, String memberName, String memberDocument,
                             String memberPhone, LocalDate startDate) {
        if (planId == null) {
            throw new usta.gym.domain.exception.BusinessRuleException("Debes elegir un plan");
        }
        var plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el plan con id " + planId));
        String document = memberDocument == null ? "" : memberDocument.trim();
        List<Enrollment> previous = enrollmentRepository.findByMemberDocument(document);
        Enrollment enrollment = enrollmentRules.create(
                plan, memberName, memberDocument, memberPhone, startDate, previous, LocalDate.now());
        return enrollmentRepository.save(enrollment);
    }

    @Override
    public List<EnrollmentAccount> listAccounts() {
        Map<Long, BigDecimal> paidByEnrollment = new HashMap<>();
        for (Payment payment : paymentRepository.findAll()) {
            paidByEnrollment.merge(payment.getEnrollment().getId(), payment.getAmount(), BigDecimal::add);
        }
        LocalDate today = LocalDate.now();
        return enrollmentRepository.findAll().stream()
                .map(enrollment -> {
                    refreshExpiration(enrollment, today);
                    BigDecimal paid = paidByEnrollment.getOrDefault(enrollment.getId(), BigDecimal.ZERO);
                    return new EnrollmentAccount(enrollment, paid, paymentRules.balance(enrollment.getPlan(), paid));
                })
                .sorted(Comparator.comparing((EnrollmentAccount a) -> a.enrollment().getId()).reversed())
                .toList();
    }

    @Override
    public EnrollmentAccount findAccount(Long enrollmentId) {
        Enrollment enrollment = find(enrollmentId);
        refreshExpiration(enrollment, LocalDate.now());
        BigDecimal paid = paymentRepository.findByEnrollmentId(enrollmentId).stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new EnrollmentAccount(enrollment, paid, paymentRules.balance(enrollment.getPlan(), paid));
    }

    @Override
    public Enrollment cancel(Long enrollmentId) {
        Enrollment enrollment = find(enrollmentId);
        refreshExpiration(enrollment, LocalDate.now());
        enrollmentRules.cancel(enrollment);
        return enrollmentRepository.save(enrollment);
    }

    private Enrollment find(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la inscripción con id " + id));
    }

    // Si la vigencia ya terminó, la inscripción pasa a EXPIRED y se guarda
    private void refreshExpiration(Enrollment enrollment, LocalDate today) {
        if (enrollmentRules.expireIfOverdue(enrollment, today)) {
            enrollmentRepository.save(enrollment);
        }
    }
}
