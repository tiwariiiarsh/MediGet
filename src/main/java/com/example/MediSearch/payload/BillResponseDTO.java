package com.example.MediSearch.payload;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BillResponseDTO {

    private Long billId;
    private String shopName;
    private LocalDate billDate;
    private Double totalAmount;
    private List<CartItemDTO> items;
}