package com.library.management.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowingForm {
    @NotNull(message = "Vui lòng chọn độc giả.")
    private Long memberId;
    @NotNull(message = "Vui lòng chọn hạn trả.")
    @FutureOrPresent(message = "Hạn trả không được ở quá khứ.")
    private LocalDate dueDate;
    private List<BorrowLineForm> lines = new ArrayList<>();

    public BorrowingForm() {
        for (int i = 0; i < 5; i++) lines.add(new BorrowLineForm());
    }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public List<BorrowLineForm> getLines() { return lines; }
    public void setLines(List<BorrowLineForm> lines) { this.lines = lines; }
}
