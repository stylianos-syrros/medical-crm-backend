package com.medicalcrm.backend.repository;

import com.medicalcrm.backend.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);

    List<Doctor> findBySpecialty(String specialty);

    List<Doctor> findByLastName(String lastName);
}
