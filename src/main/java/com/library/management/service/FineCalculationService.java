package com.library.management.service;

import com.library.management.model.BorrowingDetail;
import com.library.management.model.FinePolicy;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class FineCalculationService {
    public FineQuote calculate(LocalDate dueDate, LocalDate actualReturnDate, int quantity,
                               BigDecimal amountAlreadyCharged, FinePolicy policy) {
        int lateDays = (int) Math.max(0, ChronoUnit.DAYS.between(dueDate, actualReturnDate) - policy.getGraceDays());
        BigDecimal fine = policy.getDailyFineAmount().multiply(BigDecimal.valueOf((long) lateDays * quantity));
        if (policy.getMaxFineAmount() != null) {
            fine = fine.add(amountAlreadyCharged).min(policy.getMaxFineAmount()).subtract(amountAlreadyCharged);
            fine = fine.max(BigDecimal.ZERO);
        }
        return new FineQuote(lateDays, fine);
    }

    public FineQuote calculate(BorrowingDetail detail, LocalDate dueDate, LocalDate returnDate,
                               int quantity, FinePolicy policy) {
        return calculate(dueDate, returnDate, quantity, detail.getFineAmount(), policy);
    }

    public record FineQuote(int lateDays, BigDecimal fineAmount) {}
}
