package com.medicalcrm.backend.repository;

import com.medicalcrm.backend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long>{

    Optional<Patient> findByUserId(Long userId);

    List<Patient> findByLastName(String lastName);
}

