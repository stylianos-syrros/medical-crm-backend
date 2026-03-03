package com.medicalcrm.backend.repository;

import com.medicalcrm.backend.model.Appointment;
import com.medicalcrm.backend.model.AppointmentStatus;
import com.medicalcrm.backend.model.Doctor;
import com.medicalcrm.backend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.time.LocalDate;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByAppointmentDate(LocalDate date);

    List<Appointment> findByStatus(AppointmentStatus status);

    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);
    List<Appointment> findByPatientIdAndStatusIn(Long patientId, Collection<AppointmentStatus> statuses);

    List<Appointment> findByPatientIdAndAppointmentDateAndStatus(
            Long patientId,
            LocalDate appointmentDate,
            AppointmentStatus status);

    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);
    List<Appointment> findByDoctorIdAndStatusIn(Long doctorId, Collection<AppointmentStatus> statuses);

    List<Appointment> findByDoctorIdAndAppointmentDateAndStatus(
            Long doctorId,
            LocalDate appointmentDate,
            AppointmentStatus status);

    boolean existsByPatientIdAndAppointmentDateAndAppointmentTimeAndStatus(
            Long patientId,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            AppointmentStatus status);

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(
            Long doctorId,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            AppointmentStatus status);

    boolean existsByPatientIdAndAppointmentDateAndAppointmentTimeAndStatusAndIdNot(
            Long patientId,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            AppointmentStatus status,
            Long id);

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusAndIdNot(
            Long doctorId,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            AppointmentStatus status,
            Long id);

    @Query("SELECT COALESCE(SUM(a.service.price),0) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.status <> com.medicalcrm.backend.model.AppointmentStatus.CANCELLED")
    BigDecimal sumAppointmentPricesToDoctor(@Param("doctorId") Long doctorId);

    @Query("SELECT COALESCE(SUM(a.service.price),0) FROM Appointment a WHERE a.patient.id = :patientId AND a.status <> com.medicalcrm.backend.model.AppointmentStatus.CANCELLED")
    BigDecimal sumAppointmentPricesByPatient(@Param("patientId") Long patietId);

    @Query("SELECT DISTINCT a.patient FROM Appointment a WHERE a.doctor.id = :doctorId")
    List<Patient> findDistinctPatientsByDoctorId(Long doctorId);

    @Query("SELECT DISTINCT a.doctor FROM Appointment a WHERE a.patient.id = :patientId")
    List<Doctor> findDistinctDoctorsByPatientId(Long patientId);

}
