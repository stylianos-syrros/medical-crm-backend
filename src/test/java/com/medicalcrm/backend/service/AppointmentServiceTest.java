package com.medicalcrm.backend.service;

import com.medicalcrm.backend.dto.request.CreateAppointmentRequest;
import com.medicalcrm.backend.dto.request.UpdateAppointmentNotesRequest;
import com.medicalcrm.backend.dto.request.UpdateAppointmentScheduleRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.exception.BusinessException;
import com.medicalcrm.backend.model.*;
import com.medicalcrm.backend.repository.*;
import com.medicalcrm.backend.service.impl.AppointmentServiceImpl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private MedicalServiceRepository medicalServiceRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Patient patient;
    private Doctor doctor;
    private Appointment appointment;
    private MedicalService service;

    @BeforeEach
    void setUp() {

        User patientUser = new User();
        patientUser.setUsername("patient1");
        patientUser.setRole(Role.PATIENT);

        patient = new Patient();
        patient.setId(1L);
        patient.setUser(patientUser);

        User doctorUser = new User();
        doctorUser.setUsername("doctor1");
        doctorUser.setRole(Role.DOCTOR);

        doctor = new Doctor();
        doctor.setId(2L);
        doctor.setUser(doctorUser);

        service = new MedicalService();
        service.setId(3L);
        service.setPrice(BigDecimal.valueOf(100));

        appointment = new Appointment();
        appointment.setId(10L);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setService(service);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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

    // PATIENT

    @Test
    void bookAppointment_success() {

        mockAuthentication("patient1", Role.PATIENT);

        CreateAppointmentRequest request =
                new CreateAppointmentRequest();
        request.setDoctorId(2L);
        request.setServiceId(3L);

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(doctorRepository.findById(2L))
                .thenReturn(Optional.of(doctor));

        when(medicalServiceRepository.findById(3L))
                .thenReturn(Optional.of(service));

        when(appointmentRepository.save(any()))
                .thenReturn(appointment);

        AppointmentResponse response =
                appointmentService.bookAppointment(1L, request);

        assertNotNull(response);
    }

    @Test
    void cancelAppointment_success() {

        mockAuthentication("patient1", Role.PATIENT);

        when(appointmentRepository.findById(10L))
                .thenReturn(Optional.of(appointment));

        when(paymentRepository.existsByAppointmentId(10L))
                .thenReturn(false);

        appointmentService.cancelAppointment(1L, 10L);

        assertEquals(AppointmentStatus.CANCELLED,
                appointment.getStatus());
    }

    @Test
    void cancelAppointment_paid() {

        mockAuthentication("patient1", Role.PATIENT);

        when(appointmentRepository.findById(10L))
                .thenReturn(Optional.of(appointment));

        when(paymentRepository.existsByAppointmentId(10L))
                .thenReturn(true);

        assertThrows(BusinessException.class,
                () -> appointmentService.cancelAppointment(1L, 10L));
    }

    @Test
    void rescheduleAppointment_success() {

        mockAuthentication("patient1", Role.PATIENT);

        UpdateAppointmentScheduleRequest request =
                new UpdateAppointmentScheduleRequest();

        when(appointmentRepository.findById(10L))
                .thenReturn(Optional.of(appointment));

        appointmentService
                .rescheduleAppointment(1L, 10L, request);

        assertEquals(AppointmentStatus.SCHEDULED,
                appointment.getStatus());
    }

    // DOCTOR

    @Test
    void completeAppointment_success() {

        mockAuthentication("doctor1", Role.DOCTOR);

        when(appointmentRepository.findById(10L))
                .thenReturn(Optional.of(appointment));

        when(doctorRepository.findById(2L))
                .thenReturn(Optional.of(doctor));

        when(paymentRepository.sumAmountByAppointmentId(10L))
                .thenReturn(BigDecimal.valueOf(100));

        appointmentService.completeAppointmentByDoctor(2L, 10L);

        assertEquals(AppointmentStatus.COMPLETED,
                appointment.getStatus());
    }

    @Test
    void completeAppointment_notFullyPaid() {

        mockAuthentication("doctor1", Role.DOCTOR);

        when(appointmentRepository.findById(10L))
                .thenReturn(Optional.of(appointment));

        when(doctorRepository.findById(2L))
                .thenReturn(Optional.of(doctor));

        when(paymentRepository.sumAmountByAppointmentId(10L))
                .thenReturn(BigDecimal.valueOf(50));

        assertThrows(BusinessException.class,
                () -> appointmentService
                        .completeAppointmentByDoctor(2L, 10L));
    }

    @Test
    void updateNotesByDoctor_success() {

        mockAuthentication("doctor1", Role.DOCTOR);

        UpdateAppointmentNotesRequest request = new UpdateAppointmentNotesRequest();
        request.setNotes("Updated notes by doctor");

        when(appointmentRepository.findById(10L))
                .thenReturn(Optional.of(appointment));

        lenient().when(doctorRepository.findById(2L))
                .thenReturn(Optional.of(doctor));

        appointmentService.updateNotesByDoctor(doctor.getId(), appointment.getId(), request);

        assertEquals("Updated notes by doctor",
                appointment.getNotes());
    }

    // ADMIN

    @Test
    void getAllAppointments_success() {

        when(appointmentRepository.findAll())
                .thenReturn(java.util.List.of(appointment));

        var list = appointmentService.getAllAppointments();

        assertEquals(1, list.size());
    }


}

