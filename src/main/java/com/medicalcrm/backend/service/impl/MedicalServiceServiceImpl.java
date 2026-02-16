package com.medicalcrm.backend.service.impl;

import com.medicalcrm.backend.dto.request.CreateMedicalServiceRequest;
import com.medicalcrm.backend.dto.request.UpdateMedicalServiceRequest;
import com.medicalcrm.backend.dto.response.MedicalServiceResponse;
import com.medicalcrm.backend.exception.BusinessException;
import com.medicalcrm.backend.exception.NotFoundException;
import com.medicalcrm.backend.mapper.MedicalServiceMapper;
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
    public MedicalServiceResponse createService(CreateMedicalServiceRequest request) {

        if (medicalServiceRepository.existsByName(request.getName())) {
            throw new BusinessException("Service with this name already exists");
        }

        MedicalService service = MedicalServiceMapper.toEntity(request);

        MedicalService saved = medicalServiceRepository.save(service);

        log.info("Medical service created: {}", saved.getName());

        return MedicalServiceMapper.toResponse(saved);
    }

    @Override
    public MedicalServiceResponse updateService(Long serviceId,
                                                UpdateMedicalServiceRequest request) {

        MedicalService service = medicalServiceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found"));

        MedicalServiceMapper.updateEntity(service, request);

        log.info("Medical service {} updated", serviceId);

        return MedicalServiceMapper.toResponse(service);
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
    public List<MedicalServiceResponse> getAllServices() {

        return medicalServiceRepository.findAll()
                .stream()
                .map(MedicalServiceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalServiceResponse getServiceById(Long serviceId) {

        MedicalService service = medicalServiceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found"));

        return MedicalServiceMapper.toResponse(service);
    }

}
