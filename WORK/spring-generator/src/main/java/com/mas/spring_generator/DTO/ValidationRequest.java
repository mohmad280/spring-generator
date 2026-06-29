package com.mas.spring_generator.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidationRequest {
    private ValidationType type;    // NOT_NULL, NOT_BLANK, EMAIL, SIZE, MIN, MAX, PATTERN

    private Integer min;
    private Integer max;
    private String regexp;
    private String message;

}
