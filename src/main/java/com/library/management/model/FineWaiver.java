package com.library.management.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fine_waivers")
public class FineWaiver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Borrowing borrowing;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 500)
    private String reason;
    @Column(nullable = false, length = 150)
    private String approvedBy;
    @Column(nullable = false)
    private LocalDate approvedDate;

    protected FineWaiver() {}
    public FineWaiver(BigDecimal amount, String reason, String approvedBy, LocalDate approvedDate) {
        this.amount = amount;
        this.reason = reason;
        this.approvedBy = approvedBy;
        this.approvedDate = approvedDate;
    }
    void setBorrowing(Borrowing borrowing) { this.borrowing = borrowing; }
    public BigDecimal getAmount() { return amount; }
    public String getReason() { return reason; }
    public String getApprovedBy() { return approvedBy; }
    public LocalDate getApprovedDate() { return approvedDate; }
}
