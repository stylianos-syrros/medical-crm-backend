package com.medicalcrm.backend.repository;

import com.medicalcrm.backend.model.Payment;
import com.medicalcrm.backend.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long>{

    List<Payment> findByAppointmentId(Long appointmentId);

    List<Payment> findByPaidById(Long patientId);

    List<Payment> findByStatus(PaymentStatus status);
}
