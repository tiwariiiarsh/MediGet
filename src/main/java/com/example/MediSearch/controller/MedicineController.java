package com.example.MediSearch.controller;

import com.example.MediSearch.config.AppConstants;
import com.example.MediSearch.payload.MedicineDTO;
import com.example.MediSearch.payload.MedicineResponse;
import com.example.MediSearch.payload.ShopDTO;
import com.example.MediSearch.service.MedicineService;
import com.example.MediSearch.service.ShopService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private ShopService shopService;

    // ================= ADD MEDICINE =================
    @PostMapping("/seller/shop/{shopId}/medicine")
    public ResponseEntity<MedicineDTO> addMedicine(
            @PathVariable Long shopId,
            @Valid @RequestBody MedicineDTO medicineDTO) {

        return ResponseEntity.ok(
                medicineService.addMedicine(shopId, medicineDTO)
        );
    }

    // ================= PUBLIC GET ALL =================
    @GetMapping("/public/medicines")
    public ResponseEntity<MedicineResponse> getAllMedicines(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_MEDICINE_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR) String sortOrder
    ) {

        return ResponseEntity.ok(
                medicineService.getAllMedicines(
                        pageNumber,
                        pageSize,
                        sortBy,
                        sortOrder,
                        keyword)
        );
    }

    // ================= SELLER MEDICINES =================
    @GetMapping("/seller/shop/{shopId}/medicines")
    public ResponseEntity<MedicineResponse> getSellerMedicines(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_MEDICINE_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR) String sortOrder
    ) {

        return ResponseEntity.ok(
                medicineService.getAllMedicinesForSeller(
                        shopId,
                        pageNumber,
                        pageSize,
                        sortBy,
                        sortOrder)
        );
    }

    // ================= NEARBY MEDICINE SEARCH =================
    @GetMapping("/public/medicines/nearby")
    public ResponseEntity<List<MedicineDTO>> searchNearbyMedicine(
            @RequestParam String keyword,
            @RequestParam Double userLat,
            @RequestParam Double userLng,
            @RequestParam(required = false) Double radiusKm
    ) {
        return ResponseEntity.ok(
                medicineService.searchNearbyMedicine(
                        keyword,
                        userLat,
                        userLng,
                        radiusKm
                )
        );
    }

    // ================= UPDATE MEDICINE =================
    @PutMapping("/seller/shop/{shopId}/medicine/{medicineId}")
    public ResponseEntity<MedicineDTO> updateMedicine(
            @PathVariable Long shopId,
            @PathVariable Long medicineId,
            @Valid @RequestBody MedicineDTO medicineDTO) {

        return ResponseEntity.ok(
                medicineService.updateProduct(
                        shopId,
                        medicineId,
                        medicineDTO)
        );
    }

    // ================= DELETE MEDICINE =================
    @DeleteMapping("/seller/shop/{shopId}/medicine/{medicineId}")
    public ResponseEntity<MedicineDTO> deleteMedicine(
            @PathVariable Long shopId,
            @PathVariable Long medicineId) {

        return ResponseEntity.ok(
                medicineService.deleteMedicine(
                        shopId,
                        medicineId)
        );
    }

    // ================= UPDATE IMAGE =================
    @PutMapping("/seller/shop/{shopId}/medicine/{medicineId}/image")
    public ResponseEntity<MedicineDTO> updateMedicineImage(
            @PathVariable Long shopId,
            @PathVariable Long medicineId,
            @RequestParam("image") MultipartFile image)
            throws IOException {

        return ResponseEntity.ok(
                medicineService.updateMedicineImage(
                        shopId,
                        medicineId,
                        image)
        );
    }


}