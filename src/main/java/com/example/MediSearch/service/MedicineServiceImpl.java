package com.example.MediSearch.service;

import com.example.MediSearch.Utils.AuthUtils;
import com.example.MediSearch.exceptions.ApiException;
import com.example.MediSearch.exceptions.ResourceNotFoundException;
import com.example.MediSearch.model.Medicine;
import com.example.MediSearch.model.User;
import com.example.MediSearch.payload.MedicineDTO;
import com.example.MediSearch.payload.MedicineResponse;
import com.example.MediSearch.repository.MedicineRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class MedicineServiceImpl implements MedicineService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private FileService fileService;

    @Value("${product.image}")
    private String path;

    @Value("${image.base.url}")
    private String imageBaseUrl;

    // ================= ADD MEDICINE =================

    @Override
    public MedicineDTO addMedicine(MedicineDTO medicineDTO) {

        Medicine medicine = modelMapper.map(medicineDTO, Medicine.class);

        medicine.setUser(authUtils.loggedInUser());
        medicine.setImage("default.png");

        double specialPrice = medicine.getPrice() -
                ((medicine.getDiscount() * 0.01) * medicine.getPrice());

        medicine.setSpecialPrice(specialPrice);

        Medicine savedMedicine = medicineRepository.save(medicine);

        return modelMapper.map(savedMedicine, MedicineDTO.class);
    }

    // ================= GET ALL MEDICINES =================

    @Override
    public MedicineResponse getAllMedicines(
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder,
            String keyword) {

        if (pageNumber == null) pageNumber = 0;
        if (pageSize == null) pageSize = 5;
        if (sortBy == null || sortBy.isBlank()) sortBy = "medicineId";
        if (sortOrder == null || sortOrder.isBlank()) sortOrder = "asc";

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Specification<Medicine> spec = Specification.where(null);

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("medicineName")),
                            "%" + keyword.toLowerCase() + "%"
                    )
            );
        }

        Page<Medicine> pageData =
                medicineRepository.findAll(spec, pageable);

        List<MedicineDTO> dtos = pageData.getContent()
                .stream()
                .map(m -> modelMapper.map(m, MedicineDTO.class))
                .toList();

        MedicineResponse response = new MedicineResponse();
        response.setContent(dtos);
        response.setPageNumber(pageData.getNumber());
        response.setPageSize(pageData.getSize());
        response.setTotalElements(pageData.getTotalElements());
        response.setTotalPages(pageData.getTotalPages());
        response.setLastPage(pageData.isLast());

        return response;
    }

    // ================= SELLER MEDICINES =================

    @Override
    public MedicineResponse getAllMedicinesForSeller(
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder) {

        if (pageNumber == null) pageNumber = 0;
        if (pageSize == null) pageSize = 5;
        if (sortBy == null || sortBy.isBlank()) sortBy = "medicineId";
        if (sortOrder == null || sortOrder.isBlank()) sortOrder = "asc";

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        User user = authUtils.loggedInUser();

        Page<Medicine> pageData =
                medicineRepository.findByUser(user, pageable);

        List<MedicineDTO> dtos = pageData.getContent()
                .stream()
                .map(m -> modelMapper.map(m, MedicineDTO.class))
                .toList();

        MedicineResponse response = new MedicineResponse();
        response.setContent(dtos);
        response.setPageNumber(pageData.getNumber());
        response.setPageSize(pageData.getSize());
        response.setTotalElements(pageData.getTotalElements());
        response.setTotalPages(pageData.getTotalPages());
        response.setLastPage(pageData.isLast());

        return response;
    }

    // ================= SEARCH =================

    @Override
    public MedicineResponse searchMedicineByKeyword(
            String keyword,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder) {

        if (pageNumber == null) pageNumber = 0;
        if (pageSize == null) pageSize = 5;
        if (sortBy == null || sortBy.isBlank()) sortBy = "medicineId";
        if (sortOrder == null || sortOrder.isBlank()) sortOrder = "asc";

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Medicine> pageData =
                medicineRepository
                        .findByMedicineNameContainingIgnoreCase(
                                keyword, pageable);

        if (pageData.isEmpty()) {
            throw new ApiException(
                    "Medicine not found with keyword: " + keyword);
        }

        List<MedicineDTO> dtos = pageData.getContent()
                .stream()
                .map(m -> modelMapper.map(m, MedicineDTO.class))
                .toList();

        MedicineResponse response = new MedicineResponse();
        response.setContent(dtos);
        response.setPageNumber(pageData.getNumber());
        response.setPageSize(pageData.getSize());
        response.setTotalElements(pageData.getTotalElements());
        response.setTotalPages(pageData.getTotalPages());
        response.setLastPage(pageData.isLast());

        return response;
    }

    // ================= UPDATE =================

    @Override
    public MedicineDTO updateProduct(Long medicineId,
                                     MedicineDTO medicineDTO) {

        Medicine medicineFromDB = medicineRepository.findById(medicineId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Medicine",
                                "medicineId",
                                medicineId));

        modelMapper.map(medicineDTO, medicineFromDB);

        double specialPrice =
                medicineFromDB.getPrice() -
                        ((medicineFromDB.getDiscount() * 0.01)
                                * medicineFromDB.getPrice());

        medicineFromDB.setSpecialPrice(specialPrice);

        Medicine savedMedicine =
                medicineRepository.save(medicineFromDB);

        return modelMapper.map(savedMedicine, MedicineDTO.class);
    }

    // ================= DELETE =================

    @Override
    public MedicineDTO deleteMedicine(Long medicineId) {

        Medicine medicineFromDB = medicineRepository.findById(medicineId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Medicine",
                                "medicineId",
                                medicineId));

        medicineRepository.delete(medicineFromDB);

        return modelMapper.map(medicineFromDB,
                MedicineDTO.class);
    }

    @Override
    public MedicineDTO updateMedicineImage(Long medicineId, MultipartFile image) throws IOException {
//        Get medicine from DB'
        Medicine medicineFromDB = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("medicine","medicineId",medicineId));
//        Upload image to server
//        Get file name of uploaded image
        String path = "images/";
        String fileName =fileService.uploadImage(path,image);


//        updating the new file name to the medicine
        medicineFromDB.setImage(fileName);
//        save the updated medicine
        Medicine updatedmedicine = medicineRepository.save(medicineFromDB);
//        return DTO after mapping medicine to DTO
        return  modelMapper.map(updatedmedicine,MedicineDTO.class);
    }
}