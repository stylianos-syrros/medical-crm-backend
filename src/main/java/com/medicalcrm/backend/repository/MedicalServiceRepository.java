package com.medicalcrm.backend.repository;

import com.medicalcrm.backend.model.MedicalService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicalServiceRepository extends JpaRepository<MedicalService, Long>{

    Optional<MedicalService> findByName(String name);

}
