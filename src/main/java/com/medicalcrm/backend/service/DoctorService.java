package com.medicalcrm.backend.service;

import com.medicalcrm.backend.dto.request.CreateDoctorRequest;
import com.medicalcrm.backend.dto.request.UpdateDoctorRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.dto.response.DoctorResponse;
import com.medicalcrm.backend.dto.response.PatientResponse;
import com.medicalcrm.backend.model.*;

import java.util.List;

public interface DoctorService {

    DoctorResponse createDoctor(CreateDoctorRequest request);

    DoctorResponse getProfile(Long doctorId);

    DoctorResponse updateProfile(Long doctorId, UpdateDoctorRequest request);

    List<PatientResponse> getMyPatients(Long doctorId);

    List<AppointmentResponse> getMyAppointments(Long doctorId);

    List<AppointmentResponse> getMyAppointmentHistory(Long doctorId);

    List<AppointmentResponse> getMyUpcomingAppointments(Long doctorId);

    void addNotes(Long doctorId,
                  Long appointmentId,
                  String notes);

}
