package com.medicalcrm.backend.repository;

import com.medicalcrm.backend.model.Appointment;
import com.medicalcrm.backend.model.AppointmentStatus;
import com.medicalcrm.backend.model.Doctor;
import com.medicalcrm.backend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByAppointmentDate(LocalDate date);

    List<Appointment> findByStatus(AppointmentStatus status);

    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);

    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    @Query("SELECT COALESCE(SUM(a.service.price),0) FROM Appointment a WHERE a.doctor.id = :doctorId")
    BigDecimal sumAppointmentPricesToDoctor(@Param("doctorId") Long doctorId);

    @Query("SELECT COALESCE(SUM(a.service.price),0) FROM Appointment a WHERE a.patient.id = :patientId")
    BigDecimal sumAppointmentPricesByPatient(@Param("patientId") Long patietId);

    //Long patient(Patient patient);

    @Query("SELECT DISTINCT a.patient FROM Appointment a WHERE a.doctor.id = :doctorId")
    List<Patient> findDistinctPatientsByDoctorId(Long doctorId);

    @Query("SELECT DISTINCT a.doctor FROM Appointment a WHERE a.patient.id = :patientId")
    List<Doctor> findDistinctDoctorsByPatientId(Long patientId);

}