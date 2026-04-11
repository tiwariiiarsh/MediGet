package com.example.MediSearch.repository;

import com.example.MediSearch.model.Medicine;
import com.example.MediSearch.model.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Page<Medicine> findByShop(Shop shop, Pageable pageable);

    Page<Medicine> findByMedicineNameContainingIgnoreCase(String keyword, Pageable pageable);

    List<Medicine> findByShop_ShopId(Long shopId);

    // Top selling medicines for a shop this month
    @Query("""
        SELECT m FROM Medicine m
        WHERE m.shop.shopId = :shopId
        ORDER BY m.salesCount DESC
    """)
    List<Medicine> findTopSellingByShop(Long shopId, Pageable pageable);

    // Least selling medicines for a shop
    @Query("""
        SELECT m FROM Medicine m
        WHERE m.shop.shopId = :shopId
        ORDER BY m.salesCount ASC
    """)
    List<Medicine> findLeastSellingByShop(Long shopId, Pageable pageable);

    // Search by generic name for alternatives
    @Query("""
        SELECT DISTINCT m FROM Medicine m
        WHERE LOWER(m.medicineName) LIKE LOWER(CONCAT('%', :genericName, '%'))
        AND m.quantity > 0
        ORDER BY m.salesCount DESC
    """)
    List<Medicine> findAlternativesByGenericName(String genericName, Pageable pageable);
}