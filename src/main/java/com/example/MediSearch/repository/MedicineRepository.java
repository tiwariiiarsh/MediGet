package com.example.MediSearch.repository;

import com.example.MediSearch.model.Medicine;
import com.example.MediSearch.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine,Long>, JpaSpecificationExecutor<Medicine> {
    Page<Medicine> findByUser(User user, Pageable pageDetails);


    Page<Medicine> findByMedicineNameContainingIgnoreCase(String keyword, Pageable pageable);
}
