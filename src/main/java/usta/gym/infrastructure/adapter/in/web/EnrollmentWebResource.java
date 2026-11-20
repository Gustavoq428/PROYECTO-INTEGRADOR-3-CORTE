package usta.gym.infrastructure.adapter.in.web;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import usta.gym.application.port.in.ManageEnrollmentsUseCase;
import usta.gym.application.port.in.ManagePlansUseCase;
import usta.gym.application.port.in.RegisterPaymentUseCase;
import usta.gym.domain.exception.BusinessRuleException;
import usta.gym.domain.model.Enrollment;
import usta.gym.domain.model.EnrollmentAccount;
import usta.gym.domain.model.EnrollmentStatus;
import usta.gym.domain.model.Payment;
import usta.gym.domain.model.PaymentMethod;
import usta.gym.domain.service.MoneyFormat;
import usta.gym.infrastructure.adapter.in.web.Views.EnrollmentForm;
import usta.gym.infrastructure.adapter.in.web.Views.EnrollmentView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

// Adaptador web (entrada): páginas de inscripciones, estado de cuenta y pagos.
// Solo traduce peticiones HTML a llamadas a los casos de uso (puertos de entrada).
@Path("/")
@Produces(MediaType.TEXT_HTML)
@Tag(name = "Inscripciones", description = "Páginas para inscribir socios, ver su estado de cuenta y registrar pagos")
public class EnrollmentWebResource {

    @Inject
    ManageEnrollmentsUseCase enrollments;

    @Inject
    ManagePlansUseCase plans;

    @Inject
    RegisterPaymentUseCase payments;

    @Inject
    @Location("index.html")
    Template index;

    @Inject
    @Location("enrollment-form.html")
    Template enrollmentForm;

    @Inject
    @Location("enrollment-detail.html")
    Template enrollmentDetail;

