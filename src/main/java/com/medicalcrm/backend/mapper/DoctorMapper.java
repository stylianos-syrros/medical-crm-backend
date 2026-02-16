package com.medicalcrm.backend.mapper;

import com.medicalcrm.backend.dto.request.CreateDoctorRequest;
import com.medicalcrm.backend.dto.request.UpdateDoctorRequest;
import com.medicalcrm.backend.dto.response.DoctorResponse;
import com.medicalcrm.backend.model.Doctor;
import com.medicalcrm.backend.model.User;

public class DoctorMapper {

    public static Doctor toEntity(CreateDoctorRequest request, User user){

        Doctor doctor = new Doctor();

        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setSpecialty(request.getSpecialty());
        doctor.setPhone(request.getPhone());
        doctor.setUser(user);

        return doctor;
    }

    public static DoctorResponse toResponse(Doctor doctor) {

        DoctorResponse response = new DoctorResponse();

        response.setId(doctor.getId());
        response.setFirstName(doctor.getFirstName());
        response.setLastName(doctor.getLastName());
        response.setSpecialty(doctor.getSpecialty());
        response.setPhone(doctor.getPhone());

        return response;
    }

    public static void updateEntity(Doctor doctor,
                                    UpdateDoctorRequest request) {

        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setSpecialty(request.getSpecialty());
        doctor.setPhone(request.getPhone());
    }

}
