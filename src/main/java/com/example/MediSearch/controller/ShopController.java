package com.example.MediSearch.controller;

import com.example.MediSearch.payload.ShopDTO;
import com.example.MediSearch.security.response.MessageResponse;
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
    public ResponseEntity<?> createShop(
            @Valid @RequestBody ShopDTO shopDTO) {

        ShopDTO savedShop = shopService.createShop(shopDTO);

        return ResponseEntity.ok(
                new MessageResponse("Shop added successfully"));
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