package com.mas.spring_generator.DTO;

import lombok.Data;

import java.util.List;

@Data
public class FieldRequest {
    private String name;
    private FieldType type;

    // SUPPORT VALIDATION
    private List<ValidationRequest> validations;
}
