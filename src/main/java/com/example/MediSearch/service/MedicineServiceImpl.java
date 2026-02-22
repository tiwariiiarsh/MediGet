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
    private MedicineRepository medicineRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    // ================= ADD MEDICINE =================

    @Override
    public MedicineDTO addMedicine(Long shopId,
                                   MedicineDTO medicineDTO) {

        User seller = authUtils.loggedInUser();

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shop","shopId",shopId));

        if (!shop.getSeller().getUserId()
                .equals(seller.getUserId())) {
            throw new ApiException("Not authorized to add medicine");
        }

        Medicine medicine =
                modelMapper.map(medicineDTO, Medicine.class);

        medicine.setShop(shop);
        medicine.setImage("default.png");

        double specialPrice =
                medicine.getPrice()
                        - ((medicine.getDiscount() * 0.01)
                        * medicine.getPrice());

        medicine.setSpecialPrice(specialPrice);

        Medicine saved =
                medicineRepository.save(medicine);

        return modelMapper.map(saved, MedicineDTO.class);
    }

    // ================= PUBLIC GET ALL =================

    @Override
    public MedicineResponse getAllMedicines(Integer pageNumber,
                                            Integer pageSize,
                                            String sortBy,
                                            String sortOrder,
                                            String keyword) {

        if (pageNumber == null) pageNumber = 0;
        if (pageSize == null) pageSize = 5;
        if (sortBy == null) sortBy = "medicineId";
        if (sortOrder == null) sortOrder = "asc";

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable =
                PageRequest.of(pageNumber, pageSize, sort);

        Page<Medicine> pageData;

        if (keyword != null && !keyword.isBlank()) {
            pageData = medicineRepository
                    .findByMedicineNameContainingIgnoreCase(
                            keyword, pageable);
        } else {
            pageData = medicineRepository.findAll(pageable);
        }

        List<MedicineDTO> dtos =
                pageData.getContent()
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
    public MedicineResponse getAllMedicinesForSeller(Long shopId,
                                                     Integer pageNumber,
                                                     Integer pageSize,
                                                     String sortBy,
                                                     String sortOrder) {

        User seller = authUtils.loggedInUser();

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shop","shopId",shopId));

        if (!shop.getSeller().getUserId()
                .equals(seller.getUserId())) {
            throw new ApiException("Not authorized");
        }

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable =
                PageRequest.of(pageNumber, pageSize, sort);

        Page<Medicine> pageData =
                medicineRepository.findByShop(shop, pageable);

        List<MedicineDTO> dtos =
                pageData.getContent()
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
    public MedicineDTO updateProduct(Long shopId,
                                     Long medicineId,
                                     MedicineDTO medicineDTO) {

        User seller = authUtils.loggedInUser();

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shop","shopId",shopId));

        if (!shop.getSeller().getUserId()
                .equals(seller.getUserId())) {
            throw new ApiException("Not authorized");
        }

        Medicine medicine =
                medicineRepository.findById(medicineId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Medicine",
                                        "medicineId", medicineId));

        if (!medicine.getShop().getShopId()
                .equals(shopId)) {
            throw new ApiException("Medicine does not belong to this shop");
        }

        modelMapper.map(medicineDTO, medicine);

        double specialPrice =
                medicine.getPrice()
                        - ((medicine.getDiscount() * 0.01)
                        * medicine.getPrice());

        medicine.setSpecialPrice(specialPrice);

        Medicine saved =
                medicineRepository.save(medicine);

        return modelMapper.map(saved, MedicineDTO.class);
    }

    // ================= DELETE =================

    @Override
    public MedicineDTO deleteMedicine(Long shopId,
                                      Long medicineId) {

        Medicine medicine =
                medicineRepository.findById(medicineId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Medicine",
                                        "medicineId", medicineId));

        if (!medicine.getShop().getShopId()
                .equals(shopId)) {
            throw new ApiException("Not authorized");
        }

        medicineRepository.delete(medicine);

        return modelMapper.map(medicine, MedicineDTO.class);
    }

    // ================= IMAGE UPDATE =================

    @Override
    public MedicineDTO updateMedicineImage(Long shopId,
                                           Long medicineId,
                                           MultipartFile image)
            throws IOException {

        Medicine medicine =
                medicineRepository.findById(medicineId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Medicine",
                                        "medicineId", medicineId));

        if (!medicine.getShop().getShopId()
                .equals(shopId)) {
            throw new ApiException("Not authorized");
        }

        String fileName =
                fileService.uploadImage("images/", image);

        medicine.setImage(fileName);

        Medicine saved =
                medicineRepository.save(medicine);

        return modelMapper.map(saved, MedicineDTO.class);
    }
}