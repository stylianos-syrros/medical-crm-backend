package com.medicalcrm.backend.service;

import com.medicalcrm.backend.model.Appointment;
import com.medicalcrm.backend.model.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentService {

    // Patient
    Appointment bookAppointment(Long patientId,
                                Long doctorId,
                                Long serviceId,
                                LocalDate date,
                                LocalTime time

    );

    void cancelAppointment(Long appointmentId);

    List<Appointment> getUpcomingAppointmentsForPatient(Long patientId);

    List<Appointment> getAppointmentHistoryForPatient(Long patientId);

    // Doctor
    List<Appointment> getUpcomingAppointmentsForDoctor(Long doctorId);

    List<Appointment> getAppointmentHistoryForDoctor(Long doctorId);

    List<Appointment> getAppointmentsForDoctorByStatus(Long doctorId, AppointmentStatus status);

    void cancelAppointmentByDoctor(Long doctorId, Long appointmentId);

    void completeAppointmentByDoctor(Long doctorId, Long appointmentId);

    void addOrUpdateNotesByDoctor(Long doctorId,
                                  Long appointmentId,
                                  String notes);

    // Admin
    List<Appointment> getAllAppointments();

    List<Appointment> getAppointmentsByDate(LocalDate date);

    List<Appointment> getAppointmentsByStatus(AppointmentStatus status);

    void updateNotesByAdmin(Long appointmentId,
                            String notes);

    List<Appointment> getAppointmentsByDoctor(Long doctorId);

    List<Appointment> getAppointmentsByPatient(Long patientId);


}
