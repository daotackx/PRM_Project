package com.example.prm_project.model;

public class Book {
    private String id;
    private String name;
    private String author;
    private String description;
    private int price;
    private int quantity;
    private String imageUrl;
    private BookType bookType;

    public Book() {
        // Firestore requires an empty constructor
    }

    public Book(String id, String name, String author, String description, int price, int quantity, String imageUrl, BookType bookType) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.bookType = bookType;
    }

    // Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BookType getBookType() {
        return bookType;
    }

    public void setBookType(BookType bookType) {
        this.bookType = bookType;
    }

    // Helper methods
    public String getFormattedPrice() {
        return String.format("$%,d", price);
    }

    public String getStockDisplay() {
        return quantity + " cuốn";
    }

    public String getCategoryName() {
        if (bookType != null) {
            return bookType.getName();
        }
        return "Chưa phân loại";
    }

    public boolean hasStock() {
        return quantity > 0;
    }
}
