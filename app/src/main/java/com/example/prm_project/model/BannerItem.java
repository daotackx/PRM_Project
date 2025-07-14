package com.example.prm_project.model;

public class BannerItem {
    private int id;
    private String title;
    private String description;
    private String imageUrl;
    private String actionUrl;

    // Empty constructor
    public BannerItem() {}

    // Constructor with 3 parameters (id as int, title, imageUrl)
    public BannerItem(int id, String title, String imageUrl) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.description = "";
        this.actionUrl = "";
    }

    // Full constructor
    public BannerItem(int id, String title, String description, String imageUrl, String actionUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.actionUrl = actionUrl;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
}
