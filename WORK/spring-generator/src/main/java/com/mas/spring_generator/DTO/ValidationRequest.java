package com.mas.spring_generator.DTO;

import lombok.Data;

@Data
public class ValidationRequest {
    private ValidationType type;    // NOT_NULL, NOT_BLANK, EMAIL, SIZE, MIN, MAX, PATTERN

    private Integer min;
    private Integer max;
    private String regexp;
    private String message;

}
