package com.example.MediSearch.controller;

import com.example.MediSearch.payload.AnalyticsDTO;
import com.example.MediSearch.payload.MedicineDTO;
import com.example.MediSearch.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/seller/shop")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    // Top 5 best-selling medicines
    @GetMapping("/{shopId}/analytics/top-selling")
    public ResponseEntity<List<MedicineDTO>> topSelling(@PathVariable Long shopId) {
        return ResponseEntity.ok(analyticsService.getTopSelling(shopId, 5));
    }

    // Bottom 5 least-selling medicines
    @GetMapping("/{shopId}/analytics/least-selling")
    public ResponseEntity<List<MedicineDTO>> leastSelling(@PathVariable Long shopId) {
        return ResponseEntity.ok(analyticsService.getLeastSelling(shopId, 5));
    }

    // Full monthly revenue report
    @GetMapping("/{shopId}/analytics/revenue")
    public ResponseEntity<List<AnalyticsDTO>> revenue(@PathVariable Long shopId) {
        return ResponseEntity.ok(analyticsService.getMonthlyRevenue(shopId));
    }

    // Low stock alert (quantity < threshold)
    @GetMapping("/{shopId}/analytics/low-stock")
    public ResponseEntity<List<MedicineDTO>> lowStock(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "10") Integer threshold) {
        return ResponseEntity.ok(analyticsService.getLowStock(shopId, threshold));
    }
}