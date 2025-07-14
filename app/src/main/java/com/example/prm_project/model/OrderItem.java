package com.example.prm_project.model;

public class OrderItem {
    private String bookId;
    private String bookTitle;
    private int bookPrice;
    private int quantity;
    private int totalPrice;

    public OrderItem() {}

    public OrderItem(String bookId, String bookTitle, int bookPrice, int quantity, int totalPrice) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookPrice = bookPrice;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public int getBookPrice() { return bookPrice; }
    public void setBookPrice(int bookPrice) { this.bookPrice = bookPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getTotalPrice() { return totalPrice; }
    public void setTotalPrice(int totalPrice) { this.totalPrice = totalPrice; }
}