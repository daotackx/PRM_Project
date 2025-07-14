package com.example.prm_project.model;

public class BookType {
    private String id;
    private String name;

    public BookType() {
        // Empty constructor required for Firestore
    }

    public BookType(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
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
}