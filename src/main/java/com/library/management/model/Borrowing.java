package com.library.management.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "borrowings")
public class Borrowing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Member member;
    @Column(nullable = false)
    private LocalDate borrowedDate;
    @Column(nullable = false)
    private LocalDate dueDate;
    private LocalDate returnedDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BorrowingStatus status;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalFineAmount = BigDecimal.ZERO;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal paidFineAmount = BigDecimal.ZERO;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal waivedFineAmount = BigDecimal.ZERO;
    @OneToMany(mappedBy = "borrowing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BorrowingDetail> details = new ArrayList<>();
    @OrderBy("paymentDate DESC, id DESC")
    @OneToMany(mappedBy = "borrowing", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FinePayment> payments = new HashSet<>();
    @OrderBy("approvedDate DESC, id DESC")
    @OneToMany(mappedBy = "borrowing", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FineWaiver> waivers = new HashSet<>();

    protected Borrowing() {}

    public Borrowing(Member member, LocalDate borrowedDate, LocalDate dueDate) {
        this.member = member;
        this.borrowedDate = borrowedDate;
        this.dueDate = dueDate;
        this.status = BorrowingStatus.BORROWING;
    }

    public void addDetail(BorrowingDetail detail) {
        details.add(detail);
        detail.setBorrowing(this);
    }

    public void addPayment(FinePayment payment) {
        payments.add(payment);
        payment.setBorrowing(this);
        paidFineAmount = paidFineAmount.add(payment.getAmount());
    }

    public void addWaiver(FineWaiver waiver) {
        waivers.add(waiver);
        waiver.setBorrowing(this);
        waivedFineAmount = waivedFineAmount.add(waiver.getAmount());
    }

    public void addFine(BigDecimal amount) {
        totalFineAmount = totalFineAmount.add(amount);
    }

    public BigDecimal getUnpaidFineAmount() {
        return totalFineAmount.subtract(paidFineAmount).subtract(waivedFineAmount).max(BigDecimal.ZERO);
    }

    public boolean isFullyReturned() {
        return !details.isEmpty() && details.stream().allMatch(BorrowingDetail::isFullyReturned);
    }

    public void refreshStatus(LocalDate today) {
        if (status == BorrowingStatus.CANCELLED) return;
        if (isFullyReturned()) {
            returnedDate = details.stream().map(BorrowingDetail::getLastReturnedDate)
                    .filter(date -> date != null).max(LocalDate::compareTo).orElse(today);
            if (getUnpaidFineAmount().signum() > 0) status = BorrowingStatus.FINE_PENDING;
            else if (totalFineAmount.signum() > 0) status = BorrowingStatus.FINE_PAID;
            else status = BorrowingStatus.RETURNED;
        } else if (today.isAfter(dueDate)) {
            status = BorrowingStatus.OVERDUE;
        } else if (details.stream().anyMatch(detail -> detail.getReturnedQuantity() > 0)) {
            status = BorrowingStatus.PARTIALLY_RETURNED;
        } else if (today.isAfter(dueDate)) {
            status = BorrowingStatus.OVERDUE;
        } else {
            status = BorrowingStatus.BORROWING;
        }
    }

    public BorrowingStatus getDisplayStatus() {
        if (status != BorrowingStatus.CANCELLED && !isFullyReturned() && LocalDate.now().isAfter(dueDate)) {
            return BorrowingStatus.OVERDUE;
        }
        return status;
    }

    public void cancel() {
        if (details.stream().anyMatch(detail -> detail.getReturnedQuantity() > 0)
                || totalFineAmount.signum() > 0) {
            throw new IllegalStateException("Chỉ được hủy phiếu chưa trả sách và chưa phát sinh phí.");
        }
        status = BorrowingStatus.CANCELLED;
    }

    public Long getId() { return id; }
    public Member getMember() { return member; }
    public LocalDate getBorrowedDate() { return borrowedDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnedDate() { return returnedDate; }
    public BorrowingStatus getStatus() { return status; }
    public BorrowingStatus getDisplayStatusValue() { return getDisplayStatus(); }
    public BigDecimal getTotalFineAmount() { return totalFineAmount; }
    public BigDecimal getPaidFineAmount() { return paidFineAmount; }
    public BigDecimal getWaivedFineAmount() { return waivedFineAmount; }
    public BigDecimal getUnpaidFineAmountValue() { return getUnpaidFineAmount(); }
    public List<BorrowingDetail> getDetails() { return details; }
    public Set<FinePayment> getPayments() { return payments; }
    public Set<FineWaiver> getWaivers() { return waivers; }
}
