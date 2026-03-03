package com.medicalcrm.backend.dto.response;

import com.medicalcrm.backend.model.Role;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Long id;

    private String username;

    private String email;

    private Role role;

    private boolean enabled;

    private LocalDateTime createdAt;

    private boolean hasDoctorProfile;

    private boolean hasPatientProfile;

    private Long doctorProfileId;

    private Long patientProfileId;
}
