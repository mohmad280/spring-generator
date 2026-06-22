package com.mas.spring_generator.DTO;

import lombok.Data;

import java.util.List;

@Data
public class EntityRequest {

    private String name; // done
    private List<FieldRequest> fields; // done
    private List<RelationRequest> relations; // 50% done

}
