package com.mas.spring_generator.service.generator;

import com.mas.spring_generator.DTO.*;
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
                        .map(this::generateFieldCode)
                        .collect(Collectors.joining("\n\n"));

        String relationsCode = entity.getRelations() == null ? "" :
                entity.getRelations()
                        .stream()
                        .map(r -> generateRelationCode(entity.getName(), r))
                        .collect(Collectors.joining("\n\n"));

        return """
            package %s.entity;

            import jakarta.persistence.*;
            import jakarta.validation.constraints.*;
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


    private String generateRelationCode(
            String entityName,
            RelationRequest r
    ) {

        if (r.getTargetEntity() == null)
            throw new IllegalArgumentException("Target entity is required");

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

            case ONE_TO_ONE -> """
                @OneToOne
                @JoinColumn(name = "%s_id")
                private %s %s;
            """.formatted(
                    r.getFieldName(),
                    r.getTargetEntity(),
                    r.getFieldName()
            );

            case MANY_TO_MANY -> """
                @ManyToMany
                @JoinTable(
                    name = "%s_%s",
                    joinColumns = @JoinColumn(name = "%s_id"),
                    inverseJoinColumns = @JoinColumn(name = "%s_id")
                )
                private List<%s> %s;
            """.formatted(
                    entityName.toLowerCase(),
                    r.getTargetEntity().toLowerCase(),

                    entityName.toLowerCase(),
                    r.getTargetEntity().toLowerCase(),

                    r.getTargetEntity(),
                    r.getFieldName()
            );

            default -> "// not implemented yet";
        };
    }




    private String generateFieldCode(FieldRequest field) {

        String validationsCode = "";

        if (field.getValidations() != null && !field.getValidations().isEmpty()) {
            validationsCode = field.getValidations()
                    .stream()
                    .map(this::generateValidationAnnotation)
                    .collect(Collectors.joining("\n"));
        }

        if (!validationsCode.isBlank()) {
            return """
                    %s
                    private %s %s;
                """.formatted(
                    validationsCode,
                    field.getType(),
                    field.getName()
            );
        }

        return "    private " + field.getType() + " " + field.getName() + ";";
    }





    private String generateValidationAnnotation(ValidationRequest validation) {

        String messagePart = validation.getMessage() != null && !validation.getMessage().isBlank()
                ? "(message = \"" + validation.getMessage() + "\")"
                : "";

        return switch (validation.getType()) {
            case NOT_NULL -> "    @NotNull" + messagePart;
            case NOT_BLANK -> "    @NotBlank" + messagePart;
            case EMAIL -> "    @Email" + messagePart;

            case MIN -> {
                String message = validation.getMessage() != null && !validation.getMessage().isBlank()
                        ? ", message = \"" + validation.getMessage() + "\""
                        : "";

                yield "    @Min(value = " + validation.getMin() + message + ")";
            }

            case MAX -> {
                String message = validation.getMessage() != null && !validation.getMessage().isBlank()
                        ? ", message = \"" + validation.getMessage() + "\""
                        : "";

                yield "    @Max(value = " + validation.getMax() + message + ")";
            }

            case SIZE -> {
                String min = validation.getMin() != null ? "min = " + validation.getMin() : "";
                String max = validation.getMax() != null ? "max = " + validation.getMax() : "";

                String comma = !min.isBlank() && !max.isBlank() ? ", " : "";
                String message = validation.getMessage() != null && !validation.getMessage().isBlank()
                        ? (!min.isBlank() || !max.isBlank() ? ", " : "") + "message = \"" + validation.getMessage() + "\""
                        : "";

                yield "    @Size(" + min + comma + max + message + ")";
            }

            case PATTERN -> {
                String message = validation.getMessage() != null && !validation.getMessage().isBlank()
                        ? ", message = \"" + validation.getMessage() + "\""
                        : "";

                yield "    @Pattern(regexp = \"" + validation.getRegexp() + "\"" + message + ")";
            }
            case UNIQUE -> null; //todo بدها تكميل
        };
    }






}
