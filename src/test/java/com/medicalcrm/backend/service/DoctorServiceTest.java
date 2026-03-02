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
import com.medicalcrm.backend.repository.PatientRepository;
import com.medicalcrm.backend.repository.UserRepository;
import com.medicalcrm.backend.service.impl.DoctorServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;
    
    @Mock
    private PatientRepository patientRepository;

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
        doctor.setPhone("123");

    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createDoctor_success(){
        mockAuthentication("doctor1", Role.DOCTOR);

        CreateDoctorRequest request = new CreateDoctorRequest();
        request.setPhone("555");

        when(doctorRepository.existsByPhoneAndIdNot("555", -1L)).thenReturn(false);
        when(patientRepository.findByPhone("555")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("doctor1")).thenReturn(Optional.of(user));
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(doctorRepository.save(any())).thenReturn(doctor);

        DoctorResponse response = doctorService.createMyProfile(request);

        assertNotNull(response);
        verify(doctorRepository).save(any());
    }

    @Test
    void createMyProfile_userNotFound() {
        mockAuthentication("doctor1", Role.DOCTOR);

        CreateDoctorRequest request = new CreateDoctorRequest();

        when(userRepository.findByUsername("doctor1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> doctorService.createMyProfile(request));
    }


    @Test
    void getMyProfile_success() {

        mockAuthentication("doctor1", Role.DOCTOR);

        when(userRepository.findByUsername("doctor1"))
            .thenReturn(Optional.of(user));

        when(doctorRepository.findByUserId(1L))
                .thenReturn(Optional.of(doctor));

        DoctorResponse response = doctorService.getMyProfile();

        assertNotNull(response);
    }

    @Test
    void getMyProfile_doctorNotFound() {

        mockAuthentication("doctor1", Role.DOCTOR);

        when(userRepository.findByUsername("doctor1"))
            .thenReturn(Optional.of(user));

        when(doctorRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> doctorService.getMyProfile());
    }

    @Test
    void getMyProfile_userNotFound() {

        mockAuthentication("doctor1", Role.DOCTOR);

        when(userRepository.findByUsername("doctor1"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> doctorService.getMyProfile());
    }


    @Test
    void updateMyProfile_success() {
                
        mockAuthentication("doctor1", Role.DOCTOR);
  
        UpdateDoctorRequest request = new UpdateDoctorRequest();
        request.setPhone("123");

        when(userRepository.findByUsername("doctor1"))
                .thenReturn(Optional.of(user));
        when(doctorRepository.findByUserId(1L))
                .thenReturn(Optional.of(doctor));

        DoctorResponse response = doctorService.updateMyProfile(request);

        assertNotNull(response);
    }


    @Test
    void getMyPatients_success() {

        mockAuthentication("doctor1", Role.DOCTOR);

        when(userRepository.findByUsername("doctor1")).thenReturn(Optional.of(user));

        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(doctor));

        var list = doctorService.getMyPatients();

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
