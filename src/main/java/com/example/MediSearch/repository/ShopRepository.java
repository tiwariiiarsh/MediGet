package com.example.MediSearch.repository;

import com.example.MediSearch.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, Long> {
}