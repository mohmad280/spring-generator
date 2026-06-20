package com.mas.spring_generator.service.generator;

import com.mas.spring_generator.DTO.ProjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
public class BaseProjectGenerator {

    private final ZipHelper zipHelper;
    private final DependencyGenerator dependencyGenerator;

    public void copyBaseProject(ZipOutputStream zip, ProjectRequest request) throws IOException {
        Path basePath = Paths.get("src/main/resources/base-project");

        Files.walk(basePath)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        String relativePath = basePath.relativize(path).toString().replace("\\", "/");

                        // لا تنسخ application.properties من base-project
                        // لأننا رح نولده ديناميكياً حسب نوع قاعدة البيانات
                        if (relativePath.equals("src/main/resources/application.properties")) {
                            return;
                        }

                        if (relativePath.contains("BaseProjectApplication.java")
                                || relativePath.contains("BaseProjectApplicationTests.java")) {
                            return;
                        }

                        String targetPath = request.getProjectName() + "/" + relativePath;

                        String content = Files.readString(path);

                        content = content.replace("{{projectName}}", request.getProjectName());
                        content = content.replace("{{packageName}}", request.getPackageName());
                        content = content.replace("{{dependencies}}", dependencyGenerator.getDependencies(request));
                        content = content.replace("com.example.demo", request.getPackageName());

                        zipHelper.addFile(zip, targetPath, content);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
