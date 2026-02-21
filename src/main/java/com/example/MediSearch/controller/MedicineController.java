package com.example.MediSearch.controller;


import com.example.MediSearch.config.AppConstants;
import com.example.MediSearch.payload.MedicineDTO;
import com.example.MediSearch.payload.MedicineResponse;
import com.example.MediSearch.service.MedicineService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MedicineController {


    @Autowired
    private MedicineService medicineService;

    @PostMapping("/seller/medicine")
    public ResponseEntity<MedicineDTO> addMedicineBySeller(@Valid  @RequestBody MedicineDTO medicineDTO){
        MedicineDTO medicine = medicineService.addMedicine(medicineDTO);
        return new ResponseEntity<MedicineDTO>(medicine, HttpStatus.OK);
    }


    @GetMapping("/public/medicines")
    public ResponseEntity<MedicineResponse> getAllMedicine(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name="pageNumber",defaultValue = AppConstants.PAGE_NUMBER,required = false) Integer pageNumber,
            @RequestParam(name = "pageSize",defaultValue = AppConstants.PAGE_SIZE,required = false) Integer pageSize,
            @RequestParam(name = "sortBy",defaultValue = AppConstants.SORT_MEDICINE_BY,required = false) String sortBy,
            @RequestParam(name = "sortOrder",defaultValue = AppConstants.SORT_DIR,required = false) String sortOrder
    ){
       MedicineResponse medicineResponse =  medicineService.getAllMedicines(pageNumber,pageSize,sortBy,sortOrder,keyword);
        return new ResponseEntity<>(medicineResponse,HttpStatus.OK );
    }

    @GetMapping("/seller/medicines")
    public ResponseEntity<MedicineResponse> getAllMedicinesForSeller(
            @RequestParam(name="pageNumber",defaultValue = AppConstants.PAGE_NUMBER,required = false) Integer pageNumber,
            @RequestParam(name = "pageSize",defaultValue = AppConstants.PAGE_SIZE,required = false) Integer pageSize,
            @RequestParam(name = "sortBy",defaultValue = AppConstants.SORT_MEDICINE_BY,required = false) String sortBy,
            @RequestParam(name = "sortOrder",defaultValue = AppConstants.SORT_DIR,required = false) String sortOrder
    ){
        MedicineResponse medicineResponse =  medicineService.getAllMedicinesForSeller(pageNumber,pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(medicineResponse,HttpStatus.OK );
    }


    @GetMapping("/public/medicines/{keyword}/keyword")
    public  ResponseEntity<MedicineResponse>getProductByKeyword(@PathVariable String keyword,
                                                               @RequestParam(name="pageNumber",defaultValue = AppConstants.PAGE_NUMBER,required = false) Integer pageNumber,
                                                               @RequestParam(name = "pageSize",defaultValue = AppConstants.PAGE_SIZE,required = false) Integer pageSize,
                                                               @RequestParam(name = "sortBy",defaultValue = AppConstants.SORT_MEDICINE_BY,required = false) String sortBy,
                                                               @RequestParam(name = "sortOrder",defaultValue = AppConstants.SORT_DIR,required = false) String sortOrder)
    {
        MedicineResponse medicineResponse = medicineService.searchMedicineByKeyword(keyword,pageNumber,pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(medicineResponse,HttpStatus.FOUND);
    }

    @PutMapping("/seller/medicine/{medicineId}")
    public ResponseEntity<MedicineDTO>updateMedicine(@Valid @RequestBody MedicineDTO medicineDTO,
                                                   @PathVariable Long medicineId){
        MedicineDTO updateMedicineDTO = medicineService.updateProduct(medicineId,medicineDTO);
        return new ResponseEntity<>(updateMedicineDTO,HttpStatus.OK);
    }

    @DeleteMapping("/seller/medicine/{medicineId}")
    public  ResponseEntity<MedicineDTO> deleteProduct(@PathVariable Long medicineId){
        MedicineDTO deleteMedicine = medicineService.deleteMedicine(medicineId);
        return new ResponseEntity<>(deleteMedicine,HttpStatus.OK);
    }

}
