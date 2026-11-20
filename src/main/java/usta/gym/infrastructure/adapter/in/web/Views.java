package usta.gym.infrastructure.adapter.in.web;

// Modelos listos para mostrar en las páginas (textos ya formateados). Los crea WebMapper.
public final class Views {

    private Views() {
    }

    public record PlanView(Long id, String name, String description, String price,
                           int durationDays, boolean active) {
    }

    public record EnrollmentView(Long id, String memberName, String memberDocument, String memberPhone,
                                 String planName, String planPrice, String startDate, String endDate,
                                 String status, String statusLabel, String statusClass,
                                 String paid, String balance, boolean hasBalance,
                                 boolean canPay, boolean canCancel) {
    }

    public record PaymentView(Long id, String date, String amount, String method) {
    }

    public record OptionView(String value, String label) {
    }

    // Valores escritos en los formularios (para no perderlos si hay un error)
    public record PlanForm(String name, String description, String price, String durationDays) {
    }

    public record EnrollmentForm(Long planId, String memberName, String memberDocument,
                                 String memberPhone, String startDate) {
    }
}
