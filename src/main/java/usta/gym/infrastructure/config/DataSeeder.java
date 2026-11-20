package usta.gym.infrastructure.config;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import usta.gym.application.port.out.EnrollmentRepository;
import usta.gym.application.port.out.PaymentRepository;
import usta.gym.application.port.out.PlanRepository;
import usta.gym.domain.model.Enrollment;
import usta.gym.domain.model.EnrollmentStatus;
import usta.gym.domain.model.Payment;
import usta.gym.domain.model.PaymentMethod;
import usta.gym.domain.model.Plan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

// Carga datos de prueba coherentes al arrancar, solo si la tabla de planes está vacía.
// Las fechas son relativas a "hoy" para que siempre haya inscripciones activas, pendientes y vencidas.
// Usa los puertos de salida (no toca JPA directamente).
@ApplicationScoped
public class DataSeeder {

    private static final Logger LOG = Logger.getLogger(DataSeeder.class);

    @ConfigProperty(name = "gym.seed.enabled", defaultValue = "true")
    boolean enabled;

    @Inject
    PlanRepository planRepository;

    @Inject
    EnrollmentRepository enrollmentRepository;

    @Inject
    PaymentRepository paymentRepository;

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (!enabled || !planRepository.findAll().isEmpty()) {
            return;
        }
        LOG.info("Cargando datos de prueba del gimnasio...");
        LocalDate today = LocalDate.now();

        Plan mensual = planRepository.save(new Plan(null, "Mensual", "Acceso libre a sala de máquinas y clases grupales",
                new BigDecimal("80000"), 30, true));
        Plan trimestral = planRepository.save(new Plan(null, "Trimestral", "3 meses con valoración física incluida",
                new BigDecimal("210000"), 90, true));
        Plan semestral = planRepository.save(new Plan(null, "Semestral", "6 meses con plan de entrenamiento personalizado",
                new BigDecimal("380000"), 180, true));
        Plan anual = planRepository.save(new Plan(null, "Anual", "12 meses, el plan más económico por mes",
                new BigDecimal("650000"), 365, true));
        planRepository.save(new Plan(null, "Promo Estudiante", "Promoción terminada (plan desactivado)",
                new BigDecimal("60000"), 30, false));

        // 1. Activa, pagó todo de una vez
        Enrollment laura = enroll("Laura Gómez", "1010123456", "3101234567", mensual, today.minusDays(10), EnrollmentStatus.ACTIVE);
        pay(laura, "80000", PaymentMethod.CARD, today.minusDays(10));

        // 2. Activa, plan trimestral pagado por transferencia
        Enrollment carlos = enroll("Carlos Pérez", "79876543", "3209876543", trimestral, today.minusDays(40), EnrollmentStatus.ACTIVE);
        pay(carlos, "210000", PaymentMethod.TRANSFER, today.minusDays(40));

        // 3. Vencida: terminó hace 15 días (puede volver a inscribirse)
        Enrollment andrea = enroll("Andrea Rojas", "1022334455", "3004455667", mensual, today.minusDays(45), EnrollmentStatus.EXPIRED);
        pay(andrea, "80000", PaymentMethod.CASH, today.minusDays(45));

        // 4. Pendiente: abonó parte del semestral y le falta el resto
        Enrollment julian = enroll("Julián Torres", "1098765432", "3157778899", semestral, today.minusDays(5), EnrollmentStatus.PENDING);
        pay(julian, "150000", PaymentMethod.CASH, today.minusDays(5));

        // 5. Pendiente: abonó la mitad del mensual
        Enrollment marcela = enroll("Marcela Díaz", "52123456", "3112223344", mensual, today.minusDays(3), EnrollmentStatus.PENDING);
        pay(marcela, "40000", PaymentMethod.TRANSFER, today.minusDays(3));

        // 6. Cancelada: pagó un abono antes de cancelar
        Enrollment sebastian = enroll("Sebastián Vargas", "1001234567", "3185556677", anual, today.minusDays(100), EnrollmentStatus.CANCELLED);
        pay(sebastian, "200000", PaymentMethod.CARD, today.minusDays(100));

        // 7. Activa, trimestral pagado en dos abonos
        Enrollment paola = enroll("Paola Ruiz", "1015550123", "3126667788", trimestral, today.minusDays(20), EnrollmentStatus.ACTIVE);
        pay(paola, "100000", PaymentMethod.CASH, today.minusDays(20));
        pay(paola, "110000", PaymentMethod.CARD, today.minusDays(8));

        // 8. Pendiente sin pagos todavía (empieza mañana)
        enroll("Diego Castro", "80456123", "3023334455", anual, today.plusDays(1), EnrollmentStatus.PENDING);

        LOG.info("Datos de prueba cargados: 5 planes, 8 inscripciones y 8 pagos.");
    }

    private Enrollment enroll(String name, String document, String phone, Plan plan,
                              LocalDate start, EnrollmentStatus status) {
        Enrollment enrollment = new Enrollment();
        enrollment.setMemberName(name);
        enrollment.setMemberDocument(document);
        enrollment.setMemberPhone(phone);
        enrollment.setPlan(plan);
        enrollment.setStartDate(start);
        enrollment.setEndDate(start.plusDays(plan.getDurationDays()));
        enrollment.setStatus(status);
        return enrollmentRepository.save(enrollment);
    }

    private void pay(Enrollment enrollment, String amount, PaymentMethod method, LocalDate day) {
        Payment payment = new Payment();
        payment.setEnrollment(enrollment);
        payment.setAmount(new BigDecimal(amount));
        payment.setMethod(method);
        payment.setPaymentDate(day.atTime(LocalTime.of(9, 30)));
        paymentRepository.save(payment);
    }
}
