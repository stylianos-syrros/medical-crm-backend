package com.medicalcrm.backend.mapper;


import com.medicalcrm.backend.dto.request.CreatePatientRequest;
import com.medicalcrm.backend.dto.request.UpdatePatientRequest;
import com.medicalcrm.backend.dto.response.PatientResponse;
import com.medicalcrm.backend.model.Patient;
import com.medicalcrm.backend.model.User;

public class PatientMapper {

    public static Patient toEntity(CreatePatientRequest request, User user){

        Patient patient = new Patient();

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setNotes(request.getNotes());
        patient.setPhone(request.getPhone());
        patient.setUser(user);

        return patient;
    }

    public static PatientResponse toResponse(Patient patient){

        PatientResponse response = new PatientResponse();

        response.setId(patient.getId());
        response.setFirstName(patient.getFirstName());
        response.setLastName(patient.getLastName());
        response.setDateOfBirth(patient.getDateOfBirth());
        response.setNotes(patient.getNotes());
        response.setPhone(patient.getPhone());

        return response;
    }

    public static void updateEntity(Patient patient,
                                    UpdatePatientRequest request) {

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setPhone(request.getPhone());
        patient.setNotes(request.getNotes());
        patient.setDateOfBirth(request.getDateOfBirth());
    }

}
