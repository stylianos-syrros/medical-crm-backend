package com.medicalcrm.backend.repository;

import com.medicalcrm.backend.model.Payment;
import com.medicalcrm.backend.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long>{

    List<Payment> findByAppointmentId(Long appointmentId);

    List<Payment> findByPaidById(Long patientId);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.appointment.id = :appointmentId")
    BigDecimal sumAmountByAppointmentId(@Param("appointmentId") Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);

    @Query("SELECT COALESCE(SUM(p.amount),0) FROM Payment p WHERE p.appointment.doctor.id = :doctorId")
    BigDecimal sumPaymentsToDoctor(@Param("doctorId") Long doctorId);

    @Query("SELECT COALESCE(SUM(p.amount),0) FROM Payment p WHERE p.appointment.patient.id = :patientId")
    BigDecimal sumPaymentsByPatient(@Param("patientId") Long patientId);
}
