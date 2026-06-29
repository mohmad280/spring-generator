package com.mas.spring_generator.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldRequest {
    private String name;
    private FieldType type;

    // SUPPORT VALIDATION
    private List<ValidationRequest> validations;
}
