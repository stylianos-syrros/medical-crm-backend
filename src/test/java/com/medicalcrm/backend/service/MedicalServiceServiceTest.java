package com.medicalcrm.backend.service;

import com.medicalcrm.backend.dto.request.CreateMedicalServiceRequest;
import com.medicalcrm.backend.dto.request.UpdateMedicalServiceRequest;
import com.medicalcrm.backend.dto.response.MedicalServiceResponse;
import com.medicalcrm.backend.exception.BusinessException;
import com.medicalcrm.backend.exception.NotFoundException;
import com.medicalcrm.backend.model.MedicalService;
import com.medicalcrm.backend.repository.MedicalServiceRepository;
import com.medicalcrm.backend.service.impl.MedicalServiceServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalServiceServiceTest {

    @Mock
    private MedicalServiceRepository medicalServiceRepository;

    @InjectMocks
    private MedicalServiceServiceImpl medicalServiceService;

    private MedicalService service;

    @BeforeEach
    void setUp() {
        service = new MedicalService();
        service.setId(1L);
        service.setName("Cleaning");
    }

    @Test
    void createService_success() {

        CreateMedicalServiceRequest request =
                new CreateMedicalServiceRequest();
        request.setName("Cleaning");

        when(medicalServiceRepository.existsByName("Cleaning"))
                .thenReturn(false);

        when(medicalServiceRepository.save(any()))
                .thenReturn(service);

        MedicalServiceResponse response =
                medicalServiceService.createService(request);

        assertNotNull(response);
        verify(medicalServiceRepository).save(any());
    }

    @Test
    void createService_duplicateName() {

        CreateMedicalServiceRequest request =
                new CreateMedicalServiceRequest();
        request.setName("Cleaning");

        when(medicalServiceRepository.existsByName("Cleaning"))
                .thenReturn(true);

        assertThrows(BusinessException.class,
                () -> medicalServiceService.createService(request));
    }

    @Test
    void updateService_success() {

        UpdateMedicalServiceRequest request =
                new UpdateMedicalServiceRequest();
        request.setName("Updated");

        when(medicalServiceRepository.findById(1L))
                .thenReturn(Optional.of(service));

        MedicalServiceResponse response =
                medicalServiceService.updateService(1L, request);

        assertNotNull(response);
    }

    @Test
    void updateService_notFound() {

        UpdateMedicalServiceRequest request =
                new UpdateMedicalServiceRequest();

        when(medicalServiceRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> medicalServiceService.updateService(1L, request));
    }

    @Test
    void deleteService_success() {

        when(medicalServiceRepository.existsById(1L))
                .thenReturn(true);

        medicalServiceService.deleteService(1L);

        verify(medicalServiceRepository)
                .deleteById(1L);
    }

    @Test
    void deleteService_notFound() {

        when(medicalServiceRepository.existsById(1L))
                .thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> medicalServiceService.deleteService(1L));
    }

    @Test
    void getAllServices_success() {

        when(medicalServiceRepository.findAll())
                .thenReturn(List.of(service));

        List<MedicalServiceResponse> list =
                medicalServiceService.getAllServices();

        assertEquals(1, list.size());
    }

    @Test
    void getServiceById_success() {

        when(medicalServiceRepository.findById(1L))
                .thenReturn(Optional.of(service));

        MedicalServiceResponse response =
                medicalServiceService.getServiceById(1L);

        assertNotNull(response);
    }

    @Test
    void getServiceById_notFound() {

        when(medicalServiceRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> medicalServiceService.getServiceById(1L));
    }
}
