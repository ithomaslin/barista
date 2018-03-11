package com.richify.goobucks.model;

/**
 * Created by thomaslin on 03/03/2018.
 *
 * User model
 */

public class User {

    public String name, email;
    public Integer orderNumber;
    public Boolean isBarista;

    public User() {

    }

    public User(String name, String email, Integer orderNumber, Boolean isBarista) {
        this.name = name;
        this.email = email;
        this.orderNumber = orderNumber;
        this.isBarista = isBarista;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public Boolean getIsBarista() {
        return isBarista;
    }

}
