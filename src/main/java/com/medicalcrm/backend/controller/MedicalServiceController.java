package com.medicalcrm.backend.controller;

import com.medicalcrm.backend.dto.request.CreateMedicalServiceRequest;
import com.medicalcrm.backend.dto.request.UpdateMedicalServiceRequest;
import com.medicalcrm.backend.dto.response.MedicalServiceResponse;
import com.medicalcrm.backend.service.MedicalServiceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class MedicalServiceController {

    private final MedicalServiceService medicalServiceService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public MedicalServiceResponse createService(
            @Valid @RequestBody CreateMedicalServiceRequest request){

        return medicalServiceService.createService(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{serviceId}")
    public MedicalServiceResponse updateService(
            @PathVariable Long serviceId,
            @Valid @RequestBody UpdateMedicalServiceRequest request){

        return medicalServiceService.updateService(serviceId, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{serviceId}")
    public void deleteService(@PathVariable Long serviceId){

        medicalServiceService.deleteService(serviceId);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public List<MedicalServiceResponse> getAllServices(){

        return medicalServiceService.getAllServices();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{serviceId}")
    public MedicalServiceResponse getServiceById(@PathVariable Long serviceId){

        return medicalServiceService.getServiceById(serviceId);
    }

}
