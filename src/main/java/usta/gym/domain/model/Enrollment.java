package usta.gym.domain.model;

import java.time.LocalDate;

// Inscripción de un socio a un plan, con su vigencia (fecha de inicio y de fin)
public class Enrollment {

    private Long id;
    private String memberName;
    private String memberDocument;
    private String memberPhone;
    private Plan plan;
    private LocalDate startDate;
    private LocalDate endDate;
    private EnrollmentStatus status;

    public Enrollment() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getMemberDocument() { return memberDocument; }
    public void setMemberDocument(String memberDocument) { this.memberDocument = memberDocument; }
    public String getMemberPhone() { return memberPhone; }
    public void setMemberPhone(String memberPhone) { this.memberPhone = memberPhone; }
    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }
}
