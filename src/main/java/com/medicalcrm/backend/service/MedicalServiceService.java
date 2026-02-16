package com.medicalcrm.backend.service;

import com.medicalcrm.backend.dto.request.CreateMedicalServiceRequest;
import com.medicalcrm.backend.dto.request.UpdateMedicalServiceRequest;
import com.medicalcrm.backend.dto.response.MedicalServiceResponse;
import com.medicalcrm.backend.model.MedicalService;

import java.math.BigDecimal;
import java.util.List;

public interface MedicalServiceService {

    MedicalServiceResponse createService(CreateMedicalServiceRequest request);

    MedicalServiceResponse updateService(Long serviceId,
                                        UpdateMedicalServiceRequest request);

    void deleteService(Long serviceId);

    List<MedicalServiceResponse> getAllServices();

    MedicalServiceResponse getServiceById(Long serviceId);
}
