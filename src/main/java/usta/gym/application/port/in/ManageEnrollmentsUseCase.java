package usta.gym.application.port.in;

import usta.gym.domain.model.Enrollment;
import usta.gym.domain.model.EnrollmentAccount;

import java.time.LocalDate;
import java.util.List;

// Puerto de entrada: inscribir socios, consultarlos y cancelar inscripciones
public interface ManageEnrollmentsUseCase {

    Enrollment enroll(Long planId, String memberName, String memberDocument, String memberPhone, LocalDate startDate);

    // Todas las inscripciones con lo pagado y el saldo (actualiza las que ya vencieron)
    List<EnrollmentAccount> listAccounts();

    EnrollmentAccount findAccount(Long enrollmentId);

    Enrollment cancel(Long enrollmentId);
}
