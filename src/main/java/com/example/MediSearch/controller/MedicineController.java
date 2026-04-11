package com.example.MediSearch.controller;

import com.example.MediSearch.Utils.DistanceUtil;
import com.example.MediSearch.config.AppConstants;
import com.example.MediSearch.exceptions.ResourceNotFoundException;
import com.example.MediSearch.model.Medicine;
import com.example.MediSearch.payload.MedicineDTO;
import com.example.MediSearch.payload.MedicineResponse;
import com.example.MediSearch.payload.ShopDTO;
import com.example.MediSearch.repository.MedicineRepository;
import com.example.MediSearch.service.MedicineService;
import com.example.MediSearch.service.ShopService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MedicineController {
    
    @Autowired
    private MedicineRepository medicineRepository;

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


    

    // ================= ALTERNATIVE MEDICINES =================
    @GetMapping("/public/medicines/{medicineId}/alternatives")
    public ResponseEntity<List<MedicineDTO>> getAlternatives(
            @PathVariable Long medicineId,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLng) {

        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", medicineId));

        // Find medicines with similar name (generic alternatives)
        String keyword = medicine.getMedicineName()
                .split(" ")[0]; // Use first word as generic search

        List<Medicine> alternatives = medicineRepository
                .findAlternativesByGenericName(keyword, PageRequest.of(0, 6))
                .stream()
                .filter(m -> !m.getMedicineId().equals(medicineId))
                .collect(Collectors.toList());

        List<MedicineDTO> dtos = alternatives.stream().map(m -> {
                    MedicineDTO dto = convertToDTO(m);
                    if (userLat != null && userLng != null
                            && m.getShop() != null
                            && m.getShop().getLatitude() != null) {
                        double dist = DistanceUtil.calculateDistance(
                                userLat, userLng,
                                m.getShop().getLatitude(),
                                m.getShop().getLongitude());
                        dto.setDistance(dist);
                    }
                    return dto;
                }).sorted(Comparator.comparingLong(d ->
                        d.getSalesCount() == null ? 0L : -d.getSalesCount()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    private MedicineDTO convertToDTO(Medicine medicine) {

        MedicineDTO dto = new MedicineDTO();

        dto.setMedicineId(medicine.getMedicineId());
        dto.setMedicineName(medicine.getMedicineName());
        dto.setDescription(medicine.getDescription());
        dto.setPrice(medicine.getPrice());
        dto.setQuantity(medicine.getQuantity());
        dto.setDiscount(medicine.getDiscount());
        dto.setSpecialPrice(medicine.getSpecialPrice());
        dto.setImage(medicine.getImage());

        // Expiry & Sales
        dto.setExpiryDate(medicine.getExpiryDate());
        dto.setSalesCount(medicine.getSalesCount());

        // Shop Details
        if (medicine.getShop() != null) {
            dto.setShopId(medicine.getShop().getShopId());
            dto.setShopName(medicine.getShop().getShopName());
            dto.setShopCity(medicine.getShop().getCity());
        }

        return dto;
    }


}