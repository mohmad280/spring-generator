package com.mas.spring_generator.service.generator;

import com.mas.spring_generator.DTO.ProjectRequest;
import com.mas.spring_generator.DTO.ProjectRequestWithERD;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class PropertiesGenerator {


    public String generateApplicationProperties(ProjectRequest request) {
        String common = """
            spring.application.name=%s

            """.formatted(request.getProjectName());

        return switch (request.getDatabase()) {
            case MYSQL -> common + """
                spring.datasource.url=jdbc:mysql://localhost:3306/%s?createDatabaseIfNotExist=true
                spring.datasource.username=root
                spring.datasource.password=

                spring.jpa.hibernate.ddl-auto=update
                spring.jpa.show-sql=true
                spring.jpa.properties.hibernate.format_sql=true
                spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
                """.formatted(request.getDatabaseName());

            case POSTGRES -> common + """
                spring.datasource.url=jdbc:postgresql://localhost:5432/%s
                spring.datasource.username=postgres
                spring.datasource.password=1234

                spring.jpa.hibernate.ddl-auto=update
                spring.jpa.show-sql=true
                spring.jpa.properties.hibernate.format_sql=true
                spring.datasource.driver-class-name=org.postgresql.Driver
                """.formatted(request.getDatabaseName());
        };
    }


    // مع ال ERD
    public String generateApplicationPropertiesWithERD(ProjectRequestWithERD request) {
        String common = """
            spring.application.name=%s

            """.formatted(request.getProjectName());

        return switch (request.getDatabase()) {
            case MYSQL -> common + """
                spring.datasource.url=jdbc:mysql://localhost:3306/%s?createDatabaseIfNotExist=true
                spring.datasource.username=root
                spring.datasource.password=

                spring.jpa.hibernate.ddl-auto=update
                spring.jpa.show-sql=true
                spring.jpa.properties.hibernate.format_sql=true
                spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
                """.formatted(request.getDatabaseName());

            case POSTGRES -> common + """
                spring.datasource.url=jdbc:postgresql://localhost:5432/%s
                spring.datasource.username=postgres
                spring.datasource.password=1234

                spring.jpa.hibernate.ddl-auto=update
                spring.jpa.show-sql=true
                spring.jpa.properties.hibernate.format_sql=true
                spring.datasource.driver-class-name=org.postgresql.Driver
                """.formatted(request.getDatabaseName());
        };
    }
}
