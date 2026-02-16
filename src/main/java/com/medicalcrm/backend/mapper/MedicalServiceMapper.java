package com.medicalcrm.backend.mapper;

import com.medicalcrm.backend.dto.request.CreateMedicalServiceRequest;
import com.medicalcrm.backend.dto.request.UpdateMedicalServiceRequest;
import com.medicalcrm.backend.dto.response.MedicalServiceResponse;
import com.medicalcrm.backend.model.MedicalService;

public class MedicalServiceMapper {

    public static MedicalService toEntity(CreateMedicalServiceRequest request) {

        MedicalService service = new MedicalService();

        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setDuration(request.getDuration());

        return service;
    }

    public static MedicalServiceResponse toResponse(MedicalService service){

        MedicalServiceResponse response = new MedicalServiceResponse();

        response.setId(service.getId());
        response.setName(service.getName());
        response.setDescription(service.getDescription());
        response.setPrice(service.getPrice());
        response.setDuration(service.getDuration());

        return response;
    }

    public static void updateEntity(MedicalService service,
                                    UpdateMedicalServiceRequest request) {

        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setDuration(request.getDuration());

    }
}
