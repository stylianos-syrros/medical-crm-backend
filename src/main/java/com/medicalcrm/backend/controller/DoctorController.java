package com.medicalcrm.backend.controller;

import com.medicalcrm.backend.dto.request.CreateDoctorRequest;
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

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/me")
    public DoctorResponse createMyProfile(
        @Valid @RequestBody CreateDoctorRequest request) {
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

}
