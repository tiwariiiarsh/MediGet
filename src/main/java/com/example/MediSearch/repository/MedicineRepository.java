package com.example.MediSearch.repository;

import com.example.MediSearch.model.Medicine;
import com.example.MediSearch.model.Shop;
import com.example.MediSearch.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Page<Medicine> findByShop(Shop shop, Pageable pageable);

    Page<Medicine> findByMedicineNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );
}
