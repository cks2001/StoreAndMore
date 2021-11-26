package com.exampe.storeandmore.model;

import java.io.Serializable;
import java.util.ArrayList;

public class OrderTracker implements Serializable {

    String id;
    String user_id;
    String otp;
    String mobile;
    String order_note;
    String total;
    String delivery_charge;
    String tax_amount;
    String tax_percentage;
    String wallet_balance;
    String discount;
    String promo_code;
    String promo_discount;
    String final_total;
    String payment_method;
    String address;
    String latitude;
    String longitude;
    String delivery_time;
    String seller_notes;
    String local_pickup;
    String pickup_time;
    String active_status;
    String date_added;
    String order_from;
    String total_attachment;
    String user_name;
    String discount_rupees;
    String bank_transfer_message;
    String bank_transfer_status;
    ArrayList<String> status_name,status_time;
    ArrayList<OrderItem> items;
    ArrayList<Attachment> attachment;

    public String getOrder_note() {
        return order_note;
    }

    public String getPromo_code() {
        return promo_code;
    }

    public String getBank_transfer_message() {
        return bank_transfer_message;
    }

    public String getBank_transfer_status() {
        return bank_transfer_status;
    }

    public ArrayList<OrderItem> getItems() {
        return items;
    }

    public String getId() {
        return id;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getOtp() {
        return otp;
    }

    public String getMobile() {
        return mobile;
    }

    public String getTotal() {
        return total;
    }

    public String getDelivery_charge() {
        return delivery_charge;
    }

    public String getTax_amount() {
        return tax_amount;
    }

    public String getTax_percentage() {
        return tax_percentage;
    }

    public String getWallet_balance() {
        return wallet_balance;
    }

    public String getDiscount() {
        return discount;
    }

    public String getPromo_discount() {
        return promo_discount;
    }

    public String getFinal_total() {
        return final_total;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public String getAddress() {
        return address;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getDelivery_time() {
        return delivery_time;
    }

    public String getSeller_notes() {
        return seller_notes;
    }

    public String getLocal_pickup() {
        return local_pickup;
    }

    public String getPickup_time() {
        return pickup_time;
    }

    public String getActive_status() {
        return active_status;
    }

    public String getDate_added() {
        return date_added;
    }

    public String getOrder_from() {
        return order_from;
    }

    public ArrayList<Attachment> getAttachment() {
        return attachment;
    }

    public String getTotal_attachment() {
        return total_attachment;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getDiscount_rupees() {
        return discount_rupees;
    }

    public ArrayList<String> getStatus_name() {
        return status_name;
    }

    public ArrayList<String> getStatus_time() {
        return status_time;
    }
}