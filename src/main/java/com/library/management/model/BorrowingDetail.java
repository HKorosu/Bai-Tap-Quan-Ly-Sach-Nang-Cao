package com.library.management.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "borrowing_details")
public class BorrowingDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Borrowing borrowing;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Book book;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false)
    private int returnedQuantity;
    private LocalDate lastReturnedDate;
    @Column(nullable = false)
    private int lateDays;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal fineAmount = BigDecimal.ZERO;

    protected BorrowingDetail() {}

    public BorrowingDetail(Book book, int quantity) {
        this.book = book;
        this.quantity = quantity;
    }

    void setBorrowing(Borrowing borrowing) { this.borrowing = borrowing; }

    public int getRemainingQuantity() { return quantity - returnedQuantity; }
    public boolean isFullyReturned() { return returnedQuantity == quantity; }
    public void returnBooks(int quantity, LocalDate returnDate, int chargeableLateDays, BigDecimal fine) {
        if (quantity <= 0 || quantity > getRemainingQuantity()) {
            throw new IllegalArgumentException("Số lượng trả phải lớn hơn 0 và không vượt quá số sách đang mượn.");
        }
        returnedQuantity += quantity;
        lastReturnedDate = returnDate;
        lateDays = Math.max(lateDays, chargeableLateDays);
        fineAmount = fineAmount.add(fine);
    }

    public Long getId() { return id; }
    public Borrowing getBorrowing() { return borrowing; }
    public Book getBook() { return book; }
    public int getQuantity() { return quantity; }
    public int getReturnedQuantity() { return returnedQuantity; }
    public LocalDate getLastReturnedDate() { return lastReturnedDate; }
    public int getLateDays() { return lateDays; }
    public BigDecimal getFineAmount() { return fineAmount; }
}
