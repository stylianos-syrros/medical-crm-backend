package com.medicalcrm.backend.service;

import com.medicalcrm.backend.dto.request.CreateDoctorRequest;
import com.medicalcrm.backend.dto.request.UpdateDoctorRequest;
import com.medicalcrm.backend.dto.response.DoctorResponse;
import com.medicalcrm.backend.exception.NotFoundException;
import com.medicalcrm.backend.model.Doctor;
import com.medicalcrm.backend.model.Role;
import com.medicalcrm.backend.model.User;
import com.medicalcrm.backend.repository.AppointmentRepository;
import com.medicalcrm.backend.repository.DoctorRepository;
import com.medicalcrm.backend.repository.UserRepository;
import com.medicalcrm.backend.service.impl.DoctorServiceImpl;
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
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private User user;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("doctor1");
        user.setRole(Role.DOCTOR);

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setUser(user);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createDoctor_success(){

        CreateDoctorRequest request = new CreateDoctorRequest();
        request.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(doctorRepository.save(any())).thenReturn(doctor);

        DoctorResponse response = doctorService.createDoctor(request);

        assertNotNull(response);
        verify(doctorRepository).save(any());
    }

    @Test
    void createDoctor_userNotFound() {

        CreateDoctorRequest request = new CreateDoctorRequest();
        request.setUserId(99L);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> doctorService.createDoctor(request));
    }


    @Test
    void getProfile_success_owner() {

        mockAuthentication("doctor1", Role.DOCTOR);

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        DoctorResponse response = doctorService.getProfile(1L);

        assertNotNull(response);
    }

    @Test
    void getProfile_doctorNotFound() {

        mockAuthentication("doctor1", Role.DOCTOR);

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> doctorService.getProfile(1L));
    }

    @Test
    void getProfile_accessDenied() {

        mockAuthentication("otherUser", Role.DOCTOR);

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        assertThrows(AccessDeniedException.class,
                () -> doctorService.getProfile(1L));
    }

    @Test
    void updateProfile_success() {

        mockAuthentication("doctor1", Role.DOCTOR);

        UpdateDoctorRequest request = new UpdateDoctorRequest();

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        DoctorResponse response =
                doctorService.updateProfile(1L, request);

        assertNotNull(response);
    }

    @Test
    void updateProfile_accessDenied() {

        mockAuthentication("otherUser", Role.DOCTOR);

        UpdateDoctorRequest request = new UpdateDoctorRequest();

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        assertThrows(AccessDeniedException.class,
                () -> doctorService.updateProfile(1L, request));
    }

    @Test
    void getMyPatients_success() {

        mockAuthentication("doctor1", Role.DOCTOR);

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(appointmentRepository.findDistinctPatientsByDoctorId(1L))
                .thenReturn(java.util.List.of());

        var list = doctorService.getMyPatients(1L);

        assertNotNull(list);
    }

    @Test
    void getMyAppointments_success() {

        mockAuthentication("doctor1", Role.DOCTOR);

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(appointmentRepository.findByDoctorId(1L))
                .thenReturn(java.util.List.of());

        var list = doctorService.getMyAppointments(1L);

        assertNotNull(list);
    }

    @Test
    void getMyAppointmentHistory_success() {

        mockAuthentication("doctor1", Role.DOCTOR);

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(appointmentRepository
                .findByDoctorIdAndStatus(any(), any()))
                .thenReturn(java.util.List.of());

        var list = doctorService.getMyAppointmentHistory(1L);

        assertNotNull(list);
    }

    @Test
    void getMyUpcomingAppointments_success() {

        mockAuthentication("doctor1", Role.DOCTOR);

        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(doctor));

        when(appointmentRepository
                .findByDoctorIdAndStatus(any(), any()))
                .thenReturn(java.util.List.of());

        var list = doctorService.getMyUpcomingAppointments(1L);

        assertNotNull(list);
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
