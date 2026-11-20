package usta.gym.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Pago (abono) que un socio hace sobre su inscripción
public class Payment {

    private Long id;
    private Enrollment enrollment;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private PaymentMethod method;

    public Payment() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Enrollment getEnrollment() { return enrollment; }
    public void setEnrollment(Enrollment enrollment) { this.enrollment = enrollment; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
}
