package com.medicalcrm.backend.dto.request;

import com.medicalcrm.backend.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Data
public class ChangeRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;
}