package com.app.shopifyuser.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class DeliveryOrder implements Serializable {

    public static final int STATUS_PENDING = 1, STATUS_PICKUP = 2, STATUS_DELIVERED = 3, STATUS_CANCELLED = 4;


    private String id;
    private String byUser;
    private String toUser;
    private long scheduledTime;
    private long orderedAt;
    private long deliveredAt;
    private List<Map<String, Integer>> orders;
    private GeoPoint location;
    private double totalPrice;
    private int status;

    @Exclude
    private String userImageUrl;
    @Exclude
    private String userName;


    public DeliveryOrder() {
    }

    public DeliveryOrder(String id, String toUser, String byUser, long scheduledTime, long orderedAt,
                         List<Map<String, Integer>> orders, GeoPoint location, double totalPrice, int status) {
        this.id = id;
        this.toUser = toUser;
        this.byUser = byUser;
        this.scheduledTime = scheduledTime;
        this.orderedAt = orderedAt;
        this.location = location;
        this.totalPrice = totalPrice;
        this.orders = orders;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public long getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public List<Map<String, Integer>> getOrders() {
        return orders;
    }

    public void setOrders(List<Map<String, Integer>> orders) {
        this.orders = orders;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public long getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(long orderedAt) {
        this.orderedAt = orderedAt;
    }

    public String getByUser() {
        return byUser;
    }

    public void setByUser(String byUser) {
        this.byUser = byUser;
    }

    @Exclude
    public String getUserImageUrl() {
        return userImageUrl;
    }

    @Exclude
    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    @Exclude
    public String getUserName() {
        return userName;
    }

    @Exclude
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(long deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
}