    @GET
    @Operation(summary = "Página principal: listado de inscripciones",
            description = "Muestra las inscripciones con su estado y saldo. El parámetro status filtra por estado (PENDING, ACTIVE, EXPIRED, CANCELLED).")
    public TemplateInstance home(@QueryParam("status") String status,
                                 @QueryParam("ok") String ok,
                                 @QueryParam("error") String error) {
        List<EnrollmentAccount> accounts = enrollments.listAccounts();

        EnrollmentStatus filter = parseStatus(status);
        List<EnrollmentView> views = accounts.stream()
                .filter(a -> filter == null || a.enrollment().getStatus() == filter)
                .map(WebMapper::toView)
                .toList();

        BigDecimal receivable = accounts.stream()
                .filter(a -> a.enrollment().getStatus() == EnrollmentStatus.PENDING
                        || a.enrollment().getStatus() == EnrollmentStatus.ACTIVE)
                .map(EnrollmentAccount::balance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Pages.of(index, ok, error)
                .data("enrollments", views)
                .data("filter", filter == null ? "" : filter.name())
                .data("total", accounts.size())
                .data("activeCount", count(accounts, EnrollmentStatus.ACTIVE))
                .data("pendingCount", count(accounts, EnrollmentStatus.PENDING))
                .data("expiredCount", count(accounts, EnrollmentStatus.EXPIRED))
                .data("cancelledCount", count(accounts, EnrollmentStatus.CANCELLED))
                .data("receivable", MoneyFormat.format(receivable));
    }

    @GET
    @Path("/enrollments/new")
    @Operation(summary = "Formulario para inscribir a un socio")
    public TemplateInstance newForm() {
        return formPage(new EnrollmentForm(null, "", "", "", LocalDate.now().toString()), null);
    }

    @POST
    @Path("/enrollments")
    @Operation(summary = "Inscribir a un socio en un plan",
            description = "Valida las reglas de negocio (documento, fechas, plan activo, una sola inscripción vigente por socio). Si todo sale bien redirige al estado de cuenta.")
    public Response create(@FormParam("planId") Long planId,
                           @FormParam("memberName") String memberName,
                           @FormParam("memberDocument") String memberDocument,
                           @FormParam("memberPhone") String memberPhone,
                           @FormParam("startDate") String startDate) {
        EnrollmentForm form = new EnrollmentForm(planId, nz(memberName), nz(memberDocument), nz(memberPhone), nz(startDate));
        try {
            LocalDate start = parseDate(startDate, "La fecha de inicio no es válida");
            Enrollment created = enrollments.enroll(planId, memberName, memberDocument, memberPhone, start);
            return Pages.redirectOk("/enrollments/" + created.getId(),
                    "Inscripción creada para " + created.getMemberName() + ". Ahora registra el primer pago.");
        } catch (BusinessRuleException e) {
            return Pages.html(formPage(form, e.getMessage()));
        }
    }

    @GET
    @Path("/enrollments/{id}")
    @Operation(summary = "Detalle y estado de cuenta de una inscripción",
            description = "Datos del socio, plan, vigencia, pagos realizados, saldo pendiente y formulario para pagar.")
    public TemplateInstance detail(@PathParam("id") Long id,
                                   @QueryParam("ok") String ok,
                                   @QueryParam("error") String error) {
        return detailPage(id, ok, error);
    }

    @POST
    @Path("/enrollments/{id}/payments")
    @Operation(summary = "Registrar un pago sobre una inscripción",
            description = "Reglas: no superar el saldo, primer pago de al menos 30% del plan, no pagar inscripciones vencidas o canceladas.")
    public Response pay(@PathParam("id") Long id,
                        @FormParam("amount") String amount,
                        @FormParam("method") String method) {
        try {
            BigDecimal value = parseAmount(amount);
            Payment payment = payments.register(id, value, parseMethod(method));
            return Pages.redirectOk("/enrollments/" + id,
                    "Pago #" + payment.getId() + " registrado por " + MoneyFormat.format(payment.getAmount()));
        } catch (BusinessRuleException e) {
            return Pages.html(detailPage(id, null, e.getMessage()));
        }
    }

    @POST
    @Path("/enrollments/{id}/cancel")
    @Operation(summary = "Cancelar una inscripción",
            description = "Solo se pueden cancelar las inscripciones pendientes o activas.")
    public Response cancel(@PathParam("id") Long id) {
        try {
            enrollments.cancel(id);
            return Pages.redirectOk("/enrollments/" + id, "La inscripción fue cancelada");
        } catch (BusinessRuleException e) {
            return Pages.html(detailPage(id, null, e.getMessage()));
        }
    }

    // ---------- armado de páginas ----------

    private TemplateInstance formPage(EnrollmentForm form, String error) {
        return Pages.of(enrollmentForm, null, error)
                .data("plans", plans.findActive().stream().map(WebMapper::toView).toList())
                .data("form", form);
    }

    private TemplateInstance detailPage(Long id, String ok, String error) {
        EnrollmentAccount account = enrollments.findAccount(id);
        return Pages.of(enrollmentDetail, ok, error)
                .data("e", WebMapper.toView(account))
                .data("payments", payments.listByEnrollment(id).stream().map(WebMapper::toView).toList())
                .data("methods", WebMapper.paymentMethods());
    }

    // ---------- conversiones de lo que llega del formulario ----------

    private static long count(List<EnrollmentAccount> accounts, EnrollmentStatus status) {
        return accounts.stream().filter(a -> a.enrollment().getStatus() == status).count();
    }

    private static EnrollmentStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return EnrollmentStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static LocalDate parseDate(String value, String message) {
        try {
            return LocalDate.parse(value == null ? "" : value.trim());
        } catch (DateTimeParseException e) {
            throw new BusinessRuleException(message);
        }
    }

    private static BigDecimal parseAmount(String value) {
        try {
            return new BigDecimal(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            throw new BusinessRuleException("El valor del pago no es válido");
        }
    }

    private static PaymentMethod parseMethod(String value) {
        try {
            return PaymentMethod.valueOf(value == null ? "" : value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String nz(String value) {
        return value == null ? "" : value;
    }
}
