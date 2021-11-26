package com.exampe.storeandmore.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Cart implements Serializable {

    String id;
    String user_id;
    String product_id;
    String product_variant_id;
    String qty;
    ArrayList<CartItems> item;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getProduct_variant_id() {
        return product_variant_id;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public ArrayList<CartItems> getItems() {
        return item;
    }
}
