package com.medicalcrm.backend.service;

import com.medicalcrm.backend.model.MedicalService;

import java.math.BigDecimal;
import java.util.List;

public interface MedicalServiceService {

    MedicalService createService(String name,
                                 String description,
                                 BigDecimal price,
                                 Integer duration);

    MedicalService updateService(Long serviceId,
                                 String name,
                                 String description,
                                 BigDecimal price,
                                 Integer duration);

    void deleteService(Long serviceId);

    List<MedicalService> getAllServices();

    MedicalService getServiceById(Long serviceId);
}
