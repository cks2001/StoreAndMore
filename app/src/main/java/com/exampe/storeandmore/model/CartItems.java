package com.exampe.storeandmore.model;

import java.io.Serializable;


public class CartItems implements Serializable {

    String id;
    String is_cod_allowed;
    String product_id;
    String type;
    String measurement;
    String price;
    String discounted_price;
    String serve_for;
    String stock;
    String name;
    String image;
    String unit;
    String tax_percentage;
    String tax_title;

    public String getId() {
        return id;
    }

    public String getIs_cod_allowed() {
        return is_cod_allowed;
    }

    public String getProduct_id() {
        return product_id;
    }

    public String getType() {
        return type;
    }

    public String getMeasurement() {
        return measurement;
    }

    public String getPrice() {
        return price;
    }

    public String getDiscounted_price() {
        return discounted_price;
    }

    public String getServe_for() {
        return serve_for;
    }

    public String getStock() {
        return stock;
    }


    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getUnit() {
        return unit;
    }

    public String getTax_percentage() {
        return tax_percentage;
    }

    public String getTax_title() {
        return tax_title;
    }
}
