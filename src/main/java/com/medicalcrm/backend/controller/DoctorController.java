package com.medicalcrm.backend.controller;

import com.medicalcrm.backend.dto.request.CreateDoctorRequest;
import com.medicalcrm.backend.dto.request.UpdateAppointmentNotesRequest;
import com.medicalcrm.backend.dto.request.UpdateDoctorRequest;
import com.medicalcrm.backend.dto.response.DoctorResponse;
import com.medicalcrm.backend.dto.response.PatientResponse;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public DoctorResponse createDoctor(
            @Valid @RequestBody CreateDoctorRequest request) {

        return doctorService.createDoctor(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping("/{doctorId}")
    public DoctorResponse getProfile(@PathVariable Long doctorId){

        return doctorService.getProfile(doctorId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PutMapping("/{doctorId}")
    public DoctorResponse updateProfile(
            @PathVariable Long doctorId,
            @Valid @RequestBody UpdateDoctorRequest request){

        return  doctorService.updateProfile(doctorId, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping("/{doctorId}/patients")
    public List<PatientResponse> getMyPatients(@PathVariable Long doctorId){

        return doctorService.getMyPatients(doctorId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping("/{doctorId}/appointments")
    public List<AppointmentResponse> getMyAppointments(@PathVariable Long doctorId){

        return doctorService.getMyAppointments(doctorId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping("/{doctorId}/appointments/history")
    public List<AppointmentResponse> getMyAppointmentHistory(@PathVariable Long doctorId){

        return doctorService.getMyAppointmentHistory(doctorId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping("/{doctorId}/appointments/upcoming")
    public List<AppointmentResponse> getMyUpcomingAppointments(@PathVariable Long doctorId){

        return doctorService.getMyUpcomingAppointments(doctorId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PutMapping("/{doctorId}/appointments/{appointmentId}/notes")
    public void updateNotes(
            @PathVariable Long doctorId,
            @PathVariable Long appointmentId,
            @Valid @RequestBody UpdateAppointmentNotesRequest request){

        appointmentService.updateNotesByDoctor(doctorId, appointmentId, request);
    }


}
