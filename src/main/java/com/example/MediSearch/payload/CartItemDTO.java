package com.example.MediSearch.payload;

import lombok.Data;

@Data
public class CartItemDTO {

    private Long medicineId;
    private Integer quantity;
}