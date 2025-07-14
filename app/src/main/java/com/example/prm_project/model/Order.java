package com.example.prm_project.model;

import java.util.List;

public class Order {
    private String id;
    private String userId;
    private List<OrderItem> items;
    private int subtotal;
    private int shipping;
    private int total;
    private String address;
    private String phone;
    private String note;
    private String paymentMethod;
    private String status;
    private long createdAt;
    private long updatedAt;

    public Order() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Order(String userId, List<OrderItem> items, int subtotal, int shipping, int total,
                 String address, String phone, String note, String paymentMethod, String status) {
        this.userId = userId;
        this.items = items;
        this.subtotal = subtotal;
        this.shipping = shipping;
        this.total = total;
        this.address = address;
        this.phone = phone;
        this.note = note;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public int getSubtotal() { return subtotal; }
    public void setSubtotal(int subtotal) { this.subtotal = subtotal; }

    public int getShipping() { return shipping; }
    public void setShipping(int shipping) { this.shipping = shipping; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}