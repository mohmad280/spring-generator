package com.mas.spring_generator.service.impl;

import com.mas.spring_generator.DTO.ProjectRequest;
import com.mas.spring_generator.service.ProjectGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import java.nio.file.*;

@Service
@RequiredArgsConstructor
public class ProjectGeneratorServiceImpl implements ProjectGeneratorService {
    @Override
    public byte[] generate(ProjectRequest request) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(outputStream);

            copyBaseProject(zip, request);

            addUserFeature(zip, request);

            zip.close();
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate project", e);
        }
    }

    private void copyBaseProject(ZipOutputStream zip, ProjectRequest request) throws IOException {
        Path basePath = Paths.get("src/main/resources/base-project");

        Files.walk(basePath)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        String relativePath = basePath.relativize(path).toString().replace("\\", "/");

                        String targetPath = request.getProjectName() + "/" + relativePath;

                        String content = Files.readString(path);

                        content = content.replace("com.example.demo", request.getPackageName());

                        addFile(zip, targetPath, content);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }


    private void addUserFeature(ZipOutputStream zip, ProjectRequest request) throws IOException {
        String packagePath = request.getPackageName().replace(".", "/");

        String userJava = """
            package %s.entity;
            
            import jakarta.persistence.*;
            import lombok.*;
            
            @Entity
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            public class User {
            
                @Id
                @GeneratedValue(strategy = GenerationType.IDENTITY)
                private Long id;
            
                private String name;
                private String email;
                private String password;
            }
            """.formatted(request.getPackageName());

        addFile(zip,
                request.getProjectName() + "/src/main/java/" + packagePath + "/entity/User.java",
                userJava);
    }

    private void addFile(ZipOutputStream zip, String path, String content) throws IOException {
        ZipEntry entry = new ZipEntry(path);
        zip.putNextEntry(entry);
        zip.write(content.getBytes());
        zip.closeEntry();
    }
}
