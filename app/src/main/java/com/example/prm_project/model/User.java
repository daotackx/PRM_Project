package com.example.prm_project.model;
public class User {
    public String id;
    public String name;
    public String email;
    public String phone;
    public String password;
    public String address;
    public String avatar;

    public User() {} // Firebase yêu cầu constructor rỗng

    public User(String id, String name, String email, String phone, String password, String address, String avatar) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.address = address;
        this.avatar = avatar;
    }
}
