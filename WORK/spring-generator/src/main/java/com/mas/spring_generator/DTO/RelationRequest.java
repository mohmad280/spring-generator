package com.mas.spring_generator.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelationRequest {

    private RelationType type;

    private String targetEntity;

    private String fieldName;

    private String mappedBy; // مهم للعكس (OneToMany)

    // من اي انتتي اجا
    private String sourceEntity;
}
