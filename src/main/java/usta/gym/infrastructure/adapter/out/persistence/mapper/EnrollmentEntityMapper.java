package usta.gym.infrastructure.adapter.out.persistence.mapper;

import usta.gym.domain.model.Enrollment;
import usta.gym.infrastructure.adapter.out.persistence.entity.EnrollmentEntity;
import usta.gym.infrastructure.adapter.out.persistence.entity.PlanEntity;

// Convierte entre la inscripción del dominio (Enrollment) y la entidad JPA (EnrollmentEntity)
public final class EnrollmentEntityMapper {

    private EnrollmentEntityMapper() {
    }

    // Dominio -> entidad. Recibe la entidad del plan (relación ya resuelta).
    public static EnrollmentEntity toEntity(Enrollment enrollment, PlanEntity planEntity) {
        if (enrollment == null) {
            return null;
        }
        EnrollmentEntity entity = new EnrollmentEntity();
        entity.setId(enrollment.getId());
        entity.setMemberName(enrollment.getMemberName());
        entity.setMemberDocument(enrollment.getMemberDocument());
        entity.setMemberPhone(enrollment.getMemberPhone());
        entity.setPlan(planEntity);
        entity.setStartDate(enrollment.getStartDate());
        entity.setEndDate(enrollment.getEndDate());
        entity.setStatus(enrollment.getStatus());
        return entity;
    }

    public static Enrollment toDomain(EnrollmentEntity entity) {
        if (entity == null) {
            return null;
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setId(entity.getId());
        enrollment.setMemberName(entity.getMemberName());
        enrollment.setMemberDocument(entity.getMemberDocument());
        enrollment.setMemberPhone(entity.getMemberPhone());
        enrollment.setPlan(PlanEntityMapper.toDomain(entity.getPlan()));
        enrollment.setStartDate(entity.getStartDate());
        enrollment.setEndDate(entity.getEndDate());
        enrollment.setStatus(entity.getStatus());
        return enrollment;
    }
}
