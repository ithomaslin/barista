package com.richify.goobucks.model;

import android.support.v4.content.PermissionChecker;

import java.sql.Timestamp;

/**
 * Created by thomaslin on 10/03/2018.
 *
 */

public class Order {

    public enum OrderStatus {
        PENDING, ASSIGNED, DELIVERED
    }

    private String ownerId, baristaId, orderId, orderStatus;
    private Timestamp createdAt;

    public Order(String ownerId, String baristaId, String orderId, String orderStatus, Timestamp createdAt) {
        this.ownerId = ownerId;
        this.baristaId = baristaId;
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.createdAt = createdAt;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getBaristaId() {
        return baristaId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public String updateOrderStatus(String _orderStatus) {
        this.orderStatus = _orderStatus;
        return orderStatus;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

}
