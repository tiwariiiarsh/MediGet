package com.example.MediSearch.service;

import com.example.MediSearch.payload.AnalyticsDTO;
import com.example.MediSearch.payload.MedicineDTO;
import java.util.List;

public interface AnalyticsService {
    List<MedicineDTO> getTopSelling(Long shopId, int limit);
    List<MedicineDTO> getLeastSelling(Long shopId, int limit);
    List<AnalyticsDTO> getMonthlyRevenue(Long shopId);
    List<MedicineDTO> getLowStock(Long shopId, Integer threshold);
}