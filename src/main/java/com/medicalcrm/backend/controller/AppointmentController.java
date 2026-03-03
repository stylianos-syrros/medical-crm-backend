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

    // PATIENT self endpoints

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/patient/me")
    public AppointmentResponse bookAppointment(
            @Valid @RequestBody CreateAppointmentRequest request) {

        return appointmentService.bookAppointment(request);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @DeleteMapping("/patient/me/{appointmentId}/cancel")
    public void cancelAppointmentByPatient(@PathVariable Long appointmentId){

        appointmentService.cancelAppointmentByPatient(appointmentId);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/patient/me/{appointmentId}/reschedule")
    public void rescheduleAppointment(
            @PathVariable Long appointmentId,
            @Valid @RequestBody UpdateAppointmentScheduleRequest request){

        appointmentService.rescheduleAppointment(appointmentId, request);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/me/upcoming")
    public List<AppointmentResponse> getUpcomingAppointmentsForPatient(){

        return appointmentService.getUpcomingAppointmentsForPatient();
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/me/history")
    public List<AppointmentResponse> getAppointmentsHistoryForPatient(){

        return appointmentService.getAppointmentsHistoryForPatient();
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/me/doctor/{doctorId}/date")
    public List<AppointmentResponse> getDoctorScheduledAppointmentsByDateForPatient(
            @PathVariable Long doctorId,
            @RequestParam LocalDate date) {

        return appointmentService.getDoctorScheduledAppointmentsByDateForPatient(doctorId, date);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/patient/me/{appointmentId}/notes")
    public void updateNotesByPatient(
            @PathVariable Long appointmentId,
            @Valid @RequestBody UpdateAppointmentNotesRequest request) {

        appointmentService.updateNotesByPatient(appointmentId, request);
    }

    // DOCTOR self endpoints

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor/me/upcoming")
    public List<AppointmentResponse> getUpcomingAppointmentsForDoctor(){

        return appointmentService.getUpcomingAppointmentsForDoctor();
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor/me/history")
    public List<AppointmentResponse> getAppointmentHistoryForDoctor(){

        return appointmentService.getAppointmentsHistoryForDoctor();
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor/me/status")
    public List<AppointmentResponse> getAppointmentsForDoctorByStatus(
            @RequestParam AppointmentStatus status){

        return appointmentService.getAppointmentsForDoctorByStatus(status);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @DeleteMapping("/doctor/me/{appointmentId}/cancel")
    public void cancelAppointmentByDoctor(
            @PathVariable Long appointmentId) {

        appointmentService.cancelAppointmentByDoctor(appointmentId);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/doctor/me/{appointmentId}/complete")
    public void completeAppointmentByDoctor(
            @PathVariable Long appointmentId) {

        appointmentService.completeAppointmentByDoctor(appointmentId);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/doctor/me/{appointmentId}/notes")
    public void updateNotesByDoctor(
            @PathVariable Long appointmentId,
            @Valid @RequestBody UpdateAppointmentNotesRequest request) {

        appointmentService.updateNotesByDoctor(appointmentId, request);
    }

    // ADMIN endpoints

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
