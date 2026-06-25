package com.mas.spring_generator.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class ProjectRequestWithERD {

    @NotBlank(message = "project name is mandatory")
    private String projectName;

    @NotBlank(message = "package name is mandatory")
    private String packageName;

    private String databaseName; // ممكن ما ينضاف

    private DatabaseType database;

    private boolean userFeature;

    private boolean securityFeature;

    private boolean jwtFeature;

    private boolean roleFeature;

    private Set<String> roles;

    private String defaultRole;

    private boolean swaggerFeature;

}
