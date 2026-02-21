package com.example.MediSearch.service;

import com.example.MediSearch.payload.MedicineDTO;
import com.example.MediSearch.payload.MedicineResponse;

public interface MedicineService {
    MedicineDTO addMedicine(MedicineDTO medicineDTO);

    MedicineResponse getAllMedicines(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String keyword);

    MedicineResponse getAllMedicinesForSeller(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
}
