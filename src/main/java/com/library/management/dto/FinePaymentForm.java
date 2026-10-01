package com.library.management.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class FinePaymentForm {
    @NotNull(message = "Vui lòng nhập số tiền.")
    @DecimalMin(value = "0.01", message = "Số tiền phải lớn hơn 0.")
    private BigDecimal amount;
    @NotBlank(message = "Vui lòng chọn phương thức thanh toán.")
    private String method;
    @Size(max = 500)
    private String note;
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
