package com.medicalcrm.backend.service.impl;

import com.medicalcrm.backend.exception.BusinessException;
import com.medicalcrm.backend.exception.NotFoundException;
import com.medicalcrm.backend.model.MedicalService;
import com.medicalcrm.backend.repository.MedicalServiceRepository;
import com.medicalcrm.backend.service.MedicalServiceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedicalServiceServiceImpl implements MedicalServiceService {


    private final MedicalServiceRepository medicalServiceRepository;

    @Override
    public MedicalService createService(String name,
                                 String description,
                                 BigDecimal price,
                                 Integer duration){

        if (medicalServiceRepository.existsByName(name)){
            throw new BusinessException("Medical service with this name already exists");
        }

        MedicalService service = new MedicalService();

        service.setName(name);
        service.setDescription(description);
        service.setDurationMinutes(duration);
        service.setPrice(price);

        MedicalService saved = medicalServiceRepository.save(service);

        log.info("Medical service created: {}", name);

        return saved;
    }

    @Override
    public MedicalService updateService(Long serviceId,
                         String name,
                         String description,
                         BigDecimal price,
                         Integer duration){

        MedicalService service = getServiceById(serviceId);

        if (!service.getName().equals(name) &&
                medicalServiceRepository.existsByName(name)) {
            throw new BusinessException("Medical service with this name already exists");
        }

        service.setName(name);
        service.setDescription(description);
        service.setPrice(price);
        service.setDurationMinutes(duration);

        log.info("Medical service updated: {}", name);

        return service;
    }

    @Override
    public void deleteService(Long serviceId){

        if (!medicalServiceRepository.existsById(serviceId)){
            throw new NotFoundException("Medical Service not found");
        }

        medicalServiceRepository.deleteById(serviceId);

        log.warn("Medical service {} deleted", serviceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalService> getAllServices(){

        return medicalServiceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalService getServiceById(Long serviceId){

        return medicalServiceRepository.findById(serviceId)
                .orElseThrow(()->
                        new NotFoundException("Medical service not found"));
    }

}
