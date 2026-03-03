package com.medicalcrm.backend.service;

import com.medicalcrm.backend.dto.request.CreateAppointmentRequest;
import com.medicalcrm.backend.dto.request.UpdateAppointmentNotesRequest;
import com.medicalcrm.backend.dto.request.UpdateAppointmentScheduleRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.model.AppointmentStatus;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    // Patient
    AppointmentResponse bookAppointment(CreateAppointmentRequest request);

    void cancelAppointmentByPatient(Long appointmentId);

    void rescheduleAppointment(Long appointmentId,
                               UpdateAppointmentScheduleRequest request);

    List<AppointmentResponse> getUpcomingAppointmentsForPatient();

    List<AppointmentResponse> getAppointmentsHistoryForPatient();

    List<AppointmentResponse> getDoctorScheduledAppointmentsByDateForPatient(
            Long doctorId,
            LocalDate date);

    void updateNotesByPatient(Long appointmentId,
                              UpdateAppointmentNotesRequest request);

    // Doctor
    List<AppointmentResponse> getUpcomingAppointmentsForDoctor();

    List<AppointmentResponse> getAppointmentsHistoryForDoctor();

    List<AppointmentResponse> getAppointmentsForDoctorByStatus(AppointmentStatus status);

    void cancelAppointmentByDoctor(Long appointmentId);

    void completeAppointmentByDoctor(Long appointmentId);

    void updateNotesByDoctor(Long appointmentId,
                                  UpdateAppointmentNotesRequest request);

    // Admin
    List<AppointmentResponse> getAllAppointments();

    List<AppointmentResponse> getAppointmentsByDate(LocalDate date);

    List<AppointmentResponse> getAppointmentsByStatus(AppointmentStatus status);

    List<AppointmentResponse> getAppointmentsByDoctor(Long doctorId);

    List<AppointmentResponse> getAppointmentsByPatient(Long patientId);


}
