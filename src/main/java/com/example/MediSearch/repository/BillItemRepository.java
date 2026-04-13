package com.example.MediSearch.repository;

import com.example.MediSearch.model.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BillItemRepository
        extends JpaRepository<BillItem, Long> {

    @Query("""
SELECT bi.medicine.medicineName, SUM(bi.quantity), SUM(bi.quantity * bi.price)
FROM BillItem bi
WHERE bi.bill.shop.shopId = :shopId
AND MONTH(bi.bill.billDate) = MONTH(CURRENT_DATE)
AND YEAR(bi.bill.billDate) = YEAR(CURRENT_DATE)
GROUP BY bi.medicine.medicineName
ORDER BY SUM(bi.quantity) DESC
""")
    List<Object[]> findMonthlySales(@Param("shopId") Long shopId);
}