package com.example.MediSearch.payload;

import lombok.Data;

@Data
public class ShopDTO {

    private Long shopId;
    private String shopName;
    private String buildingName;
    private String street;
    private String city;
    private String state;
    private String country;
    private String pincode;

    private Double latitude;
    private Double longitude;

    private Boolean isOpen;
}