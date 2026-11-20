package usta.gym.domain.model;

// Estados de una inscripción.
// PENDING: tiene saldo por pagar | ACTIVE: pagada y vigente | EXPIRED: terminó su vigencia | CANCELLED: anulada
public enum EnrollmentStatus {
    PENDING("Pendiente de pago"),
    ACTIVE("Activa"),
    EXPIRED("Vencida"),
    CANCELLED("Cancelada");

    private final String label;

    EnrollmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
