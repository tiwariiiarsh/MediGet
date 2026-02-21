package com.example.MediSearch.service;

import com.example.MediSearch.payload.MedicineDTO;
import com.example.MediSearch.payload.MedicineResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MedicineService {
    MedicineDTO addMedicine(MedicineDTO medicineDTO);

    MedicineResponse getAllMedicines(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String keyword);

    MedicineResponse getAllMedicinesForSeller(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    MedicineResponse searchMedicineByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    MedicineDTO updateProduct(Long medicineId, MedicineDTO medicineDTO);

    MedicineDTO deleteMedicine(Long medicineId);

    MedicineDTO updateMedicineImage(Long medicineId, MultipartFile image) throws IOException;
}
