package com.mas.spring_generator.DTO;

import lombok.Data;

@Data
public class RelationRequest {

    private RelationType type;

    private String targetEntity;

    private String fieldName;

    private String mappedBy; // مهم للعكس (OneToMany)

}
