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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

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
    private UserRepository userRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User patientUser;
    private Patient patient;
    private Doctor doctor;
    private MedicalService medicalService;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        patientUser = new User();
        patientUser.setId(100L);
        patientUser.setUsername("patient1");
        patientUser.setRole(Role.PATIENT);

        patient = new Patient();
        patient.setId(1L);
        patient.setUser(patientUser);

        User doctorUser = new User();
        doctorUser.setId(200L);
        doctorUser.setUsername("doctor1");
        doctorUser.setRole(Role.DOCTOR);

        doctor = new Doctor();
        doctor.setId(2L);
        doctor.setUser(doctorUser);

        medicalService = new MedicalService();
        medicalService.setId(3L);
        medicalService.setPrice(BigDecimal.valueOf(100));

        appointment = new Appointment();
        appointment.setId(10L);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setService(medicalService);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        mockAuthentication("patient1", Role.PATIENT);

        lenient().when(userRepository.findByUsername("patient1"))
                .thenReturn(Optional.of(patientUser));

        lenient().when(patientRepository.findByUserId(100L))
                .thenReturn(Optional.of(patient));

        lenient().when(appointmentRepository.findByPatientId(1L))
                .thenReturn(List.of(appointment));

        lenient().when(paymentRepository.sumAmountByAppointmentId(10L))
                .thenReturn(BigDecimal.ZERO);

        lenient().when(paymentRepository.sumPaymentsByPatient(1L))
                .thenReturn(BigDecimal.valueOf(50));

        lenient().when(appointmentRepository.sumAppointmentPricesByPatient(1L))
                .thenReturn(BigDecimal.valueOf(100));
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

    @Test
    void makePayment_success() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAppointmentId(10L);
        request.setAmount(BigDecimal.valueOf(50));
        request.setPaymentMethod(PaymentMethod.CARD);

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(paymentRepository.sumAmountByAppointmentId(10L)).thenReturn(BigDecimal.ZERO);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(999L);
            return p;
        });

        PaymentResponse response = paymentService.makePayment(request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(50), response.getAmount());
        assertEquals(10L, response.getAppointmentId());
    }

    @Test
    void makePayment_exceedsAmount_throwsBusinessException() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAppointmentId(10L);
        request.setAmount(BigDecimal.valueOf(200));
        request.setPaymentMethod(PaymentMethod.CARD);

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(paymentRepository.sumAmountByAppointmentId(10L)).thenReturn(BigDecimal.valueOf(50));

        assertThrows(BusinessException.class, () -> paymentService.makePayment(request));
    }

    @Test
    void makePayment_alreadyFullyPaid_throwsBusinessException() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAppointmentId(10L);
        request.setAmount(BigDecimal.valueOf(10));
        request.setPaymentMethod(PaymentMethod.CASH);

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(paymentRepository.sumAmountByAppointmentId(10L)).thenReturn(BigDecimal.valueOf(100));

        assertThrows(BusinessException.class, () -> paymentService.makePayment(request));
    }

    @Test
    void makePayment_cancelledAppointment_throwsBusinessException() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAppointmentId(10L);
        request.setAmount(BigDecimal.valueOf(10));
        request.setPaymentMethod(PaymentMethod.CARD);

        appointment.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));

        assertThrows(BusinessException.class, () -> paymentService.makePayment(request));
    }

    @Test
    void getPaidAppointmentsByPatient_success() {
        when(appointmentRepository.findByPatientId(1L)).thenReturn(List.of(appointment));
        when(paymentRepository.sumAmountByAppointmentId(10L)).thenReturn(BigDecimal.valueOf(100));

        var result = paymentService.getPaidAppointmentsByPatient();

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    void getUnpaidAppointmentsByPatient_success() {
        when(appointmentRepository.findByPatientId(1L)).thenReturn(List.of(appointment));
        when(paymentRepository.sumAmountByAppointmentId(10L)).thenReturn(BigDecimal.ZERO);

        var result = paymentService.getUnpaidAppointmentsByPatient();

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    void getTotalPaidByPatient_success() {
        when(paymentRepository.sumPaymentsByPatient(1L)).thenReturn(BigDecimal.valueOf(100));

        BigDecimal total = paymentService.getTotalPaidByPatient();

        assertEquals(BigDecimal.valueOf(100), total);
    }

    @Test
    void getTotalPendingByPatient_success() {
        when(paymentRepository.sumPaymentsByPatient(1L)).thenReturn(BigDecimal.valueOf(50));
        when(appointmentRepository.sumAppointmentPricesByPatient(1L)).thenReturn(BigDecimal.valueOf(100));

        BigDecimal total = paymentService.getTotalPendingByPatient();

        assertEquals(BigDecimal.valueOf(50), total);
    }
}
