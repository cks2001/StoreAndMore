package com.exampe.storeandmore.model;

import java.io.Serializable;

public class Review implements Serializable {
    String id;
    String product_id;
    String user_id;
    String review;
    String status;
    String date_added;
    String ratings;
    String username;
    String user_profile;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate_added() {
        return date_added;
    }

    public String getRatings() {
        return ratings;
    }

    public String getUsername() {
        return username;
    }

    public String getUser_profile() {
        return user_profile;
    }
}
