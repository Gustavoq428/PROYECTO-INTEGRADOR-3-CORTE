package usta.gym.domain.model;

import java.math.BigDecimal;

// Plan de membresía que ofrece el gimnasio (mensual, trimestral...)
public class Plan {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int durationDays;
    private boolean active;

    public Plan() {
    }

    public Plan(Long id, String name, String description, BigDecimal price, int durationDays, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.durationDays = durationDays;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
