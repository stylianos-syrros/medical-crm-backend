package com.medicalcrm.backend.service;

import com.medicalcrm.backend.dto.request.CreateAppointmentRequest;
import com.medicalcrm.backend.dto.request.UpdateAppointmentNotesRequest;
import com.medicalcrm.backend.dto.request.UpdateAppointmentScheduleRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.model.Appointment;
import com.medicalcrm.backend.model.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentService {

    // Patient
    AppointmentResponse bookAppointment(Long patientId, CreateAppointmentRequest request);

    void cancelAppointment(Long patientId, Long appointmentId);

    void rescheduleAppointment(Long patientId,
                               Long appointmentId,
                               UpdateAppointmentScheduleRequest request);

    List<AppointmentResponse> getUpcomingAppointmentsForPatient(Long patientId);

    List<AppointmentResponse> getAppointmentHistoryForPatient(Long patientId);

    // Doctor
    List<AppointmentResponse> getUpcomingAppointmentsForDoctor(Long doctorId);

    List<AppointmentResponse> getAppointmentHistoryForDoctor(Long doctorId);

    List<AppointmentResponse> getAppointmentsForDoctorByStatus(Long doctorId, AppointmentStatus status);

    void cancelAppointmentByDoctor(Long doctorId, Long appointmentId);

    void completeAppointmentByDoctor(Long doctorId, Long appointmentId);

    void updateNotesByDoctor(Long doctorId,
                                  Long appointmentId,
                                  UpdateAppointmentNotesRequest request);

    // Admin
    List<AppointmentResponse> getAllAppointments();

    List<AppointmentResponse> getAppointmentsByDate(LocalDate date);

    List<AppointmentResponse> getAppointmentsByStatus(AppointmentStatus status);

    List<AppointmentResponse> getAppointmentsByDoctor(Long doctorId);

    List<AppointmentResponse> getAppointmentsByPatient(Long patientId);


}
