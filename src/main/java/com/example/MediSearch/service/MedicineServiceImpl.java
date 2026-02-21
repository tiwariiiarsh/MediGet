package com.example.MediSearch.service;

import com.example.MediSearch.Utils.AuthUtils;
import com.example.MediSearch.exceptions.ApiException;
import com.example.MediSearch.model.Medicine;
import com.example.MediSearch.model.User;
import com.example.MediSearch.payload.MedicineDTO;
import com.example.MediSearch.payload.MedicineResponse;
import com.example.MediSearch.repository.MedicineRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicineServiceImpl implements MedicineService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private MedicineRepository medicineRepository;

    @Override
    public MedicineDTO addMedicine(MedicineDTO medicineDTO) {
        Medicine medicine = modelMapper.map(medicineDTO, Medicine.class);
        medicine.setImage("default.png");
        medicine.setUser(authUtils.loggedInUser());
        double specialPrice = medicine.getPrice() - ((medicine.getDiscount() * 0.01) * medicine.getPrice());
        medicine.setSpecialPrice(specialPrice);
        Medicine savedProduct = medicineRepository.save(medicine);
//            ye sare data ko DTO se entity me save kr rhi h
        return modelMapper.map(savedProduct, MedicineDTO.class);
    }

    @Override
    public MedicineResponse getAllMedicines(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String keyword) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")  //    Ignore case matlab ASC, asc, Asc sabko same treat karega.
                ?Sort.by(sortBy).ascending()
                :Sort.by(sortBy).descending();
// 1.Ye ek Pageable object banata hai jo Spring Data JPA ko batata hai ki kaunsa page aur kitne records chahiye.
        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);
//    Page<T> = Data + Metadata (pagination info)

//       Dynamic filtering using Specification
        Specification<Medicine> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("medicineName")), "%" + keyword.toLowerCase() + "%"));
        }

        Page<Medicine> pageProducts = medicineRepository.findAll(spec, pageDetails);
        List<Medicine> products =pageProducts.getContent();

        List<MedicineDTO> productDTOS = products.stream()
                .map(medicine -> {
                    MedicineDTO productDTO = modelMapper.map(medicine, MedicineDTO.class);
//                    productDTO.setImage(constructImageUrl(medicine.getImage()));
                    return productDTO;
                })
                .toList();


//        check is medicine is zero 0
//        if(products.isEmpty()){
//            throw new ApiException("medicine is not present!!");
//        }
        MedicineResponse productResponse = new MedicineResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(productResponse.getPageNumber());
        productResponse.setLastPage(productResponse.isLastPage());
        productResponse.setTotalPages(productResponse.getTotalPages());
        productResponse.setTotalElements(productResponse.getTotalElements());
        productResponse.setPageSize(productResponse.getPageSize());
        return productResponse;
    }

    @Override
    public MedicineResponse getAllMedicinesForSeller(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAnOrder = sortOrder.equalsIgnoreCase("asc")?
                Sort.by(sortBy).ascending():
                Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAnOrder);

        User user = authUtils.loggedInUser();
        Page<Medicine> pageMedicine = medicineRepository.findByUser(user,pageDetails);
        List<Medicine> products =pageMedicine.getContent();

        List<MedicineDTO> productDTOS = products.stream()
                .map(medicine -> {
                   MedicineDTO medicineDTO = modelMapper.map(medicine, MedicineDTO.class);

//                    productDTO.setImage(constructImageUrl(medicine.getImage())
                    return medicineDTO;
                })
                .toList();

        MedicineResponse productResponse = new MedicineResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(productResponse.getPageNumber());
        productResponse.setLastPage(productResponse.isLastPage());
        productResponse.setTotalPages(productResponse.getTotalPages());
        productResponse.setTotalElements(productResponse.getTotalElements());
        productResponse.setPageSize(productResponse.getPageSize());
        return productResponse;
    }

    @Override
    public MedicineResponse searchMedicineByKeyword(
            String keyword,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder) {

        // Default sort field safety
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "medicineId";
        }

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Medicine> pageProducts =
                medicineRepository.findByMedicineNameContainingIgnoreCase(
                        keyword,
                        pageable
                );

        if (pageProducts.isEmpty()) {
            throw new ApiException("Medicine not found with keyword: " + keyword);
        }

        List<MedicineDTO> medicineDTOS = pageProducts.getContent()
                .stream()
                .map(medicine -> modelMapper.map(medicine, MedicineDTO.class))
                .toList();

        MedicineResponse response = new MedicineResponse();
        response.setContent(medicineDTOS);
        response.setPageNumber(pageProducts.getNumber());
        response.setPageSize(pageProducts.getSize());
        response.setTotalElements(pageProducts.getTotalElements());
        response.setTotalPages(pageProducts.getTotalPages());
        response.setLastPage(pageProducts.isLast());

        return response;
    }


}
