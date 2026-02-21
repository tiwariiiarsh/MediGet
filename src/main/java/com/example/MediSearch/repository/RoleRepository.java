package com.example.MediSearch.repository;

import com.example.MediSearch.model.AppRole;
import com.example.MediSearch.model.Role;   // ✅ CORRECT IMPORT
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByRoleName(AppRole appRole);
}