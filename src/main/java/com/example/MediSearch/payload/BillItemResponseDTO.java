package com.example.MediSearch.payload;

import lombok.Data;

@Data
public class BillItemResponseDTO {

    private String medicineName;
    private Integer quantity;
    private Double price;
    private Double totalPrice;
}

