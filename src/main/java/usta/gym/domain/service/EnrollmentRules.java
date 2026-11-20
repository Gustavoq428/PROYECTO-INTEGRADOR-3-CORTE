package usta.gym.domain.service;

import usta.gym.domain.exception.BusinessRuleException;
import usta.gym.domain.model.Enrollment;
import usta.gym.domain.model.EnrollmentStatus;
import usta.gym.domain.model.Plan;

import java.time.LocalDate;
import java.util.List;

// Reglas de negocio de las inscripciones: crear, cancelar y vencer
public class EnrollmentRules {

    // Crea una inscripción PENDING (sin pagos aún). La vigencia es fecha de inicio + duración del plan.
    public Enrollment create(Plan plan, String memberName, String memberDocument, String memberPhone,
                             LocalDate startDate, List<Enrollment> previousOfMember, LocalDate today) {
        if (memberName == null || memberName.isBlank()) {
            throw new BusinessRuleException("El nombre del socio es obligatorio");
        }
        if (memberDocument == null || !memberDocument.trim().matches("\\d{5,12}")) {
            throw new BusinessRuleException("El documento debe tener entre 5 y 12 dígitos");
        }
        if (memberPhone != null && !memberPhone.isBlank() && !memberPhone.trim().matches("\\d{7,10}")) {
            throw new BusinessRuleException("El teléfono debe tener entre 7 y 10 dígitos");
        }
        if (startDate == null) {
            throw new BusinessRuleException("La fecha de inicio es obligatoria");
        }
        if (startDate.isBefore(today)) {
            throw new BusinessRuleException("La fecha de inicio no puede ser anterior a hoy");
        }
        if (!plan.isActive()) {
            throw new BusinessRuleException("El plan \"" + plan.getName() + "\" no está disponible");
        }
        // Un socio no puede tener dos inscripciones vigentes al tiempo
        for (Enrollment previous : previousOfMember) {
            boolean current = (previous.getStatus() == EnrollmentStatus.PENDING
                    || previous.getStatus() == EnrollmentStatus.ACTIVE)
                    && !previous.getEndDate().isBefore(today);
            if (current) {
                throw new BusinessRuleException("El socio ya tiene una inscripción vigente (#"
                        + previous.getId() + " hasta " + previous.getEndDate() + ")");
            }
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setMemberName(memberName.trim());
        enrollment.setMemberDocument(memberDocument.trim());
        enrollment.setMemberPhone(memberPhone == null ? "" : memberPhone.trim());
        enrollment.setPlan(plan);
        enrollment.setStartDate(startDate);
        enrollment.setEndDate(startDate.plusDays(plan.getDurationDays()));
        enrollment.setStatus(EnrollmentStatus.PENDING);
        return enrollment;
    }

    // Cancela una inscripción: solo se pueden cancelar las pendientes o activas
    public void cancel(Enrollment enrollment) {
        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            throw new BusinessRuleException("La inscripción ya está cancelada");
        }
        if (enrollment.getStatus() == EnrollmentStatus.EXPIRED) {
            throw new BusinessRuleException("No se puede cancelar una inscripción vencida");
        }
        enrollment.setStatus(EnrollmentStatus.CANCELLED);
    }

    // Marca como vencida la inscripción pendiente o activa cuya fecha de fin ya pasó.
    // Devuelve true si cambió el estado (para que el caso de uso la guarde).
    public boolean expireIfOverdue(Enrollment enrollment, LocalDate today) {
        boolean open = enrollment.getStatus() == EnrollmentStatus.PENDING
                || enrollment.getStatus() == EnrollmentStatus.ACTIVE;
        if (open && enrollment.getEndDate().isBefore(today)) {
            enrollment.setStatus(EnrollmentStatus.EXPIRED);
            return true;
        }
        return false;
    }
}
