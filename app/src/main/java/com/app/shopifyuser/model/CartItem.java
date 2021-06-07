package com.app.shopifyuser.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class CartItem implements Serializable {

    private int id;
    private int quantity;

    @Exclude
    private String name;
    @Exclude
    private String imageUrl;
    @Exclude
    private double price;


    public CartItem() {
    }

    public CartItem(int id, int quantity) {
        this.name = name;
        this.id = id;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    @Exclude
    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    @Exclude
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getPrice() {
        return price;
    }

    @Exclude
    public void setPrice(double price) {
        this.price = price;
    }
}
