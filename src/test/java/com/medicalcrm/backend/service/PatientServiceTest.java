package com.medicalcrm.backend.service;

import com.medicalcrm.backend.dto.request.CreatePatientRequest;
import com.medicalcrm.backend.dto.request.UpdatePatientRequest;
import com.medicalcrm.backend.dto.response.PatientResponse;
import com.medicalcrm.backend.exception.BusinessException;
import com.medicalcrm.backend.exception.NotFoundException;
import com.medicalcrm.backend.model.*;
import com.medicalcrm.backend.repository.*;
import com.medicalcrm.backend.service.impl.PatientServiceImpl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    private User user;
    private Patient patient;
    private Appointment appointment;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setUsername("patient1");
        user.setRole(Role.PATIENT);

        patient = new Patient();
        patient.setId(1L);
        patient.setUser(user);

        appointment = new Appointment();
        appointment.setId(10L);
        appointment.setPatient(patient);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createPatient_success() {

        CreatePatientRequest request = new CreatePatientRequest();
        request.setUserId(1L);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(patientRepository.save(any()))
                .thenReturn(patient);

        PatientResponse response = patientService.createPatient(request);

        assertNotNull(response);
        verify(patientRepository).save(any());
    }

    @Test
    void createPatient_userNotFound() {

        CreatePatientRequest request = new CreatePatientRequest();
        request.setUserId(99L);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> patientService.createPatient(request));
    }

    @Test
    void getProfile_success_owner() {

        mockAuthentication("patient1", Role.PATIENT);

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        PatientResponse response = patientService.getProfile(1L);

        assertNotNull(response);
    }

    @Test
    void getProfile_accessDenied() {

        mockAuthentication("otherUser", Role.PATIENT);

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        assertThrows(AccessDeniedException.class,
                () -> patientService.getProfile(1L));
    }

    @Test
    void updateProfile_success() {

        mockAuthentication("patient1", Role.PATIENT);

        UpdatePatientRequest request = new UpdatePatientRequest();

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        PatientResponse response =
                patientService.updateProfile(1L, request);

        assertNotNull(response);
    }

    @Test
    void getMyDoctors_success() {

        mockAuthentication("patient1", Role.PATIENT);

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(appointmentRepository.findDistinctDoctorsByPatientId(1L))
                .thenReturn(java.util.List.of());

        var list = patientService.getMyDoctors(1L);

        assertNotNull(list);
    }

    @Test
    void getMyAppointments_success() {

        mockAuthentication("patient1", Role.PATIENT);

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(appointmentRepository.findByPatientId(1L))
                .thenReturn(java.util.List.of());

        var list = patientService.getMyAppointments(1L);

        assertNotNull(list);
    }

    @Test
    void cancelAppointment_success() {

        mockAuthentication("patient1", Role.PATIENT);

        when(appointmentRepository.findById(10L))
                .thenReturn(Optional.of(appointment));

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(paymentRepository.existsByAppointmentId(10L))
                .thenReturn(false);

        patientService.cancelAppointment(1L, 10L);

        assertEquals(AppointmentStatus.CANCELLED,
                appointment.getStatus());
    }

    @Test
    void cancelAppointment_notOwner() {

        mockAuthentication("patient1", Role.PATIENT);

        Patient otherPatient = new Patient();
        otherPatient.setId(2L);

        appointment.setPatient(otherPatient);

        when(appointmentRepository.findById(10L))
                .thenReturn(Optional.of(appointment));

        assertThrows(BusinessException.class,
                () -> patientService.cancelAppointment(1L, 10L));
    }

    @Test
    void cancelAppointment_hasPayments() {

        mockAuthentication("patient1", Role.PATIENT);

        when(appointmentRepository.findById(10L))
                .thenReturn(Optional.of(appointment));

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(paymentRepository.existsByAppointmentId(10L))
                .thenReturn(true);

        assertThrows(BusinessException.class,
                () -> patientService.cancelAppointment(1L, 10L));
    }

    private void mockAuthentication(String username, Role role) {

        TestingAuthenticationToken auth =
                new TestingAuthenticationToken(
                        username,
                        null,
                        "ROLE_" + role.name()
                );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
