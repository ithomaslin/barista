package com.richify.goobucks.model;

import android.support.v4.content.PermissionChecker;

import java.sql.Timestamp;

/**
 * Created by thomaslin on 10/03/2018.
 *
 */

public class Order {

    public enum OrderStatus {
        PENDING(0), ASSIGNED(1), DELIVERED(2)
        ;

        private final int orderStatus;

        OrderStatus(int orderStatus) {
            this.orderStatus = orderStatus;
        }

        public int getOrderStatus() {
            return this.orderStatus;
        }
    }

    private String orderId, ownerId, assigneeId, feedback;
    private int type, status, rating;
    private Boolean isDecaf;
    private Timestamp createdAt;

    public Order(String ownerId, String assigneeId,
                 int type, int status, int rating, String feedback,
                 Boolean isDecaf, Timestamp createdAt) {
        this.ownerId = ownerId;
        this.assigneeId = assigneeId;
        this.type = type;
        this.status = status;
        this.rating = rating;
        this.feedback = feedback;
        this.isDecaf = isDecaf;
        this.createdAt = createdAt;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getFeedback() {
        return feedback;
    }

    public int getType() {
        return type;
    }

    public int getStatus() {
        return status;
    }

    public int getRating() {
        return rating;
    }

    public Boolean getIsDecaf() {
        return isDecaf;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

}
