package com.example.MediSearch.repository;

import com.example.MediSearch.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {

    @Query("""
    SELECT bi.medicine.medicineName, SUM(bi.quantity)
    FROM BillItem bi
    WHERE bi.bill.shop.shopId = :shopId
    AND MONTH(bi.bill.billDate) = MONTH(CURRENT_DATE)
    AND YEAR(bi.bill.billDate) = YEAR(CURRENT_DATE)
    GROUP BY bi.medicine.medicineName
    ORDER BY SUM(bi.quantity) DESC
    """)
    List<Object[]> findMonthlySales(Long shopId);
}
