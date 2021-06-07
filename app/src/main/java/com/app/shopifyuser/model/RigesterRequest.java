package com.app.shopifyuser.model;


public class RigesterRequest {

    private int id;

    private String email;

    private String password;

    private String username;

    private String phoneNumber;

    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public RigesterRequest(){


    }


    @Override
    public String toString() {
        return "RigesterRequest{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
