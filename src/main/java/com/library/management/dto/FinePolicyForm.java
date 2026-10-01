package com.library.management.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class FinePolicyForm {
    @NotNull(message = "Vui lòng nhập mức phạt mỗi ngày.")
    @DecimalMin(value = "0.00", message = "Mức phạt không được âm.")
    private BigDecimal dailyFineAmount;
    @DecimalMin(value = "0.00", message = "Mức phí tối đa không được âm.")
    private BigDecimal maxFineAmount;
    @Min(value = 0, message = "Số ngày miễn phí không được âm.")
    private int graceDays;

    public BigDecimal getDailyFineAmount() { return dailyFineAmount; }
    public void setDailyFineAmount(BigDecimal dailyFineAmount) { this.dailyFineAmount = dailyFineAmount; }
    public BigDecimal getMaxFineAmount() { return maxFineAmount; }
    public void setMaxFineAmount(BigDecimal maxFineAmount) { this.maxFineAmount = maxFineAmount; }
    public int getGraceDays() { return graceDays; }
    public void setGraceDays(int graceDays) { this.graceDays = graceDays; }
}
