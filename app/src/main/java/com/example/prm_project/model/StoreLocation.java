package com.example.prm_project.model;

public class StoreLocation {
    private String name;
    private String address;
    private String workingHours;
    private String phone;
    private double latitude;
    private double longitude;

    public StoreLocation() {
        // Required empty constructor
    }

    public StoreLocation(String name, String address, String workingHours, String phone, double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.workingHours = workingHours;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}