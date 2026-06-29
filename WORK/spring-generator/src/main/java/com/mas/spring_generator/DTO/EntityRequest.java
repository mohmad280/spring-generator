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
public class EntityRequest {

    private String name; // done
    private List<FieldRequest> fields; // done
    private List<RelationRequest> relations; //  done

}
