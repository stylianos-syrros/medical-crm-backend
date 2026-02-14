package com.medicalcrm.backend.dto.response;

import lombok.Data;

@Data
public class DoctorResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String specialty;
    private String phone;
}
