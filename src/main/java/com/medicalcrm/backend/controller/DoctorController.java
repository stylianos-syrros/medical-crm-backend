package com.medicalcrm.backend.controller;

import com.medicalcrm.backend.dto.request.CreateDoctorRequest;
import com.medicalcrm.backend.dto.request.UpdateAppointmentNotesRequest;
import com.medicalcrm.backend.dto.request.UpdateDoctorRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.dto.response.DoctorResponse;
import com.medicalcrm.backend.dto.response.PatientResponse;
import com.medicalcrm.backend.service.AppointmentService;
import com.medicalcrm.backend.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/me")
    public DoctorResponse createMyProfile(@Valid @RequestBody CreateDoctorRequest request) {
        return doctorService.createMyProfile(request);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/me")
    public DoctorResponse getMyProfile() {
        return doctorService.getMyProfile();
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/me")
    public DoctorResponse updateMyProfile(
            @Valid @RequestBody UpdateDoctorRequest request) {

        return doctorService.updateMyProfile(request);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/me/patients")
    public List<PatientResponse> getMyPatients() {
        return doctorService.getMyPatients();
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/me/appointments")
    public List<AppointmentResponse> getMyAppointments() {
        return doctorService.getMyAppointments();
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/me/appointments/history")
    public List<AppointmentResponse> getMyAppointmentHistory() {
        return doctorService.getMyAppointmentHistory();
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/me/appointments/upcoming")
    public List<AppointmentResponse> getMyUpcomingAppointments() {
        return doctorService.getMyUpcomingAppointments();
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/me/appointments/{appointmentId}/notes")
    public void updateMyAppointmentNotes(
            @PathVariable Long appointmentId,
            @Valid @RequestBody UpdateAppointmentNotesRequest request) {

        Long doctorId = doctorService.getMyProfile().getId();
        appointmentService.updateNotesByDoctor(doctorId, appointmentId, request);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/me/appointments/{appointmentId}/complete")
    public void completeMyAppointment(@PathVariable Long appointmentId) {
        Long doctorId = doctorService.getMyProfile().getId();
        appointmentService.completeAppointmentByDoctor(doctorId, appointmentId);
    }

}
