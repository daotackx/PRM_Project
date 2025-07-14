package com.example.prm_project.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    private String userId;
    private String bookId;
    private int quantity;
    private boolean selected;
    private long createdAt;
    private long updatedAt;

    // Cache fields for display (không lưu vào Firebase - dùng @Exclude)
    private transient Book book;
    
    // Cached data for quick access (từ Book object)
    private String bookTitle;
    private String bookAuthor;
    private int bookPrice;
    private String bookImageUrl;

    public CartItem() {
        // Required empty constructor for Firebase
    }

    public CartItem(String userId, String bookId, int quantity) {
        this.userId = userId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.selected = false;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public CartItem(Book book, int quantity, String userId) {
        this.book = book;
        this.bookId = book != null ? book.getId() : null;
        this.userId = userId;
        this.quantity = quantity;
        this.selected = false;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        
        // Cache book info
        if (book != null) {
            this.bookTitle = book.getName();
            this.bookAuthor = book.getAuthor();
            this.bookPrice = book.getPrice();
            this.bookImageUrl = book.getImageUrl();
        }
    }

    // Parcelable constructor
    protected CartItem(Parcel in) {
        userId = in.readString();
        bookId = in.readString();
        quantity = in.readInt();
        selected = in.readByte() != 0;
        createdAt = in.readLong();
        updatedAt = in.readLong();
        bookTitle = in.readString();
        bookAuthor = in.readString();
        bookPrice = in.readInt();
        bookImageUrl = in.readString();
        // Note: book object không serialize vì là transient
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(bookId);
        dest.writeInt(quantity);
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeLong(createdAt);
        dest.writeLong(updatedAt);
        dest.writeString(bookTitle);
        dest.writeString(bookAuthor);
        dest.writeInt(bookPrice);
        dest.writeString(bookImageUrl);
    }

    // Firebase fields getters/setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity;
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { 
        this.selected = selected;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // Cached book info getters/setters (cho Firebase)
    public String getBookTitle() { return bookTitle != null ? bookTitle : ""; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getBookAuthor() { return bookAuthor != null ? bookAuthor : ""; }
    public void setBookAuthor(String bookAuthor) { this.bookAuthor = bookAuthor; }

    public int getBookPrice() { return bookPrice; }
    public void setBookPrice(int bookPrice) { this.bookPrice = bookPrice; }

    public String getBookImageUrl() { return bookImageUrl != null ? bookImageUrl : ""; }
    public void setBookImageUrl(String bookImageUrl) { this.bookImageUrl = bookImageUrl; }

    // Transient Book object methods
    public Book getBook() { return book; }
    public void setBook(Book book) { 
        this.book = book;
        if (book != null) {
            this.bookId = book.getId();
            this.bookTitle = book.getName();
            this.bookAuthor = book.getAuthor();
            this.bookPrice = book.getPrice();
            this.bookImageUrl = book.getImageUrl();
        }
    }

    // Helper methods
    public int getTotalPrice() {
        return bookPrice * quantity;
    }

    public String getFormattedPrice() {
        return String.format("%,dđ", bookPrice);
    }

    public String getFormattedTotalPrice() {
        return String.format("%,dđ", getTotalPrice());
    }

    public boolean hasValidBook() {
        return bookTitle != null && !bookTitle.isEmpty();
    }

    // Backward compatibility
    public int getPrice() {
        return getBookPrice();
    }

    public void updateFromBook(Book book) {
        if (book != null) {
            this.book = book;
            this.bookTitle = book.getName();
            this.bookAuthor = book.getAuthor();
            this.bookPrice = book.getPrice();
            this.bookImageUrl = book.getImageUrl();
        }
    }

    // Utility method to update timestamp
    public void touch() {
        this.updatedAt = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "userId='" + userId + '\'' +
                ", bookId='" + bookId + '\'' +
                ", quantity=" + quantity +
                ", selected=" + selected +
                ", bookTitle='" + bookTitle + '\'' +
                '}';
    }
}