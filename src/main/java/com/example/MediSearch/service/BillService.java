package com.example.MediSearch.service;

import com.example.MediSearch.payload.BillResponseDTO;
import com.example.MediSearch.payload.CartItemDTO;

import java.util.List;

public interface BillService {

    BillResponseDTO generateBill(
            Long shopId,
            List<CartItemDTO> cartItems);

    String getBestSelling(Long shopId);

    String getLeastSelling(Long shopId);
}