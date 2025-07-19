package com.example.prm_project.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class NotificationModel implements Serializable {
    private String id;
    private String userId;
    private String orderId;
    private String title;
    private String message;
    private String type;
    private String status;
    private boolean isRead;
    private Timestamp createdAt;
    private Timestamp readAt;
    private Map<String, Object> orderDetails;

    // Constructors
    public NotificationModel() {}

    public NotificationModel(String id, String userId, String orderId, String title, 
                           String message, String type, String status, boolean isRead, 
                           Timestamp createdAt, Map<String, Object> orderDetails) {
        this.id = id;
        this.userId = userId;
        this.orderId = orderId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.status = status;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.orderDetails = orderDetails;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @PropertyName("isRead")
    public boolean isRead() { return isRead; }
    
    @PropertyName("isRead") 
    public void setIsRead(boolean isRead) { this.isRead = isRead; }
    
    // Thêm setter cũ để backward compatibility
    public void setRead(boolean read) { this.isRead = read; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getReadAt() { return readAt; }
    public void setReadAt(Timestamp readAt) { this.readAt = readAt; }

    public Map<String, Object> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(Map<String, Object> orderDetails) { this.orderDetails = orderDetails; }

    // Helper methods
    public String getFormattedDate() {
        if (createdAt != null) {
            return android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", createdAt.toDate()).toString();
        }
        return "Vừa xong";
    }

    public String getOrderTotal() {
        if (orderDetails != null && orderDetails.containsKey("total")) {
            Object total = orderDetails.get("total");
            if (total instanceof Number) {
                return String.format("%,d đ", ((Number) total).longValue());
            }
        }
        return "0 đ";
    }

    public String getCustomerName() {
        if (orderDetails != null && orderDetails.containsKey("customerName")) {
            return (String) orderDetails.get("customerName");
        }
        return "";
    }
}
