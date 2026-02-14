package com.medicalcrm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import jakarta.validation.constraints.Min;

@Data
public class UpdateMedicalServiceRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive
    private BigDecimal price;

    @NotNull(message = "Price is required")
    @Min(1)
    private Integer duration;
}
