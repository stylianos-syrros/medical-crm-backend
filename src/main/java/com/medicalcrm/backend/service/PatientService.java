package com.medicalcrm.backend.service;

import com.medicalcrm.backend.dto.request.CreatePatientRequest;
import com.medicalcrm.backend.dto.request.UpdatePatientRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.dto.response.DoctorResponse;
import com.medicalcrm.backend.dto.response.PatientResponse;
import com.medicalcrm.backend.model.*;

import java.util.List;

public interface PatientService {

    PatientResponse createPatient(CreatePatientRequest request);

    PatientResponse getProfile(Long patientId);

    PatientResponse updateProfile(Long patientId, UpdatePatientRequest request);

    List<DoctorResponse> getMyDoctors(Long patientId);

    List<AppointmentResponse> getMyAppointments(Long patientId);

    List<AppointmentResponse> getMyAppointmentHistory(Long patientId);

    List<AppointmentResponse> getMyUpcomingAppointments(Long patientId);

    void cancelAppointment(Long patientId,
                           Long appointmentId);
}
