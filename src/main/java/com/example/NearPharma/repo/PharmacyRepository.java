package com.example.NearPharma.repo;
import com.example.NearPharma.model.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, Long>{
    List<Pharmacy> findAll();
    Optional<Pharmacy> findById(Long id);
}

