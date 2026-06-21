package com.mas.spring_generator.service.generator;

import com.mas.spring_generator.DTO.ProjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
public class RoleFeatureGenerator {

    private final ZipHelper zipHelper;

    public void addRoleFeature(ZipOutputStream zip, ProjectRequest request) throws IOException {
        String packagePath = request.getPackageName().replace(".", "/");
        String basePath = request.getProjectName() + "/src/main/java/" + packagePath;

        zipHelper.addFile(zip, basePath + "/enums/Role.java", generateRoleEnum(request));
    }

    private String generateRoleEnum(ProjectRequest request) {
        String roles = request.getRoles()
                .stream()
                .map(role -> "    " + role.toUpperCase())
                .reduce((a, b) -> a + ",\n" + b)
                .orElse("    USER");

        return """
                package %s.enums;

                public enum Role {
        %s
                }
                """.formatted(request.getPackageName(), roles);
    }

}
