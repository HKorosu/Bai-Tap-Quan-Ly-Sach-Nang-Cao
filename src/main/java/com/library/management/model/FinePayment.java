package com.library.management.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fine_payments")
public class FinePayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Borrowing borrowing;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false)
    private LocalDate paymentDate;
    @Column(nullable = false, length = 40)
    private String method;
    @Column(length = 500)
    private String note;

    protected FinePayment() {}
    public FinePayment(BigDecimal amount, LocalDate paymentDate, String method, String note) {
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.method = method;
        this.note = note;
    }
    void setBorrowing(Borrowing borrowing) { this.borrowing = borrowing; }
    public Long getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public String getMethod() { return method; }
    public String getNote() { return note; }
}
