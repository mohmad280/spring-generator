package com.mas.spring_generator.service.generator;

import com.mas.spring_generator.DTO.ProjectRequest;
import org.springframework.stereotype.Component;

@Component
public class DependencyGenerator {


    public String getDependencies(ProjectRequest request) {
        StringBuilder dependencies = new StringBuilder();

        dependencies.append("""
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-web</artifactId>
            </dependency>

            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <optional>true</optional>
            </dependency>
            """);

        switch (request.getDatabase()) {
            case MYSQL -> dependencies.append("""
                
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-data-jpa</artifactId>
                </dependency>

                <dependency>
                    <groupId>com.mysql</groupId>
                    <artifactId>mysql-connector-j</artifactId>
                    <scope>runtime</scope>
                </dependency>
                """);

            case POSTGRES -> dependencies.append("""
                
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-data-jpa</artifactId>
                </dependency>

                <dependency>
                    <groupId>org.postgresql</groupId>
                    <artifactId>postgresql</artifactId>
                    <scope>runtime</scope>
                </dependency>
                """);
        }

        if (request.isSecurityFeature()) {
            dependencies.append("""
            
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-security</artifactId>
            </dependency>
            """);
        }

        if (request.isJwtFeature()) {
            dependencies.append("""
            
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-api</artifactId>
                <version>0.12.6</version>
            </dependency>

            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-impl</artifactId>
                <version>0.12.6</version>
                <scope>runtime</scope>
            </dependency>

            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-jackson</artifactId>
                <version>0.12.6</version>
                <scope>runtime</scope>
            </dependency>
            """);
        }

        return dependencies.toString();

    }

}
