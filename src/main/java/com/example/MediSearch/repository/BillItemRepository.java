package com.example.MediSearch.repository;

import com.example.MediSearch.model.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillItemRepository
        extends JpaRepository<BillItem, Long> {
}