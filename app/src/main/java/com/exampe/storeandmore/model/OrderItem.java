package com.exampe.storeandmore.model;

import java.io.Serializable;

public class OrderItem implements Serializable {
    String id, user_id, order_id, product_variant_id, quantity, price, discounted_price, tax_amount, tax_percentage, discount, sub_total, deliver_by, active_status, date_added, product_id, variant_id, rate, review, name, image, manufacturer, made_in, return_status, cancelable_status, till_status, measurement, unit;

    public void setRate(String rate) {
        this.rate = rate;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public void setReview_status(boolean review_status) {
        this.review_status = review_status;
    }

    boolean review_status, applied_for_return;

    public String getId() {
        return id;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public String getProduct_variant_id() {
        return product_variant_id;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getPrice() {
        return price;
    }

    public String getDiscounted_price() {
        return discounted_price;
    }

    public String getTax_amount() {
        return tax_amount;
    }

    public String getTax_percentage() {
        return tax_percentage;
    }

    public String getDiscount() {
        return discount;
    }

    public String getSub_total() {
        return sub_total;
    }

    public String getDeliver_by() {
        return deliver_by;
    }

    public String getActive_status() {
        return active_status;
    }

    public String getDate_added() {
        return date_added;
    }

    public String getProduct_id() {
        return product_id;
    }

    public String getVariant_id() {
        return variant_id;
    }

    public String getRate() {
        return rate;
    }

    public String getReview() {
        return review;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getMade_in() {
        return made_in;
    }

    public String getReturn_status() {
        return return_status;
    }

    public String getCancelable_status() {
        return cancelable_status;
    }

    public String getTill_status() {
        return till_status;
    }

    public String getMeasurement() {
        return measurement;
    }

    public String getUnit() {
        return unit;
    }

    public boolean isReview_status() {
        return review_status;
    }

    public boolean isApplied_for_return() {
        return applied_for_return;
    }
}