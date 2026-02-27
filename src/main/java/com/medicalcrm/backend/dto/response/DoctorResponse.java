package com.medicalcrm.backend.dto.response;

import lombok.Data;
import com.medicalcrm.backend.model.DoctorSpecialty;

@Data
public class DoctorResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private DoctorSpecialty specialty;
    private String phone;
}
