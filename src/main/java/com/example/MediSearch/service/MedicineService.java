package com.example.MediSearch.service;

import com.example.MediSearch.payload.MedicineDTO;
import com.example.MediSearch.payload.MedicineResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


public interface MedicineService {

    MedicineDTO addMedicine(Long shopId, MedicineDTO medicineDTO);

    MedicineResponse getAllMedicines(Integer pageNumber,
                                     Integer pageSize,
                                     String sortBy,
                                     String sortOrder,
                                     String keyword);

    MedicineResponse getAllMedicinesForSeller(Long shopId,
                                              Integer pageNumber,
                                              Integer pageSize,
                                              String sortBy,
                                              String sortOrder);

    List<MedicineSearchResponse> searchNearbyMedicine(
            String keyword,
            Double userLat,
            Double userLng,
            Double radiusKm
    );

    MedicineDTO updateProduct(Long shopId,
                              Long medicineId,
                              MedicineDTO medicineDTO);

    MedicineDTO deleteMedicine(Long shopId,
                               Long medicineId);

    MedicineDTO updateMedicineImage(Long shopId,
                                    Long medicineId,
                                    MultipartFile image) throws IOException;
}