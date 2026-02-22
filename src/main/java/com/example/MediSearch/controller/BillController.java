package com.example.MediSearch.controller;

import com.example.MediSearch.payload.BillResponseDTO;
import com.example.MediSearch.payload.CartItemDTO;
import com.example.MediSearch.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller/shop")
public class BillController {

    @Autowired
    private BillService billService;

//    @PostMapping("/{shopId}/bill")
//    public ResponseEntity<?> generateBill(
//            @PathVariable Long shopId,
//            @RequestBody List<CartItemDTO> cartItems) {
//
//        return ResponseEntity.ok(
//                billService.generateBill(
//                        shopId,
//                        cartItems));
//    }
@PostMapping("/{shopId}/bill")
public ResponseEntity<BillResponseDTO> generateBill(
        @PathVariable Long shopId,
        @RequestBody List<CartItemDTO> cartItems) {

    return ResponseEntity.ok(
            billService.generateBill(shopId, cartItems)
    );
}

    @GetMapping("/{shopId}/analytics/best")
    public ResponseEntity<String> bestSelling(
            @PathVariable Long shopId) {

        return ResponseEntity.ok(
                billService.getBestSelling(shopId));
    }

    @GetMapping("/{shopId}/analytics/least")
    public ResponseEntity<String> leastSelling(
            @PathVariable Long shopId) {

        return ResponseEntity.ok(
                billService.getLeastSelling(shopId));
    }
}
