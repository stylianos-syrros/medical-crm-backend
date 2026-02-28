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

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/me")
    public PatientResponse createMyProfile(
            @Valid @RequestBody CreatePatientRequest request){

        return patientService.createMyProfile(request);
    }

    @PreAuthorize("hasAnyRole('PATIENT')")
    @GetMapping("/me")
    public PatientResponse getMyProfile(){

        return patientService.getMyProfile();
    }

    @PreAuthorize("hasAnyRole('PATIENT')")
    @PutMapping("/me")
    public PatientResponse updateMyProfile(
            @Valid @RequestBody UpdatePatientRequest request){

        return patientService.updateMyProfile(request);
    }

    @PreAuthorize("hasAnyRole('PATIENT')")
    @GetMapping("/me/doctors")
    public List<DoctorResponse> getMyDoctors(){

        return patientService.getMyDoctors();
    }

    @PreAuthorize("hasAnyRole('PATIENT')")
    @GetMapping("/me/appointments")
    public List<AppointmentResponse> getMyAppointments(){

        return patientService.getMyAppointments();
    }

    @PreAuthorize("hasAnyRole('PATIENT')")
    @GetMapping("/me/appointments/history")
    public List<AppointmentResponse> getMyAppointmentsHistory() {

        return patientService.getMyAppointmentsHistory();
    }


    @PreAuthorize("hasAnyRole('PATIENT')")
    @GetMapping("/me/appointments/upcoming")
    public List<AppointmentResponse> getMyUpcomingAppointments() {

        return patientService.getMyUpcomingAppointments();
    }

    @PreAuthorize("hasAnyRole('PATIENT')")
    @DeleteMapping("/me/appointments/{appointmentId}")
    public void cancelAppointment(
            @PathVariable Long appointmentId){

        patientService.cancelAppointment(appointmentId);
    }

}
