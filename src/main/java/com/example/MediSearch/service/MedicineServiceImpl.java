package com.example.MediSearch.service;

import com.example.MediSearch.Utils.AuthUtils;
import com.example.MediSearch.Utils.DistanceUtil;
import com.example.MediSearch.exceptions.ApiException;
import com.example.MediSearch.exceptions.ResourceNotFoundException;
import com.example.MediSearch.model.Medicine;
import com.example.MediSearch.model.Shop;
import com.example.MediSearch.model.User;
import com.example.MediSearch.payload.MedicineDTO;
import com.example.MediSearch.payload.MedicineResponse;
import com.example.MediSearch.repository.MedicineRepository;
import com.example.MediSearch.repository.ShopRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
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

    // ================= ADD =================

    @Override
    public MedicineDTO addMedicine(Long shopId, MedicineDTO dto) {

        Shop shop = validateSeller(shopId);

        Medicine medicine = modelMapper.map(dto, Medicine.class);
        medicine.setShop(shop);
        medicine.setImage("default.png");

        double specialPrice =
                medicine.getPrice() -
                        ((medicine.getDiscount() * 0.01)
                                * medicine.getPrice());

        medicine.setSpecialPrice(specialPrice);

        Medicine saved = medicineRepository.save(medicine);

        return convertToDTO(saved);
    }

    // ================= PUBLIC LIST =================

    @Override
    public MedicineResponse getAllMedicines(Integer pageNumber,
                                            Integer pageSize,
                                            String sortBy,
                                            String sortOrder,
                                            String keyword) {

        Pageable pageable = getPageable(pageNumber, pageSize, sortBy, sortOrder);

        Page<Medicine> pageData =
                (keyword != null && !keyword.isBlank())
                        ? medicineRepository.findByMedicineNameContainingIgnoreCase(keyword, pageable)
                        : medicineRepository.findAll(pageable);

        List<MedicineDTO> dtos =
                pageData.getContent()
                        .stream()
                        .map(this::convertToDTO)
                        .toList();

        return new MedicineResponse(
                dtos,
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages(),
                pageData.isLast()
        );
    }

    // ================= SELLER LIST =================

    @Override
    public MedicineResponse getAllMedicinesForSeller(Long shopId,
                                                     Integer pageNumber,
                                                     Integer pageSize,
                                                     String sortBy,
                                                     String sortOrder) {

        Shop shop = validateSeller(shopId);

        Pageable pageable = getPageable(pageNumber, pageSize, sortBy, sortOrder);

        Page<Medicine> pageData =
                medicineRepository.findByShop(shop, pageable);

        List<MedicineDTO> dtos =
                pageData.getContent()
                        .stream()
                        .map(this::convertToDTO)
                        .toList();

        return new MedicineResponse(
                dtos,
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages(),
                pageData.isLast()
        );
    }

    // ================= NEARBY SEARCH =================

    @Override
    public List<MedicineDTO> searchNearbyMedicine(String keyword,
                                                  Double userLat,
                                                  Double userLng,
                                                  Double radiusKm) {

        List<Medicine> medicines =
                medicineRepository
                        .findByMedicineNameContainingIgnoreCase(keyword,
                                Pageable.unpaged())
                        .getContent();

        return medicines.stream()
                .filter(m -> m.getQuantity() > 0)
                .map(m -> {

                    double distance =
                            DistanceUtil.calculateDistance(
                                    userLat,
                                    userLng,
                                    m.getShop().getLatitude(),
                                    m.getShop().getLongitude());

                    if (distance <= radiusKm) {
                        MedicineDTO dto = convertToDTO(m);
                        dto.setDistance(distance);
                        return dto;
                    }
                    return null;
                })
                .filter(m -> m != null)
                .sorted(Comparator.comparingDouble(MedicineDTO::getDistance))
                .toList();
    }

    // ================= UPDATE =================

    @Override
    public MedicineDTO updateProduct(Long shopId,
                                     Long medicineId,
                                     MedicineDTO dto) {

        validateSeller(shopId);

        Medicine medicine =
                medicineRepository.findById(medicineId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Medicine", "medicineId", medicineId));

        if (!medicine.getShop().getShopId().equals(shopId))
            throw new ApiException("Not authorized");

        modelMapper.map(dto, medicine);

        double specialPrice =
                medicine.getPrice() -
                        ((medicine.getDiscount() * 0.01)
                                * medicine.getPrice());

        medicine.setSpecialPrice(specialPrice);

        return convertToDTO(medicineRepository.save(medicine));
    }

    // ================= DELETE =================

    @Override
    public MedicineDTO deleteMedicine(Long shopId,
                                      Long medicineId) {

        validateSeller(shopId);

        Medicine medicine =
                medicineRepository.findById(medicineId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Medicine", "medicineId", medicineId));

        if (!medicine.getShop().getShopId().equals(shopId))
            throw new ApiException("Not authorized");

        medicineRepository.delete(medicine);

        return convertToDTO(medicine);
    }

    // ================= IMAGE =================

    @Override
    public MedicineDTO updateMedicineImage(Long shopId,
                                           Long medicineId,
                                           MultipartFile image) throws IOException {

        validateSeller(shopId);

        Medicine medicine =
                medicineRepository.findById(medicineId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Medicine", "medicineId", medicineId));

        String fileName =
                fileService.uploadImage("images/", image);

        medicine.setImage(fileName);

        return convertToDTO(medicineRepository.save(medicine));
    }

    // ================= COMMON METHODS =================

    private Shop validateSeller(Long shopId) {

        User seller = authUtils.loggedInUser();

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shop", "shopId", shopId));

        if (!shop.getSeller().getUserId().equals(seller.getUserId()))
            throw new ApiException("Unauthorized");

        return shop;
    }

    private Pageable getPageable(Integer pageNumber,
                                 Integer pageSize,
                                 String sortBy,
                                 String sortOrder) {

        if (pageNumber == null) pageNumber = 0;
        if (pageSize == null) pageSize = 5;
        if (sortBy == null) sortBy = "medicineId";
        if (sortOrder == null) sortOrder = "asc";

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return PageRequest.of(pageNumber, pageSize, sort);
    }

    private MedicineDTO convertToDTO(Medicine medicine) {

        MedicineDTO dto = modelMapper.map(medicine, MedicineDTO.class);

        dto.setShopId(medicine.getShop().getShopId());
        dto.setShopName(medicine.getShop().getShopName());
        dto.setShopCity(medicine.getShop().getCity());

        return dto;
    }
}