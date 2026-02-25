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

        if (dto.getPrice() == null)
            throw new ApiException("Price must not be null");

        Double discount = dto.getDiscount() == null ? 0.0 : dto.getDiscount();

        if (discount < 0 || discount > 100)
            throw new ApiException("Discount must be between 0 and 100");

        Medicine medicine = modelMapper.map(dto, Medicine.class);

        medicine.setShop(shop);
        medicine.setImage("default.png");
        medicine.setDiscount(discount);

        double specialPrice =
                dto.getPrice() - ((discount / 100) * dto.getPrice());

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
                        ? medicineRepository
                        .findByMedicineNameContainingIgnoreCase(keyword, pageable)
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

        // Safety checks
        if (keyword == null || keyword.isBlank()
                || userLat == null || userLng == null) {
            return List.of();
        }

        List<Medicine> medicines =
                medicineRepository
                        .findByMedicineNameContainingIgnoreCase(
                                keyword, Pageable.unpaged())
                        .getContent();

        return medicines.stream()
                // ✔ Only available stock
                .filter(m -> m.getQuantity() != null && m.getQuantity() > 0)

                .map(m -> {

                    if (m.getShop() == null
                            || m.getShop().getLatitude() == null
                            || m.getShop().getLongitude() == null)
                        return null;

                    double distance =
                            DistanceUtil.calculateDistance(
                                    userLat,
                                    userLng,
                                    m.getShop().getLatitude(),
                                    m.getShop().getLongitude());

                    //  If user selected radius → apply filter
                    if (radiusKm != null && distance > radiusKm)
                        return null;

                    MedicineDTO dto = convertToDTO(m);
                    dto.setDistance(distance);
                    return dto;
                })

                .filter(dto -> dto != null)

                // ✔ Sort nearest first
                .sorted(Comparator.comparingDouble(MedicineDTO::getDistance))

                .toList();
    }
    // ================= UPDATE =================

    @Override
    public MedicineDTO updateProduct(Long shopId,
                                     Long medicineId,
                                     MedicineDTO dto) {

        validateSeller(shopId);

        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Medicine", "medicineId", medicineId));

        if (!medicine.getShop().getShopId().equals(shopId))
            throw new ApiException("Not authorized");

        // 🔥 MANUAL MAPPING (SAFE)

        medicine.setMedicineName(dto.getMedicineName());
        medicine.setDescription(dto.getDescription());
        medicine.setQuantity(dto.getQuantity());
        medicine.setPrice(dto.getPrice());
        medicine.setDiscount(dto.getDiscount());

        Double discount =
                dto.getDiscount() == null ? 0.0 : dto.getDiscount();

        if (discount < 0 || discount > 100)
            throw new ApiException("Discount must be between 0 and 100");

        double specialPrice =
                dto.getPrice() - ((discount / 100) * dto.getPrice());

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
                                new ResourceNotFoundException(
                                        "Medicine", "medicineId", medicineId));

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
                                new ResourceNotFoundException(
                                        "Medicine", "medicineId", medicineId));

        if (!medicine.getShop().getShopId().equals(shopId))
            throw new ApiException("Not authorized");

        String fileName =
                fileService.uploadImage("images/", image);

        medicine.setImage(fileName);

        return convertToDTO(medicineRepository.save(medicine));
    }

    // ================= COMMON =================

    private Shop validateSeller(Long shopId) {

        User seller = authUtils.loggedInUser();

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shop", "shopId", shopId));

        if (!shop.getSeller().getUserId()
                .equals(seller.getUserId()))
            throw new ApiException("Unauthorized");

        return shop;
    }

    private Pageable getPageable(Integer pageNumber,
                                 Integer pageSize,
                                 String sortBy,
                                 String sortOrder) {

        if (pageNumber == null || pageNumber < 0)
            pageNumber = 0;

        if (pageSize == null || pageSize <= 0)
            pageSize = 5;

        if (sortBy == null || sortBy.isBlank())
            sortBy = "medicineId";

        if (sortOrder == null || sortOrder.isBlank())
            sortOrder = "asc";

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return PageRequest.of(pageNumber, pageSize, sort);
    }
    private MedicineDTO convertToDTO(Medicine medicine) {

        MedicineDTO dto = new MedicineDTO();

        dto.setMedicineId(medicine.getMedicineId());
        dto.setMedicineName(medicine.getMedicineName());
        dto.setDescription(medicine.getDescription());
        dto.setQuantity(medicine.getQuantity());
        dto.setPrice(medicine.getPrice());
        dto.setDiscount(medicine.getDiscount());
        dto.setSpecialPrice(medicine.getSpecialPrice());

        if (medicine.getImage() != null) {
            dto.setImage("http://localhost:8080/images/" + medicine.getImage());
        }

        if (medicine.getShop() != null) {
            dto.setShopName(medicine.getShop().getShopName());
            dto.setShopCity(medicine.getShop().getCity());
            dto.setShopId(medicine.getShop().getShopId()); // 🔥 THIS WAS MISSING
        }

        return dto;
    }
}