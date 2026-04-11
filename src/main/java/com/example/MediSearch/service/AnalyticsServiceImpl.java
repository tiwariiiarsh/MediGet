package com.example.MediSearch.service;

import com.example.MediSearch.payload.AnalyticsDTO;
import com.example.MediSearch.payload.MedicineDTO;
import com.example.MediSearch.repository.BillRepository;
import com.example.MediSearch.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private BillRepository billRepository;

    @Override
    public List<MedicineDTO> getTopSelling(Long shopId, int limit) {
        return medicineRepository
                .findTopSellingByShop(shopId, PageRequest.of(0, limit))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicineDTO> getLeastSelling(Long shopId, int limit) {
        return medicineRepository
                .findLeastSellingByShop(shopId, PageRequest.of(0, limit))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnalyticsDTO> getMonthlyRevenue(Long shopId) {
        List<Object[]> results = billRepository.findMonthlySales(shopId);
        return results.stream().map(row -> {
            AnalyticsDTO dto = new AnalyticsDTO();
            dto.setMedicineName((String) row[0]);
            dto.setTotalSold(((Number) row[1]).longValue());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<MedicineDTO> getLowStock(Long shopId, Integer threshold) {
        return medicineRepository
                .findByShop_ShopId(shopId)
                .stream()
                .filter(m -> m.getQuantity() != null && m.getQuantity() <= threshold)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private MedicineDTO toDTO(com.example.MediSearch.model.Medicine m) {
        MedicineDTO dto = new MedicineDTO();
        dto.setMedicineId(m.getMedicineId());
        dto.setMedicineName(m.getMedicineName());
        dto.setDescription(m.getDescription());
        dto.setQuantity(m.getQuantity());
        dto.setPrice(m.getPrice());
        dto.setDiscount(m.getDiscount());
        dto.setSpecialPrice(m.getSpecialPrice());
        dto.setSalesCount(m.getSalesCount());
        dto.setExpiryDate(m.getExpiryDate());
        if (m.getImage() != null)
            dto.setImage("http://localhost:8080/images/" + m.getImage());
        if (m.getShop() != null) {
            dto.setShopName(m.getShop().getShopName());
            dto.setShopCity(m.getShop().getCity());
            dto.setShopId(m.getShop().getShopId());
        }
        return dto;
    }
}