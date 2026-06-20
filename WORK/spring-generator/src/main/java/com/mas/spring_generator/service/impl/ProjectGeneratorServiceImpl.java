package com.mas.spring_generator.service.impl;

import com.mas.spring_generator.DTO.ProjectRequest;
import com.mas.spring_generator.service.ProjectGeneratorService;
import com.mas.spring_generator.service.generator.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import java.nio.file.*;

import static com.mas.spring_generator.DTO.DatabaseType.MYSQL;
import static com.mas.spring_generator.DTO.DatabaseType.POSTGRES;

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
    @Override
    public byte[] generate(ProjectRequest request) {
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

            if(request.isSecurityFeature()) {
                securityFeatureGenerator.addSecurityFeature(zip, request);
            }

            if(request.isJwtFeature()) {
                jwtFeatureGenerator.addJwtFeature(zip, request);
            }

            zip.close();
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate project", e);
        }
    }




}
