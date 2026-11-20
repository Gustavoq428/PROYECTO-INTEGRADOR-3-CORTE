package usta.gym.infrastructure.adapter.out.persistence.mapper;

import usta.gym.domain.model.Plan;
import usta.gym.infrastructure.adapter.out.persistence.entity.PlanEntity;

// Convierte entre el plan del dominio (Plan) y la entidad JPA (PlanEntity)
public final class PlanEntityMapper {

    private PlanEntityMapper() {
    }

    public static PlanEntity toEntity(Plan plan) {
        if (plan == null) {
            return null;
        }
        PlanEntity entity = new PlanEntity();
        entity.setId(plan.getId());
        entity.setName(plan.getName());
        entity.setDescription(plan.getDescription());
        entity.setPrice(plan.getPrice());
        entity.setDurationDays(plan.getDurationDays());
        entity.setActive(plan.isActive());
        return entity;
    }

    public static Plan toDomain(PlanEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Plan(entity.getId(), entity.getName(), entity.getDescription(),
                entity.getPrice(), entity.getDurationDays(), entity.isActive());
    }
}
