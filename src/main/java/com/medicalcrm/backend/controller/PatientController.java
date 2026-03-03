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

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me")
    public PatientResponse getMyProfile(){

        return patientService.getMyProfile();
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/me")
    public PatientResponse updateMyProfile(
            @Valid @RequestBody UpdatePatientRequest request){

        return patientService.updateMyProfile(request);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me/doctors")
    public List<DoctorResponse> getMyDoctors(){

        return patientService.getMyDoctors();
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me/doctors/all")
    public List<DoctorResponse> getAllDoctorsForBooking() {

        return patientService.getAllDoctorsForBooking();
    }
}
