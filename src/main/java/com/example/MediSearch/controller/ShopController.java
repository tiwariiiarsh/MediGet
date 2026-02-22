package com.example.MediSearch.controller;

import com.example.MediSearch.payload.ShopDTO;
import com.example.MediSearch.service.ShopService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    // ================= CREATE =================
    @PostMapping
    public ResponseEntity<ShopDTO> createShop(
            @Valid @RequestBody ShopDTO shopDTO) {

        return ResponseEntity.ok(
                shopService.createShop(shopDTO));
    }

    // ================= GET MY SHOP =================
    @GetMapping
    public ResponseEntity<ShopDTO> getMyShop() {

        return ResponseEntity.ok(
                shopService.getMyShop());
    }

    // ================= UPDATE =================
    @PutMapping
    public ResponseEntity<ShopDTO> updateShop(
            @Valid @RequestBody ShopDTO shopDTO) {

        return ResponseEntity.ok(
                shopService.updateShop(shopDTO));
    }

    // ================= DELETE =================
    @DeleteMapping
    public ResponseEntity<String> deleteShop() {

        return ResponseEntity.ok(
                shopService.deleteShop());
    }
}