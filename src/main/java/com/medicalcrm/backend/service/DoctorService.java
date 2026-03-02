package com.medicalcrm.backend.service;

import com.medicalcrm.backend.dto.request.CreateDoctorRequest;
import com.medicalcrm.backend.dto.request.UpdateDoctorRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.dto.response.DoctorResponse;
import com.medicalcrm.backend.dto.response.PatientResponse;
import com.medicalcrm.backend.model.*;

import java.util.List;

public interface DoctorService {

    DoctorResponse createMyProfile(CreateDoctorRequest request);

    DoctorResponse getMyProfile();

    DoctorResponse updateMyProfile(UpdateDoctorRequest request);

    List<PatientResponse> getMyPatients();


}
