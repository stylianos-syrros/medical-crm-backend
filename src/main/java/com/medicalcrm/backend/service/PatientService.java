package com.medicalcrm.backend.service;

import com.medicalcrm.backend.dto.request.CreatePatientRequest;
import com.medicalcrm.backend.dto.request.UpdatePatientRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.dto.response.DoctorResponse;
import com.medicalcrm.backend.dto.response.PatientResponse;
import com.medicalcrm.backend.model.*;

import java.util.List;

public interface PatientService {

    PatientResponse createMyProfile(CreatePatientRequest request);

    PatientResponse getMyProfile();

    PatientResponse updateMyProfile(UpdatePatientRequest request);

    List<DoctorResponse> getMyDoctors();

    /*List<AppointmentResponse> getMyAppointments();

    List<AppointmentResponse> getMyAppointmentsHistory(); 

    List<AppointmentResponse> getMyUpcomingAppointments();

    void cancelAppointment(Long appointmentId);*/
}
