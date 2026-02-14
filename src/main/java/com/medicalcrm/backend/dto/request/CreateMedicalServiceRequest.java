package com.medicalcrm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateMedicalServiceRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull
    @Positive(message = "Duration must be positive")
    private Integer duration;
}
