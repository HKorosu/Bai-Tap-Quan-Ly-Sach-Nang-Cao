package com.library.management.service;

import com.library.management.model.FinePolicy;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class FineCalculationServiceTest {
    private final FineCalculationService service = new FineCalculationService();

    @Test
    void calculatesFeeForLateReturnOfTwoBooks() {
        FinePolicy policy = new FinePolicy(new BigDecimal("1000"), null, 0, true);
        var result = service.calculate(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 13),
                2, BigDecimal.ZERO, policy);
        assertEquals(3, result.lateDays());
        assertEquals(new BigDecimal("6000"), result.fineAmount());
    }

    @Test
    void appliesGracePeriodAndMaximumPerBorrowingLine() {
        FinePolicy policy = new FinePolicy(new BigDecimal("1000"), new BigDecimal("2500"), 1, true);
        var result = service.calculate(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 30),
                2, new BigDecimal("1000"), policy);
        assertEquals(19, result.lateDays());
        assertEquals(new BigDecimal("1500"), result.fineAmount());
    }

    @Test
    void doesNotChargeOnOrBeforeDueDate() {
        FinePolicy policy = new FinePolicy(new BigDecimal("1000"), null, 0, true);
        var result = service.calculate(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 10),
                1, BigDecimal.ZERO, policy);
        assertEquals(0, result.lateDays());
        assertEquals(BigDecimal.ZERO, result.fineAmount());
    }
}
