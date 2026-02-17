package com.medicalcrm.backend.controller;

import com.medicalcrm.backend.dto.request.CreatePatientRequest;
import com.medicalcrm.backend.dto.request.UpdatePatientRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.dto.response.DoctorResponse;
import com.medicalcrm.backend.dto.response.PatientResponse;
import com.medicalcrm.backend.service.PatientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public PatientResponse createPatient(
            @Valid @RequestBody CreatePatientRequest request){

        return patientService.createPatient(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @GetMapping("/{patientId}")
    public PatientResponse getProfile(@PathVariable Long patientId){

        return patientService.getProfile(patientId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @PutMapping("/{patientId}")
    public PatientResponse updateProfile(
            @PathVariable Long patientId,
            @Valid @RequestBody UpdatePatientRequest request){

        return patientService.updateProfile(patientId, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @GetMapping("/{patientId}/doctors")
    public List<DoctorResponse> getMyDoctors(@PathVariable Long patientId){

        return patientService.getMyDoctors(patientId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @GetMapping("/{patientId}/appointments")
    public List<AppointmentResponse> getMyAppointments(@PathVariable Long patientId){

        return patientService.getMyAppointments(patientId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @GetMapping("/{patientId}/appointments/history")
    public List<AppointmentResponse> getMyAppointmentHistory(
            @PathVariable Long patientId) {

        return patientService.getMyAppointmentHistory(patientId);
    }


    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @GetMapping("/{patientId}/appointments/upcoming")
    public List<AppointmentResponse> getMyUpcomingAppointments(
            @PathVariable Long patientId) {

        return patientService.getMyUpcomingAppointments(patientId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @DeleteMapping("/{patientId}/appointments/{appointmentId}")
    public void cancelAppointment(
            @PathVariable Long patientId,
            @PathVariable Long appointmentId){

        patientService.cancelAppointment(patientId,appointmentId);
    }

}
