package com.library.management.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "fine_policies")
public class FinePolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal dailyFineAmount;
    @Column(precision = 12, scale = 2)
    private BigDecimal maxFineAmount;
    @Column(nullable = false)
    private int graceDays;
    @Column(nullable = false)
    private boolean active;

    protected FinePolicy() {}
    public FinePolicy(BigDecimal dailyFineAmount, BigDecimal maxFineAmount, int graceDays, boolean active) {
        this.dailyFineAmount = dailyFineAmount;
        this.maxFineAmount = maxFineAmount;
        this.graceDays = graceDays;
        this.active = active;
    }
    public void deactivate() { active = false; }
    public Long getId() { return id; }
    public BigDecimal getDailyFineAmount() { return dailyFineAmount; }
    public BigDecimal getMaxFineAmount() { return maxFineAmount; }
    public int getGraceDays() { return graceDays; }
    public boolean isActive() { return active; }
}
