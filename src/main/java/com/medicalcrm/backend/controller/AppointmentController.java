package com.medicalcrm.backend.controller;

import com.medicalcrm.backend.dto.request.CreateAppointmentRequest;
import com.medicalcrm.backend.dto.request.UpdateAppointmentNotesRequest;
import com.medicalcrm.backend.dto.request.UpdateAppointmentScheduleRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.model.AppointmentStatus;
import com.medicalcrm.backend.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @PostMapping("/patient/{patientId}")
    public AppointmentResponse bookAppointment(
            @PathVariable Long patientId,
            @Valid @RequestBody CreateAppointmentRequest request) {

        return appointmentService.bookAppointment(patientId, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @DeleteMapping("/patient/{patientId}/{appointmentId}")
    public void cancelAppointment(
            @PathVariable Long patientId,
            @PathVariable Long appointmentId){

        appointmentService.cancelAppointment(patientId, appointmentId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @PutMapping("/patient/{patientId}/{appointmentId}/reschedule")
    public void rescheduleAppointment(
            @PathVariable Long patientId,
            @PathVariable Long appointmentId,
            @Valid @RequestBody UpdateAppointmentScheduleRequest request){

        appointmentService.rescheduleAppointment(patientId, appointmentId, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @GetMapping("/patient/{patientId}/upcoming")
    public List<AppointmentResponse> getUpcomingAppointmentsForPatient(
            @PathVariable Long patientId){

        return appointmentService.getUpcomingAppointmentsForPatient(patientId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @GetMapping("/patient/{patientId}/history")
    public List<AppointmentResponse> getAppointmentHistoryForPatient(
            @PathVariable Long patientId){

        return appointmentService.getAppointmentHistoryForPatient(patientId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @GetMapping("/doctor/{doctorId}/upcoming")
    public List<AppointmentResponse> getUpcomingAppointmentsForDoctor(
            @PathVariable Long doctorId){

        return appointmentService.getUpcomingAppointmentsForDoctor(doctorId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping("/doctor/{doctorId}/history")
    public List<AppointmentResponse> getAppointmentHistoryForDoctor(
            @PathVariable Long doctorId){

        return appointmentService.getAppointmentHistoryForDoctor(doctorId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping("/doctor/{doctorId}/status")
    public List<AppointmentResponse> getAppointmentsForDoctorByStatus(
            @PathVariable Long doctorId,
            @RequestParam AppointmentStatus status){

        return appointmentService.getAppointmentsForDoctorByStatus(doctorId, status);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @DeleteMapping("/doctor/{doctorId}/{appointmentId}/cancel")
    public void cancelAppointmentByDoctor(
            @PathVariable Long doctorId,
            @PathVariable Long appointmentId) {

        appointmentService.cancelAppointmentByDoctor(doctorId, appointmentId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PutMapping("/doctor/{doctorId}/{appointmentId}/complete")
    public void completeAppointmentByDoctor(
            @PathVariable Long doctorId,
            @PathVariable Long appointmentId) {

        appointmentService.completeAppointmentByDoctor(doctorId, appointmentId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PutMapping("/doctor/{doctorId}/{appointmentId}/notes")
    public void updateNotesByDoctor(
            @PathVariable Long doctorId,
            @PathVariable Long appointmentId,
            @Valid @RequestBody UpdateAppointmentNotesRequest request) {

        appointmentService.updateNotesByDoctor(doctorId, appointmentId, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/date")
    public List<AppointmentResponse> getAppointmentsByDate(
            @RequestParam LocalDate date) {

        return appointmentService.getAppointmentsByDate(date);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status")
    public List<AppointmentResponse> getAppointmentsByStatus(
            @RequestParam AppointmentStatus status) {

        return appointmentService.getAppointmentsByStatus(status);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/doctor/{doctorId}")
    public List<AppointmentResponse> getAppointmentsByDoctor(
            @PathVariable Long doctorId) {

        return appointmentService.getAppointmentsByDoctor(doctorId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/patient/{patientId}")
    public List<AppointmentResponse> getAppointmentsByPatient(
            @PathVariable Long patientId) {

        return appointmentService.getAppointmentsByPatient(patientId);
    }

}
