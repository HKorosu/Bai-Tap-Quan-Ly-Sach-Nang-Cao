package com.library.management.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class FineWaiverForm {
    @NotNull(message = "Vui lòng nhập số tiền miễn giảm.")
    @DecimalMin(value = "0.01", message = "Số tiền phải lớn hơn 0.")
    private BigDecimal amount;
    @NotBlank(message = "Vui lòng nhập lý do.")
    @Size(max = 500)
    private String reason;
    @NotBlank(message = "Vui lòng nhập người duyệt.")
    @Size(max = 150)
    private String approvedBy;
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
}
