package com.example.MediSearch.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shops")
@Getter
@Setter
@NoArgsConstructor
public class Shop {

    @Id
    private Long shopId;  // Same as seller userId

    private String shopName;

    @NotBlank
    @Size(min = 5,message = "street name must be atleast 5 characters")
    public  String street;

    @NotBlank
    @Size(min = 5,message = "building name must be atleast 5 characters")
    public String buildingName;

    @NotBlank
    @Size(min = 4,message = "city name must be atleast 4 characters")
    public String city;

    @NotBlank
    @Size(min = 2,message = "state name must be atleast 2 characters")
    public String state;

    @NotBlank
    @Size(min = 2,message = "country name must be atleast 2 characters")
    public String country;

    @NotBlank
    @Size(min = 5,message = "Pincode  must be atleast 5 characters")
    public String pincode;

    // ================= UNIQUE SELLER =================
    @OneToOne
    @MapsId
    @JoinColumn(name = "shop_id")
    private User seller;

    // ================= ONE SHOP MANY MEDICINES =================
    @OneToMany(mappedBy = "shop",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Medicine> medicines = new ArrayList<>();
}