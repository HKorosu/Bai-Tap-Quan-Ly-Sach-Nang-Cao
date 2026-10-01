package com.library.management.dto;

import java.math.BigDecimal;

public class ReturnLineForm {
    private Long detailId;
    private String title;
    private int remainingQuantity;
    private int quantityToReturn;
    private BigDecimal fineAlreadyCharged = BigDecimal.ZERO;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(int remainingQuantity) { this.remainingQuantity = remainingQuantity; }
    public int getQuantityToReturn() { return quantityToReturn; }
    public void setQuantityToReturn(int quantityToReturn) { this.quantityToReturn = quantityToReturn; }
    public BigDecimal getFineAlreadyCharged() { return fineAlreadyCharged; }
    public void setFineAlreadyCharged(BigDecimal fineAlreadyCharged) { this.fineAlreadyCharged = fineAlreadyCharged; }
}
