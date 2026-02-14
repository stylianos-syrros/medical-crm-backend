package com.medicalcrm.backend.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MedicalServiceResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer duration;
}
