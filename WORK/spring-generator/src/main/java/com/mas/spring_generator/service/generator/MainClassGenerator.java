package com.mas.spring_generator.service.generator;

import com.mas.spring_generator.DTO.ProjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
public class MainClassGenerator {

    private final ZipHelper zipHelper;

    public void addMainApplicationClass(ZipOutputStream zip, ProjectRequest request) throws IOException {
        String packagePath = request.getPackageName().replace(".", "/");

        String content = """
            package %s;

            import org.springframework.boot.SpringApplication;
            import org.springframework.boot.autoconfigure.SpringBootApplication;

            @SpringBootApplication
            public class BaseProjectApplication {

                public static void main(String[] args) {
                    SpringApplication.run(BaseProjectApplication.class, args);
                }
            }
            """.formatted(request.getPackageName());

        zipHelper.addFile(
                zip,
                request.getProjectName() + "/src/main/java/" + packagePath + "/BaseProjectApplication.java",
                content
        );
    }
}
