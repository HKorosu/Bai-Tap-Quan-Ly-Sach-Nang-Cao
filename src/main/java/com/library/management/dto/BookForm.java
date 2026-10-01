package com.library.management.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BookForm {
    @NotBlank(message = "Vui lòng nhập tên sách.")
    @Size(max = 180)
    private String title;
    @NotBlank(message = "Vui lòng nhập tác giả.")
    @Size(max = 120)
    private String author;
    @NotBlank(message = "Vui lòng nhập mã ISBN.")
    @Size(max = 40)
    private String isbn;
    @NotNull(message = "Vui lòng chọn thể loại.")
    private Long categoryId;
    @Min(value = 0, message = "Số lượng không được âm.")
    private int availableQuantity;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }
}
