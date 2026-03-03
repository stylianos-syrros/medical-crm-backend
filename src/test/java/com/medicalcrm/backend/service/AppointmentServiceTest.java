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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private User patientUser;
    private User doctorUser;
    private Patient patient;
    private Doctor doctor;
    private Appointment appointment;
    private MedicalService service;

    @BeforeEach
    void setUp() {
        patientUser = new User();
        patientUser.setId(100L);
        patientUser.setUsername("patient1");
        patientUser.setRole(Role.PATIENT);

        patient = new Patient();
        patient.setId(1L);
        patient.setUser(patientUser);

        doctorUser = new User();
        doctorUser.setId(200L);
        doctorUser.setUsername("doctor1");
        doctorUser.setRole(Role.DOCTOR);

        doctor = new Doctor();
        doctor.setId(2L);
        doctor.setUser(doctorUser);

        service = new MedicalService();
        service.setId(3L);
        service.setPrice(BigDecimal.valueOf(100));
        service.setDuration(30);

        appointment = new Appointment();
        appointment.setId(10L);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setService(service);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setAppointmentDate(LocalDate.now().minusDays(1));
        appointment.setAppointmentTime(LocalTime.of(10, 0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthentication(String username, Role role) {
        TestingAuthenticationToken auth = new TestingAuthenticationToken(
                username,
                null,
                "ROLE_" + role.name()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void mockPatientAuth() {
        mockAuthentication("patient1", Role.PATIENT);
        when(userRepository.findByUsername("patient1")).thenReturn(Optional.of(patientUser));
        when(patientRepository.findByUserId(100L)).thenReturn(Optional.of(patient));
    }

    private void mockDoctorAuth() {
        mockAuthentication("doctor1", Role.DOCTOR);
        when(userRepository.findByUsername("doctor1")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(200L)).thenReturn(Optional.of(doctor));
    }

    // PATIENT

    @Test
    void bookAppointment_success() {
        mockPatientAuth();

        CreateAppointmentRequest request = new CreateAppointmentRequest();
        request.setDoctorId(2L);
        request.setServiceId(3L);
        request.setAppointmentDate(LocalDate.now().plusDays(1));
        request.setAppointmentTime(LocalTime.of(10, 0));
        request.setNotes("test");

        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(medicalServiceRepository.findById(3L)).thenReturn(Optional.of(service));

        when(appointmentRepository.findByPatientIdAndAppointmentDateAndStatus(
                1L, request.getAppointmentDate(), AppointmentStatus.SCHEDULED))
                .thenReturn(List.of());

        when(appointmentRepository.findByDoctorIdAndAppointmentDateAndStatus(
                2L, request.getAppointmentDate(), AppointmentStatus.SCHEDULED))
                .thenReturn(List.of());

        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponse response = appointmentService.bookAppointment(request);

        assertNotNull(response);
        assertEquals(request.getAppointmentDate(), response.getAppointmentDate());
        assertEquals(request.getAppointmentTime(), response.getAppointmentTime());
    }

    @Test
    void cancelAppointment_success() {
        mockPatientAuth();
        appointment.setAppointmentDate(LocalDate.now().plusDays(1));
        appointment.setAppointmentTime(LocalTime.of(10, 0));

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(paymentRepository.existsByAppointmentId(10L)).thenReturn(false);

        appointmentService.cancelAppointmentByPatient(10L);

        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
    }

    @Test
    void cancelAppointment_paid() {
        mockPatientAuth();
        appointment.setAppointmentDate(LocalDate.now().plusDays(1));
        appointment.setAppointmentTime(LocalTime.of(10, 0));

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(paymentRepository.existsByAppointmentId(10L)).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> appointmentService.cancelAppointmentByPatient(10L));
    }

    @Test
    void rescheduleAppointment_success() {
        mockPatientAuth();

        UpdateAppointmentScheduleRequest request = new UpdateAppointmentScheduleRequest();
        request.setAppointmentDate(LocalDate.now().plusDays(2));
        request.setAppointmentTime(LocalTime.of(11, 0));

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));

        when(appointmentRepository.findByPatientIdAndAppointmentDateAndStatus(
                1L, request.getAppointmentDate(), AppointmentStatus.SCHEDULED))
                .thenReturn(List.of(appointment));

        when(appointmentRepository.findByDoctorIdAndAppointmentDateAndStatus(
                2L, request.getAppointmentDate(), AppointmentStatus.SCHEDULED))
                .thenReturn(List.of(appointment));

        appointmentService.rescheduleAppointment(10L, request);

        assertEquals(request.getAppointmentDate(), appointment.getAppointmentDate());
        assertEquals(request.getAppointmentTime(), appointment.getAppointmentTime());
    }

    // DOCTOR

    @Test
    void completeAppointment_success() {
        mockDoctorAuth();

        appointment.setAppointmentDate(LocalDate.now().minusDays(1));
        appointment.setAppointmentTime(LocalTime.of(9, 30));

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(paymentRepository.sumAmountByAppointmentId(10L)).thenReturn(BigDecimal.valueOf(100));

        appointmentService.completeAppointmentByDoctor(10L);

        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
    }

    @Test
    void completeAppointment_notFullyPaid() {
        mockDoctorAuth();

        appointment.setAppointmentDate(LocalDate.now().minusDays(1));
        appointment.setAppointmentTime(LocalTime.of(9, 30));

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(paymentRepository.sumAmountByAppointmentId(10L)).thenReturn(BigDecimal.valueOf(50));

        assertThrows(BusinessException.class,
                () -> appointmentService.completeAppointmentByDoctor(10L));
    }

    @Test
    void updateNotesByDoctor_success() {
        mockDoctorAuth();

        UpdateAppointmentNotesRequest request = new UpdateAppointmentNotesRequest();
        request.setNotes("Updated notes by doctor");

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));

        appointmentService.updateNotesByDoctor(appointment.getId(), request);

        assertEquals("Updated notes by doctor", appointment.getDoctorNotes());
    }

    // ADMIN

    @Test
    void getAllAppointments_success() {
        when(appointmentRepository.findAll()).thenReturn(List.of(appointment));

        var list = appointmentService.getAllAppointments();

        assertEquals(1, list.size());
    }
}
