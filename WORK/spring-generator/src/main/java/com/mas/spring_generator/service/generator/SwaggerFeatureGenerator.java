package com.mas.spring_generator.service.generator;

import com.mas.spring_generator.DTO.ProjectRequest;
import com.mas.spring_generator.DTO.ProjectRequestWithERD;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
public class SwaggerFeatureGenerator {

    private final ZipHelper zipHelper;


    public void addSwaggerFeature(ZipOutputStream zip, ProjectRequest request) throws IOException {
        String packagePath = request.getPackageName().replace(".", "/");
        String basePath = request.getProjectName() + "/src/main/java/" + packagePath;

        zipHelper.addFile(
                zip,
                basePath + "/config/SwaggerConfig.java",
                generateSwaggerConfig(request)
        );
    }


    private String generateSwaggerConfig(ProjectRequest request) {
        return """
                package %s.config;
                
                import io.swagger.v3.oas.models.OpenAPI;
                import io.swagger.v3.oas.models.info.Info;
                import org.springframework.context.annotation.Bean;
                import org.springframework.context.annotation.Configuration;
                
                @Configuration
                public class SwaggerConfig {
                
                    @Bean
                    public OpenAPI customOpenAPI() {
                        return new OpenAPI()
                                .info(new Info()
                                        .title("%s API")
                                        .version("1.0.0")
                                        .description("Generated Spring Boot API documentation"));
                    }
                }
                """.formatted(
                request.getPackageName(),
                request.getProjectName()
        );
    }

    // مع ال ERD
    public void addSwaggerFeatureWithERD(ZipOutputStream zip, ProjectRequestWithERD request) throws IOException {
        String packagePath = request.getPackageName().replace(".", "/");
        String basePath = request.getProjectName() + "/src/main/java/" + packagePath;

        zipHelper.addFile(
                zip,
                basePath + "/config/SwaggerConfig.java",
                generateSwaggerConfigWithERD(request)
        );
    }


    private String generateSwaggerConfigWithERD(ProjectRequestWithERD request) {
        return """
                package %s.config;
                
                import io.swagger.v3.oas.models.OpenAPI;
                import io.swagger.v3.oas.models.info.Info;
                import org.springframework.context.annotation.Bean;
                import org.springframework.context.annotation.Configuration;
                
                @Configuration
                public class SwaggerConfig {
                
                    @Bean
                    public OpenAPI customOpenAPI() {
                        return new OpenAPI()
                                .info(new Info()
                                        .title("%s API")
                                        .version("1.0.0")
                                        .description("Generated Spring Boot API documentation"));
                    }
                }
                """.formatted(
                request.getPackageName(),
                request.getProjectName()
        );
    }

}
