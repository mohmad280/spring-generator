package com.mas.spring_generator.service.impl;

import com.mas.spring_generator.DTO.ProjectRequest;
import com.mas.spring_generator.service.ProjectGeneratorService;
import com.mas.spring_generator.service.generator.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;




@Service
@RequiredArgsConstructor
public class ProjectGeneratorServiceImpl implements ProjectGeneratorService {

    private final BaseProjectGenerator baseProjectGenerator;
    private final ZipHelper zipHelper;
    private final MainClassGenerator mainClassGenerator;
    private final UserFeatureGenerator userFeatureGenerator;
    private final SecurityFeatureGenerator securityFeatureGenerator;
    private final JwtFeatureGenerator jwtFeatureGenerator;
    private final PropertiesGenerator propertiesGenerator;
    private final RoleFeatureGenerator roleFeatureGenerator;
    private final SwaggerFeatureGenerator swaggerFeatureGenerator;

    @Override
    public byte[] generate(ProjectRequest request) {
        validateRequest(request);
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(outputStream);

            // نسخ المشروع الأساسي
            baseProjectGenerator.copyBaseProject(zip, request);

            mainClassGenerator.addMainApplicationClass(zip, request);

            // توليد application.properties حسب قاعدة البيانات المختارة
            zipHelper.addFile(
                    zip,
                    request.getProjectName() + "/src/main/resources/application.properties",
                    propertiesGenerator.generateApplicationProperties(request)
            );

            // إضافة User Feature إذا مختارة
            if (request.isUserFeature()) {
                userFeatureGenerator.addUserFeature(zip, request);
            }

            // add role
            if (request.isRoleFeature()) {
                roleFeatureGenerator.addRoleFeature(zip,request);
            }

            // add security base
            if(request.isSecurityFeature()) {
                securityFeatureGenerator.addSecurityFeature(zip, request);
            }
            // add jwt
            if(request.isJwtFeature() && request.isSecurityFeature()) {
                jwtFeatureGenerator.addJwtFeature(zip, request);
            }

            if (request.isSwaggerFeature()) {
                swaggerFeatureGenerator.addSwaggerFeature(zip, request);
            }

            zip.close();
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate project", e);
        }
    }


    private void validateRequest(ProjectRequest request) {
        // Validation User
        if (request.isRoleFeature() && !request.isUserFeature()) {
            throw new IllegalArgumentException("Role feature requires User feature");
        }

        // Validation Security
        if (request.isJwtFeature() && !request.isSecurityFeature()) {
            throw new IllegalArgumentException("JWT feature requires Security feature");
        }

        // Validation JWT
        if (request.isJwtFeature() && !request.isUserFeature()) {
            throw new IllegalArgumentException("JWT feature requires User feature");
        }

        // Validation role
        if (request.isRoleFeature()) {
            if (request.getRoles() == null || request.getRoles().isEmpty()) {
                throw new IllegalArgumentException("Roles list is required when Role feature is enabled");
            }

            if (request.getDefaultRole() == null || request.getDefaultRole().isBlank()) {
                throw new IllegalArgumentException("Default role is required when Role feature is enabled");
            }

            boolean exists = request.getRoles()
                    .stream()
                    .anyMatch(role -> role.equalsIgnoreCase(request.getDefaultRole()));

            if (!exists) {
                throw new IllegalArgumentException("Default role must exist in roles list");
            }
        }

        // Validation -------

    }



}
