package com.example.MediSearch.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) //incase of IDENTITY-->> database dependent(used in Mysql) --> auto increement,, AUTO--> Hibernate dependent ->use in anyother multiple db
    private Long medicineId;

//    private String barcode;

    @NotBlank
    @Size(min = 3,message = "Medicine name must contain atleast 3 characters")
    private String medicineName;

    @NotBlank
    @Size(min = 6,message = "Medicine description must contain atleast 6 characters")
    private String description;

    private Integer quantity;
    private Double price; //100
    private Double specialPrice;  //75
    private Double discount; //25
    private String image;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User user;

    // ================= MANY MEDICINES → ONE SHOP =================
    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;
}
