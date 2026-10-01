package com.library.management.model;

import jakarta.persistence.*;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 180)
    private String title;
    @Column(nullable = false, length = 120)
    private String author;
    @Column(nullable = false, unique = true, length = 40)
    private String isbn;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Category category;
    @Column(nullable = false)
    private int availableQuantity;
    @Version
    private long version;

    protected Book() {}

    public Book(String title, String author, String isbn, Category category, int availableQuantity) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.availableQuantity = availableQuantity;
    }

    public void borrow(int quantity) {
        if (quantity <= 0 || availableQuantity < quantity) {
            throw new IllegalArgumentException("Số lượng sách còn lại không đủ.");
        }
        availableQuantity -= quantity;
    }

    public void restore(int quantity) {
        availableQuantity += quantity;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public Category getCategory() { return category; }
    public int getAvailableQuantity() { return availableQuantity; }
    public long getVersion() { return version; }
}
