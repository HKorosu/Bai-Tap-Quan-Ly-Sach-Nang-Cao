package com.library.management.model;

public enum BorrowingStatus {
    BORROWING,
    PARTIALLY_RETURNED,
    RETURNED,
    OVERDUE,
    FINE_PENDING,
    FINE_PAID,
    CANCELLED;

    public String getLabel() {
        return switch (this) {
            case BORROWING -> "Đang mượn";
            case PARTIALLY_RETURNED -> "Đã trả một phần";
            case RETURNED -> "Đã trả hết";
            case OVERDUE -> "Quá hạn";
            case FINE_PENDING -> "Còn nợ phí";
            case FINE_PAID -> "Đã thanh toán phí";
            case CANCELLED -> "Đã hủy";
        };
    }
}
