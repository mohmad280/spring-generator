package com.mas.spring_generator.service.impl;

import com.mas.spring_generator.DTO.ProjectRequest;
import com.mas.spring_generator.DTO.ProjectRequestWithERD;
import com.mas.spring_generator.service.ProjectGeneratorService;
import com.mas.spring_generator.service.generator.*;
import com.mas.spring_generator.service.parser.ErdParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;


@Service
@RequiredArgsConstructor
public class ProjectGeneratorServiceImpl implements ProjectGeneratorService {

    private final EntityGenerator entityGenerator;
    private final BaseProjectGenerator baseProjectGenerator;
    private final ZipHelper zipHelper;
    private final MainClassGenerator mainClassGenerator;
    private final UserFeatureGenerator userFeatureGenerator;
    private final SecurityFeatureGenerator securityFeatureGenerator;
    private final JwtFeatureGenerator jwtFeatureGenerator;
    private final PropertiesGenerator propertiesGenerator;
    private final RoleFeatureGenerator roleFeatureGenerator;
    private final SwaggerFeatureGenerator swaggerFeatureGenerator;
    // parser
    private final ErdParser erdParser;

    @Override
    public byte[] generate(ProjectRequest request) {
        validateRequest(request);
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); // عبارة عن ملف ZIP داخل الذاكرة.
            ZipOutputStream zip = new ZipOutputStream(outputStream); // لكتابة الملفات داخل الـ ZIP.

            // نسخ المشروع الأساسي
            baseProjectGenerator.copyBaseProject(zip, request);

            mainClassGenerator.addMainApplicationClass(zip, request);

            // انشاء الانتتي
            entityGenerator.addEntities(zip, request);

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
                roleFeatureGenerator.addRoleFeature(zip, request);
            }

            // add security base
            if (request.isSecurityFeature()) {
                securityFeatureGenerator.addSecurityFeature(zip, request);
            }
            // add jwt
            if (request.isJwtFeature() && request.isSecurityFeature()) {
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


    // مع ال ERD
    @Override
    public byte[] generateWithERD(ProjectRequestWithERD request, MultipartFile erdFile) {

        // الان معطله بحاجه الى اعادة كتابه ب ال dto الجديد فقط
        //validateRequest(request);
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); // عبارة عن ملف ZIP داخل الذاكرة.
            ZipOutputStream zip = new ZipOutputStream(outputStream); // لكتابة الملفات داخل الـ ZIP.

            // نسخ المشؤوع من الريسورسز و اضاف المكاتب و الاسماء المطلوبه من العميل
            baseProjectGenerator.copyBaseProjectWithERD(zip, request);

            mainClassGenerator.addMainApplicationClassWithERD(zip, request);


            ProjectRequest projectRequest = ProjectRequest.builder()
                    .projectName(request.getProjectName())
                    .packageName(request.getPackageName())
                    .databaseName(request.getDatabaseName())
                    .database(request.getDatabase())
                    .userFeature(request.isUserFeature())
                    .securityFeature(request.isSecurityFeature())
                    .jwtFeature(request.isJwtFeature())
                    .roleFeature(request.isRoleFeature())
                    .roles(request.getRoles())
                    .defaultRole(request.getDefaultRole())
                    .swaggerFeature(request.isSwaggerFeature())
                    .entities(erdParser.parse(erdFile))
                    .build();


            // انشاء الانتتي
            entityGenerator.addEntities(zip, projectRequest);


            // توليد application.properties حسب قاعدة البيانات المختارة
            zipHelper.addFile(
                    zip,
                    request.getProjectName() + "/src/main/resources/application.properties",
                    propertiesGenerator.generateApplicationPropertiesWithERD(request)
            );

            // إضافة User Feature إذا مختارة
            if (request.isUserFeature()) {
                userFeatureGenerator.addUserFeatureWithERD(zip, request);
            }

            // add role
            if (request.isRoleFeature()) {
                roleFeatureGenerator.addRoleFeatureWithERD(zip, request);
            }

            // add security base
            if (request.isSecurityFeature()) {
                securityFeatureGenerator.addSecurityFeatureWithERD(zip, request);
            }

            // add jwt
            if (request.isJwtFeature() && request.isSecurityFeature()) {
                jwtFeatureGenerator.addJwtFeatureWithERD(zip, request);
            }

            if (request.isSwaggerFeature()) {
                swaggerFeatureGenerator.addSwaggerFeatureWithERD(zip, request);
            }

            zip.close();
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
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
