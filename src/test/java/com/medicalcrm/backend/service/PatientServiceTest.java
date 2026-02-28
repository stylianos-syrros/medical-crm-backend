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
    void createMyProfile_success() {
        mockAuthentication("patient1", Role.PATIENT);

        CreatePatientRequest request = new CreatePatientRequest();
        request.setPhone("123");

        when(userRepository.findByUsername("patient1"))
                .thenReturn(Optional.of(user));
        when(patientRepository.findByUserId(1L))
                .thenReturn(Optional.empty());
        when(patientRepository.existsByPhone("123"))
                .thenReturn(false);

        when(patientRepository.save(any()))
                .thenReturn(patient);

        PatientResponse response = patientService.createMyProfile(request);

        assertNotNull(response);
        verify(patientRepository).save(any());
    }

    @Test
    void createMyProfile_userNotFound() {
        mockAuthentication("missingUser", Role.PATIENT);

        CreatePatientRequest request = new CreatePatientRequest();
        request.setPhone("123");

        when(userRepository.findByUsername("missingUser"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> patientService.createMyProfile(request));
    }

    @Test
    void createMyProfile_alreadyExists() {
        mockAuthentication("patient1", Role.PATIENT);

        CreatePatientRequest request = new CreatePatientRequest();
        request.setPhone("123");

        when(userRepository.findByUsername("patient1"))
                .thenReturn(Optional.of(user));
        when(patientRepository.findByUserId(1L))
                .thenReturn(Optional.of(patient));

        assertThrows(BusinessException.class,
                () -> patientService.createMyProfile(request));
    }

    @Test
    void getMyProfile_success() {

        mockAuthentication("patient1", Role.PATIENT);

        when(userRepository.findByUsername("patient1"))
                .thenReturn(Optional.of(user));
        when(patientRepository.findByUserId(1L))
                .thenReturn(Optional.of(patient));

        PatientResponse response = patientService.getMyProfile();

        assertNotNull(response);
    }

    @Test
    void getMyProfile_patientNotFound() {
        mockAuthentication("patient1", Role.PATIENT);

        when(userRepository.findByUsername("patient1"))
                .thenReturn(Optional.of(user));
        when(patientRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> patientService.getMyProfile());
    }

    @Test
    void updateMyProfile_success() {

        mockAuthentication("patient1", Role.PATIENT);
        UpdatePatientRequest request = new UpdatePatientRequest();
        request.setPhone("123");

        when(userRepository.findByUsername("patient1"))
                .thenReturn(Optional.of(user));
        when(patientRepository.findByUserId(1L))
                .thenReturn(Optional.of(patient));
        when(patientRepository.existsByPhoneAndIdNot("123", 1L))
                .thenReturn(false);

        PatientResponse response =
                patientService.updateMyProfile(request);

        assertNotNull(response);
    }

    @Test
    void updateMyProfile_phoneAlreadyUsed() {
        mockAuthentication("patient1", Role.PATIENT);

        UpdatePatientRequest request = new UpdatePatientRequest();
        request.setPhone("123");

        when(userRepository.findByUsername("patient1"))
                .thenReturn(Optional.of(user));
        when(patientRepository.findByUserId(1L))
                .thenReturn(Optional.of(patient));
        when(patientRepository.existsByPhoneAndIdNot("123", 1L))
                .thenReturn(true);

        assertThrows(BusinessException.class,
                () -> patientService.updateMyProfile(request));
    }

    @Test
    void getMyDoctors_success() {

        mockAuthentication("patient1", Role.PATIENT);

        when(userRepository.findByUsername("patient1"))
                .thenReturn(Optional.of(user));
        when(patientRepository.findByUserId(1L))
                .thenReturn(Optional.of(patient));

        when(appointmentRepository.findDistinctDoctorsByPatientId(1L))
                .thenReturn(java.util.List.of());

        var list = patientService.getMyDoctors();

        assertNotNull(list);
    }

    @Test
    void getMyAppointments_success() {

        mockAuthentication("patient1", Role.PATIENT);

        when(userRepository.findByUsername("patient1"))
                .thenReturn(Optional.of(user));
        when(patientRepository.findByUserId(1L))
                .thenReturn(Optional.of(patient));

        when(appointmentRepository.findByPatientId(1L))
                .thenReturn(java.util.List.of());

        var list = patientService.getMyAppointments();

        assertNotNull(list);
    }

    @Test
    void getMyAppointmentsHistory_success() {
        mockAuthentication("patient1", Role.PATIENT);

        when(userRepository.findByUsername("patient1"))
                .thenReturn(Optional.of(user));
        when(patientRepository.findByUserId(1L))
                .thenReturn(Optional.of(patient));
        when(appointmentRepository.findByPatientIdAndStatus(1L, AppointmentStatus.COMPLETED))
                .thenReturn(java.util.List.of());

        var list = patientService.getMyAppointmentsHistory();

        assertNotNull(list);
    }

    @Test
    void getMyUpcomingAppointments_success() {
        mockAuthentication("patient1", Role.PATIENT);

        when(userRepository.findByUsername("patient1"))
                .thenReturn(Optional.of(user));
        when(patientRepository.findByUserId(1L))
                .thenReturn(Optional.of(patient));
        when(appointmentRepository.findByPatientIdAndStatus(1L, AppointmentStatus.SCHEDULED))
                .thenReturn(java.util.List.of());

        var list = patientService.getMyUpcomingAppointments();

        assertNotNull(list);
    }

    @Test
    void cancelAppointment_success() {

        mockAuthentication("patient1", Role.PATIENT);
        when(userRepository.findByUsername("patient1"))
                .thenReturn(Optional.of(user));
        when(patientRepository.findByUserId(1L))
                .thenReturn(Optional.of(patient));

        when(appointmentRepository.findById(10L))
                .thenReturn(Optional.of(appointment));

        when(paymentRepository.existsByAppointmentId(10L))
                .thenReturn(false);

        patientService.cancelAppointment(10L);

        assertEquals(AppointmentStatus.CANCELLED,
                appointment.getStatus());
    }

    @Test
    void cancelAppointment_notOwner() {

        mockAuthentication("patient1", Role.PATIENT);
        when(userRepository.findByUsername("patient1"))
                .thenReturn(Optional.of(user));
        when(patientRepository.findByUserId(1L))
                .thenReturn(Optional.of(patient));

        Patient otherPatient = new Patient();
        otherPatient.setId(2L);

        appointment.setPatient(otherPatient);

        when(appointmentRepository.findById(10L))
                .thenReturn(Optional.of(appointment));

        assertThrows(BusinessException.class,
                () -> patientService.cancelAppointment(10L));
    }

    @Test
    void cancelAppointment_hasPayments() {

        mockAuthentication("patient1", Role.PATIENT);
        when(userRepository.findByUsername("patient1"))
                .thenReturn(Optional.of(user));
        when(patientRepository.findByUserId(1L))
                .thenReturn(Optional.of(patient));

        when(appointmentRepository.findById(10L))
                .thenReturn(Optional.of(appointment));

        when(paymentRepository.existsByAppointmentId(10L))
                .thenReturn(true);

        assertThrows(BusinessException.class,
                () -> patientService.cancelAppointment(10L));
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
