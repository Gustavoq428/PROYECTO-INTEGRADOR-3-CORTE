package usta.gym.infrastructure.adapter.in.web;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import usta.gym.domain.model.EnrollmentAccount;
import usta.gym.domain.model.IncomeReport;
import usta.gym.domain.model.Payment;
import usta.gym.domain.model.PaymentMethod;
import usta.gym.domain.model.PaymentReceipt;
import usta.gym.domain.service.MoneyFormat;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

// Genera los reportes PDF con OpenPDF usando datos reales de la base de datos
public final class PdfReports {

    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(0x0D, 0x6E, 0xFD));
    private static final Font SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(0x60, 0x60, 0x60));
    private static final Font HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font TEXT = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Color BLUE = new Color(0x0D, 0x6E, 0xFD);
    private static final Color LIGHT = new Color(0xF2, 0xF4, 0xF7);

    private PdfReports() {
    }

    // Comprobante de un pago
    public static byte[] receipt(PaymentReceipt receipt) {
        Payment p = receipt.payment();
        return build(PageSize.A5, document -> {
            document.add(new Paragraph("Comprobante de pago #" + p.getId(), TITLE));
            document.add(spaced(new Paragraph("Gimnasio - Planes, inscripciones y pagos", SUBTITLE), 14));

            PdfPTable table = new PdfPTable(new float[]{2f, 3f});
            table.setWidthPercentage(100);
            row(table, "Fecha del pago", WebMapper.dateTime(p.getPaymentDate()), false);
            row(table, "Socio", p.getEnrollment().getMemberName(), true);
            row(table, "Documento", p.getEnrollment().getMemberDocument(), false);
            row(table, "Plan", p.getEnrollment().getPlan().getName(), true);
            row(table, "Vigencia", WebMapper.date(p.getEnrollment().getStartDate()) + " al "
                    + WebMapper.date(p.getEnrollment().getEndDate()), false);
            row(table, "Medio de pago", p.getMethod().getLabel(), true);
            row(table, "Valor de este pago", MoneyFormat.format(p.getAmount()), false);
            row(table, "Total pagado a la fecha", MoneyFormat.format(receipt.paidToDate()), true);
            row(table, "Saldo pendiente", MoneyFormat.format(receipt.balance()), false);
            document.add(table);

            document.add(spaced(new Paragraph("Conserva este comprobante como soporte de tu pago.", SUBTITLE), 0));
        });
    }

    // Reporte de ingresos entre dos fechas
    public static byte[] income(IncomeReport report) {
        return build(PageSize.A4, document -> {
            document.add(new Paragraph("Reporte de ingresos", TITLE));
            document.add(spaced(new Paragraph("Del " + WebMapper.date(report.from()) + " al "
                    + WebMapper.date(report.to()) + "  |  Generado: "
                    + WebMapper.dateTime(LocalDateTime.now()), SUBTITLE), 12));

            PdfPTable table = new PdfPTable(new float[]{1f, 2.2f, 3f, 2.4f, 2f, 2f});
            table.setWidthPercentage(100);
            headers(table, "Pago", "Fecha", "Socio", "Plan", "Medio", "Valor");
            boolean alternate = false;
            for (Payment p : report.payments()) {
                Color background = alternate ? LIGHT : Color.WHITE;
                cell(table, "#" + p.getId(), background, Element.ALIGN_LEFT);
                cell(table, WebMapper.dateTime(p.getPaymentDate()), background, Element.ALIGN_LEFT);
                cell(table, p.getEnrollment().getMemberName(), background, Element.ALIGN_LEFT);
                cell(table, p.getEnrollment().getPlan().getName(), background, Element.ALIGN_LEFT);
                cell(table, p.getMethod().getLabel(), background, Element.ALIGN_LEFT);
                cell(table, MoneyFormat.format(p.getAmount()), background, Element.ALIGN_RIGHT);
                alternate = !alternate;
            }
            if (report.payments().isEmpty()) {
                PdfPCell empty = new PdfPCell(new Phrase("No hay pagos en este rango de fechas.", TEXT));
                empty.setColspan(6);
                empty.setPadding(8);
                table.addCell(empty);
            }
            document.add(table);

            document.add(spaced(new Paragraph("Resumen por medio de pago", BOLD), 6));
            for (PaymentMethod method : PaymentMethod.values()) {
                document.add(new Paragraph(method.getLabel() + ": "
                        + MoneyFormat.format(report.totalByMethod().get(method)), TEXT));
            }
            document.add(spaced(new Paragraph("Pagos registrados: " + report.payments().size()
                    + "   |   TOTAL RECAUDADO: " + MoneyFormat.format(report.total()), BOLD), 0));
        });
    }

    // Listado de inscripciones con su estado de cuenta
    public static byte[] enrollments(List<EnrollmentAccount> accounts) {
        return build(PageSize.A4.rotate(), document -> {
            document.add(new Paragraph("Listado de inscripciones", TITLE));
            document.add(spaced(new Paragraph("Total: " + accounts.size() + " inscripciones  |  Generado: "
                    + WebMapper.dateTime(LocalDateTime.now()), SUBTITLE), 12));

            PdfPTable table = new PdfPTable(new float[]{0.7f, 3f, 1.8f, 1.8f, 1.7f, 1.7f, 2f, 1.7f, 1.7f});
            table.setWidthPercentage(100);
            headers(table, "N.º", "Socio", "Documento", "Plan", "Inicio", "Fin", "Estado", "Pagado", "Saldo");
            boolean alternate = false;
            for (EnrollmentAccount a : accounts) {
                Color background = alternate ? LIGHT : Color.WHITE;
                cell(table, "#" + a.enrollment().getId(), background, Element.ALIGN_LEFT);
                cell(table, a.enrollment().getMemberName(), background, Element.ALIGN_LEFT);
                cell(table, a.enrollment().getMemberDocument(), background, Element.ALIGN_LEFT);
                cell(table, a.enrollment().getPlan().getName(), background, Element.ALIGN_LEFT);
                cell(table, WebMapper.date(a.enrollment().getStartDate()), background, Element.ALIGN_LEFT);
                cell(table, WebMapper.date(a.enrollment().getEndDate()), background, Element.ALIGN_LEFT);
                cell(table, a.enrollment().getStatus().getLabel(), background, Element.ALIGN_LEFT);
                cell(table, MoneyFormat.format(a.paid()), background, Element.ALIGN_RIGHT);
                cell(table, MoneyFormat.format(a.balance()), background, Element.ALIGN_RIGHT);
                alternate = !alternate;
            }
            document.add(table);
        });
    }

    // ---------- utilidades internas ----------

    private interface Content {
        void write(Document document) throws DocumentException;
    }

    private static byte[] build(com.lowagie.text.Rectangle pageSize, Content content) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(pageSize, 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(document, output);
            document.open();
            content.write(document);
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        return output.toByteArray();
    }

    private static Paragraph spaced(Paragraph paragraph, float after) {
        paragraph.setSpacingAfter(after);
        paragraph.setSpacingBefore(after == 0 ? 10 : 0);
        return paragraph;
    }

    private static void headers(PdfPTable table, String... titles) {
        for (String title : titles) {
            PdfPCell cell = new PdfPCell(new Phrase(title, HEADER));
            cell.setBackgroundColor(BLUE);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private static void cell(PdfPTable table, String text, Color background, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, TEXT));
        cell.setBackgroundColor(background);
        cell.setHorizontalAlignment(align);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private static void row(PdfPTable table, String label, String value, boolean shaded) {
        Color background = shaded ? LIGHT : Color.WHITE;
        PdfPCell left = new PdfPCell(new Phrase(label, BOLD));
        left.setBackgroundColor(background);
        left.setPadding(6);
        table.addCell(left);
        PdfPCell right = new PdfPCell(new Phrase(value, TEXT));
        right.setBackgroundColor(background);
        right.setPadding(6);
        table.addCell(right);
    }
}
