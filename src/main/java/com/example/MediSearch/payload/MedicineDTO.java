package com.example.MediSearch.payload;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MedicineDTO {

//    private Long medicineId;
//    private String barcode;
    private String medicineName;
    private String description;
    private Integer quantity;
    @NotNull
    @Positive
    private Double price;


    @Min(0)
    @Max(100)
    private Double discount;
    private Double specialPrice;  //75
    private String image;

    // For user search display

    private String shopName;
    private String shopCity;
    private Double distance;

}
