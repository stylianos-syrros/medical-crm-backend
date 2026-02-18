package com.medicalcrm.backend.service;

import com.medicalcrm.backend.dto.request.CreatePaymentRequest;
import com.medicalcrm.backend.dto.response.PaymentResponse;
import com.medicalcrm.backend.exception.BusinessException;
import com.medicalcrm.backend.model.*;
import com.medicalcrm.backend.repository.*;
import com.medicalcrm.backend.service.impl.PaymentServiceImpl;

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
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private MedicalServiceRepository medicalServiceRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Patient patient;
    private Appointment appointment;
    private MedicalService service;
    private Doctor doctor;

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
        appointment.setDoctor(doctor);  // Σύνδεση doctor με appointment
        appointment.setService(service);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        mockAuthentication("patient1", Role.PATIENT);

        lenient().when(patientRepository.findById(1L))  // lenient mock
                .thenReturn(Optional.of(patient));

        lenient().when(doctorRepository.findById(2L))  // lenient mock
                .thenReturn(Optional.of(doctor));

        lenient().when(paymentRepository.sumAmountByAppointmentId(10L))  // lenient mock
                .thenReturn(BigDecimal.ZERO);

        lenient().when(appointmentRepository.findByPatientId(1L))
                .thenReturn(java.util.List.of(appointment));

        lenient().when(appointmentRepository.sumAppointmentPricesByPatient(1L))
                .thenReturn(BigDecimal.valueOf(100));

        lenient().when(paymentRepository.sumPaymentsByPatient(1L))
                .thenReturn(BigDecimal.valueOf(50));
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
    void makePayment_success() {

        mockAuthentication("patient1", Role.PATIENT);

        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(BigDecimal.valueOf(50));
        request.setAppointmentId(10L);

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(appointmentRepository.findById(10L))
                .thenReturn(Optional.of(appointment));

        when(paymentRepository.sumAmountByAppointmentId(10L))
                .thenReturn(BigDecimal.ZERO);

        when(paymentRepository.save(any()))
                .thenAnswer(invocation -> {
                    Payment payment = invocation.getArgument(0);
                    payment.setAppointment(appointment);  // Set appointment in the payment
                    return payment;
                });

        appointment.setDoctor(doctor);

        PaymentResponse response = paymentService.makePayment(1L, request);

        assertNotNull(response);
    }


    @Test
    void makePayment_exceedsAmount() {

        mockAuthentication("patient1", Role.PATIENT);

        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(BigDecimal.valueOf(200)); // Exceeds price
        request.setAppointmentId(10L);

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(appointmentRepository.findById(10L))
                .thenReturn(Optional.of(appointment));

        when(paymentRepository.sumAmountByAppointmentId(10L))
                .thenReturn(BigDecimal.valueOf(50));

        assertThrows(BusinessException.class,
                () -> paymentService.makePayment(1L, request));
    }

    @Test
    void makePayment_alreadyPaid() {

        mockAuthentication("patient1", Role.PATIENT);

        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(BigDecimal.valueOf(50)); // Already fully paid
        request.setAppointmentId(10L);

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(appointmentRepository.findById(10L))
                .thenReturn(Optional.of(appointment));

        when(paymentRepository.sumAmountByAppointmentId(10L))
                .thenReturn(BigDecimal.valueOf(100)); // Fully paid already

        assertThrows(BusinessException.class,
                () -> paymentService.makePayment(1L, request));
    }

    @Test
    void getPaidAppointmentsByPatient_success() {

        mockAuthentication("patient1", Role.PATIENT);

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(appointmentRepository.findByPatientId(1L))
                .thenReturn(java.util.List.of(appointment));

        when(paymentRepository.sumAmountByAppointmentId(10L))
                .thenReturn(BigDecimal.valueOf(100));

        var appointments = paymentService.getPaidAppointmentsByPatient(1L);

        assertEquals(1, appointments.size());
    }

    @Test
    void getUnpaidAppointmentsByPatient_success() {

        mockAuthentication("patient1", Role.PATIENT);

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(appointmentRepository.findByPatientId(1L))
                .thenReturn(java.util.List.of(appointment));

        when(paymentRepository.sumAmountByAppointmentId(10L))
                .thenReturn(BigDecimal.ZERO);

        var appointments = paymentService.getUnpaidAppointmentsByPatient(1L);

        assertEquals(1, appointments.size());
    }

    @Test
    void getTotalPaidByPatient_success() {

        when(paymentRepository.sumPaymentsByPatient(1L))
                .thenReturn(BigDecimal.valueOf(100));

        BigDecimal totalPaid = paymentService.getTotalPaidByPatient(1L);

        assertEquals(BigDecimal.valueOf(100), totalPaid);
    }

    @Test
    void getTotalPendingByPatient_success() {

        when(paymentRepository.sumPaymentsByPatient(1L))
                .thenReturn(BigDecimal.valueOf(50));

        when(appointmentRepository.sumAppointmentPricesByPatient(1L))
                .thenReturn(BigDecimal.valueOf(100));

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));  // Ensure patient is returned from repo

        BigDecimal totalPending = paymentService.getTotalPendingByPatient(1L);

        assertEquals(BigDecimal.valueOf(50), totalPending);
    }
}