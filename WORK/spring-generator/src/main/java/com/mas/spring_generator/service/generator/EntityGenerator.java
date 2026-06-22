package com.mas.spring_generator.service.generator;

import com.mas.spring_generator.DTO.EntityRequest;
import com.mas.spring_generator.DTO.ProjectRequest;
import com.mas.spring_generator.DTO.RelationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
public class EntityGenerator {

    private final ZipHelper zipHelper;

    public void addEntities (
            ZipOutputStream zip,
            ProjectRequest request
    ) throws IOException {

        if(request.getEntities()==null)
            return;

        for(EntityRequest entity : request.getEntities()) {
            generateEntity(zip, request, entity);
        }
    }


    private void generateEntity(
            ZipOutputStream zip,
            ProjectRequest request,
            EntityRequest entity
    ) throws IOException {

        String packagePath = request.getPackageName().replace(".", "/");

        String basePath = request.getProjectName()
                + "/src/main/java/"
                + packagePath;

        zipHelper.addFile(
                zip,
                basePath + "/entity/" + entity.getName() + ".java",
                generateEntityCode(request, entity)
        );
    }




    private String generateEntityCode(
            ProjectRequest request,
            EntityRequest entity
    ) {

        String fieldsCode = entity.getFields() == null ? "" :
                entity.getFields()
                        .stream()
                        .map(f -> "    private " + f.getType() + " " + f.getName() + ";")
                        .collect(Collectors.joining("\n"));

        String relationsCode = entity.getRelations() == null ? "" :
                entity.getRelations()
                        .stream()
                        .map(this::generateRelationCode)
                        .collect(Collectors.joining("\n"));

        return """
            package %s.entity;

            import jakarta.persistence.*;
            import lombok.*;
            import java.util.List;

            @Entity
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public class %s {

                @Id
                @GeneratedValue(strategy = GenerationType.IDENTITY)
                private Long id;

            %s

                %s
            }
            """.formatted(
                request.getPackageName(),
                entity.getName(),
                fieldsCode,
                relationsCode
        );
    }


    private String generateRelationCode(RelationRequest r) {

        return switch (r.getType()) {

            case MANY_TO_ONE -> """
            @ManyToOne
                @JoinColumn(name = "%s_id")
                private %s %s;
            """.formatted(
                    r.getFieldName(),
                    r.getTargetEntity(),
                    r.getFieldName()
            );

            case ONE_TO_MANY -> """
            @OneToMany(mappedBy = "%s")
                private List<%s> %s;
            """.formatted(
                    r.getMappedBy(),
                    r.getTargetEntity(),
                    r.getFieldName()
            );

            // todo many to many
            // todo one to one

            default -> "// not implemented yet";
        };
    }
}
