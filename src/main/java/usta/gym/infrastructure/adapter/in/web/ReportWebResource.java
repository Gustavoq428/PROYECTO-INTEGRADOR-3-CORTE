package usta.gym.infrastructure.adapter.in.web;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import usta.gym.application.port.in.IncomeReportUseCase;
import usta.gym.application.port.in.ManageEnrollmentsUseCase;
import usta.gym.application.port.in.RegisterPaymentUseCase;
import usta.gym.domain.exception.BusinessRuleException;
import usta.gym.domain.model.IncomeReport;
import usta.gym.domain.model.PaymentMethod;
import usta.gym.domain.service.MoneyFormat;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

// Adaptador web (entrada): pantalla de reportes y descargas de PDF
@Path("/reports")
@Tag(name = "Reportes", description = "Reporte de ingresos por rango de fechas y descargas en PDF")
public class ReportWebResource {

    @Inject
    IncomeReportUseCase incomeReport;

    @Inject
    ManageEnrollmentsUseCase enrollments;

    @Inject
    RegisterPaymentUseCase payments;

    @Inject
    @Location("reports.html")
    Template reports;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Pantalla de reportes: ingresos por rango de fechas",
            description = "Por defecto muestra los últimos 30 días. Parámetros from y to en formato yyyy-MM-dd.")
    public TemplateInstance page(@QueryParam("from") String from,
                                 @QueryParam("to") String to,
                                 @QueryParam("error") String error) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(30);
        LocalDate end = today;
        String message = error;
        try {
            if (from != null && !from.isBlank()) {
                start = LocalDate.parse(from.trim());
            }
            if (to != null && !to.isBlank()) {
                end = LocalDate.parse(to.trim());
            }
        } catch (DateTimeParseException e) {
            message = "Alguna de las fechas no es válida";
            start = today.minusDays(30);
            end = today;
        }

        TemplateInstance page = Pages.of(reports, null, message)
                .data("from", start.toString())
                .data("to", end.toString());
        try {
            IncomeReport report = incomeReport.report(start, end);
            List<MethodTotal> byMethod = new ArrayList<>();
            for (PaymentMethod method : PaymentMethod.values()) {
                byMethod.add(new MethodTotal(method.getLabel(), MoneyFormat.format(report.totalByMethod().get(method))));
            }
            List<ReportRow> rows = report.payments().stream()
                    .map(p -> new ReportRow(p.getId(), WebMapper.dateTime(p.getPaymentDate()),
                            p.getEnrollment().getMemberName(), p.getEnrollment().getPlan().getName(),
                            p.getMethod().getLabel(), MoneyFormat.format(p.getAmount())))
                    .toList();
            return page
                    .data("hasReport", true)
                    .data("rows", rows)
                    .data("count", rows.size())
                    .data("total", MoneyFormat.format(report.total()))
                    .data("byMethod", byMethod);
        } catch (BusinessRuleException e) {
            return Pages.of(reports, null, e.getMessage())
                    .data("from", start.toString())
                    .data("to", end.toString())
                    .data("hasReport", false);
        }
    }

    @GET
    @Path("/income.pdf")
    @Produces("application/pdf")
    @Operation(summary = "Descargar el reporte de ingresos en PDF",
            description = "Resumen de pagos entre las fechas from y to (yyyy-MM-dd), con total por medio de pago.")
    public Response incomePdf(@QueryParam("from") String from, @QueryParam("to") String to) {
        try {
            LocalDate start = LocalDate.parse(from == null ? "" : from.trim());
            LocalDate end = LocalDate.parse(to == null ? "" : to.trim());
            byte[] pdf = PdfReports.income(incomeReport.report(start, end));
            return pdf("ingresos-" + start + "-a-" + end + ".pdf", pdf);
        } catch (DateTimeParseException e) {
            return Pages.redirectError("/reports", "Las fechas del reporte no son válidas");
        } catch (BusinessRuleException e) {
            return Pages.redirectError("/reports", e.getMessage());
        }
    }

    @GET
    @Path("/receipt/{paymentId}.pdf")
    @Produces("application/pdf")
    @Operation(summary = "Descargar el comprobante de un pago en PDF")
    public Response receiptPdf(@PathParam("paymentId") Long paymentId) {
        byte[] pdf = PdfReports.receipt(payments.findReceipt(paymentId));
        return pdf("comprobante-" + paymentId + ".pdf", pdf);
    }

    @GET
    @Path("/enrollments.pdf")
    @Produces("application/pdf")
    @Operation(summary = "Descargar el listado de inscripciones en PDF")
    public Response enrollmentsPdf() {
        return pdf("inscripciones.pdf", PdfReports.enrollments(enrollments.listAccounts()));
    }

    private static Response pdf(String filename, byte[] content) {
        return Response.ok(content, "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .build();
    }

    // Total por medio de pago ya formateado para la vista
    public record MethodTotal(String label, String total) {
    }

    // Fila de la tabla de pagos del reporte
    public record ReportRow(Long id, String date, String member, String plan, String method, String amount) {
    }
}
