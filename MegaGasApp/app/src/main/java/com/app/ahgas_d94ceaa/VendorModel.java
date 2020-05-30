package com.app.ahgas_d94ceaa;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VendorModel {

    @Expose
    @SerializedName("product_container_price")
    private String product_container_price;
    @Expose
    @SerializedName("product_container_flag")
    private String product_container_flag;
    @Expose
    @SerializedName("product_special_price")
    private String product_special_price;
    @Expose
    @SerializedName("product_short_description")
    private String product_short_description;
    @Expose
    @SerializedName("product_image_url")
    private String product_image_url;
    @Expose
    @SerializedName("product_hash_id")
    private String product_hash_id;
    @Expose
    @SerializedName("partner_zip_code")
    private String partner_zip_code;
    @Expose
    @SerializedName("partner_state")
    private String partner_state;
    @Expose
    @SerializedName("partner_city")
    private String partner_city;
    @Expose
    @SerializedName("partner_neighborhood")
    private String partner_neighborhood;
    @Expose
    @SerializedName("partner_street_number")
    private String partner_street_number;
    @Expose
    @SerializedName("partner_address")
    private String partner_address;
    @Expose
    @SerializedName("partner_name")
    private String partner_name;
    @Expose
    @SerializedName("partner_hash_id")
    private String partner_hash_id;
    @Expose
    @SerializedName("partner_phone")
    private String phone;

    private double rating;

    private int open; //0=loading,1=open,2=closed

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getPhone() {
        if(phone == null)
            phone = "";
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProduct_container_price() {
        return product_container_price;
    }

    public void setProduct_container_price(String product_container_price) {
        this.product_container_price = product_container_price;
    }

    public String getProduct_container_flag() {
        return product_container_flag;
    }

    public void setProduct_container_flag(String product_container_flag) {
        this.product_container_flag = product_container_flag;
    }

    public String getProduct_special_price() {
        return product_special_price;
    }

    public void setProduct_special_price(String product_special_price) {
        this.product_special_price = product_special_price;
    }

    public String getProduct_short_description() {
        return product_short_description;
    }

    public void setProduct_short_description(String product_short_description) {
        this.product_short_description = product_short_description;
    }

    public String getProduct_image_url() {
        return product_image_url;
    }

    public void setProduct_image_url(String product_image_url) {
        this.product_image_url = product_image_url;
    }

    public String getProduct_hash_id() {
        return product_hash_id;
    }

    public void setProduct_hash_id(String product_hash_id) {
        this.product_hash_id = product_hash_id;
    }

    public String getPartner_zip_code() {
        return partner_zip_code;
    }

    public void setPartner_zip_code(String partner_zip_code) {
        this.partner_zip_code = partner_zip_code;
    }

    public String getPartner_state() {
        return partner_state;
    }

    public void setPartner_state(String partner_state) {
        this.partner_state = partner_state;
    }

    public String getPartner_city() {
        return partner_city;
    }

    public void setPartner_city(String partner_city) {
        this.partner_city = partner_city;
    }

    public String getPartner_neighborhood() {
        return partner_neighborhood;
    }

    public void setPartner_neighborhood(String partner_neighborhood) {
        this.partner_neighborhood = partner_neighborhood;
    }

    public String getPartner_street_number() {
        return partner_street_number;
    }

    public void setPartner_street_number(String partner_street_number) {
        this.partner_street_number = partner_street_number;
    }

    public String getPartner_address() {
        return partner_address;
    }

    public void setPartner_address(String partner_address) {
        this.partner_address = partner_address;
    }

    public String getPartner_name() {
        return partner_name;
    }

    public void setPartner_name(String partner_name) {
        this.partner_name = partner_name;
    }

    public String getPartner_hash_id() {
        return partner_hash_id;
    }

    public void setPartner_hash_id(String partner_hash_id) {
        this.partner_hash_id = partner_hash_id;
    }
}
