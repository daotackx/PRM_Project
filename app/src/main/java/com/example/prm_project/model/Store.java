package com.example.prm_project.model;
public class Store {
    public String id;
    public String name;
    public double latitude;
    public double longitude;
    public String address;

    public Store() {}

    public Store(String id, String name, double latitude, double longitude, String address) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }
}
