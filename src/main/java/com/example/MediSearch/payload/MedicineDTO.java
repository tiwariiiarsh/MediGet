package com.example.MediSearch.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MedicineDTO {

    private Long medicineId;
//    private String barcode;
    private String medicineName;
    private String description;
    private Integer quantity;
    private Double price; //100
    private Double specialPrice;  //75
    private Double discount; //25
    private String image;

    // For user search display
    private Long shopId;
    private String shopName;
    private String shopCity;
    private Double distance;

}
