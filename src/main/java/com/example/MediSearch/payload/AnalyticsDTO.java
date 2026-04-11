package com.example.MediSearch.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class AnalyticsDTO {
    private String medicineName;
    private Long totalSold;
    private Double revenue;
    private Integer currentStock;
}