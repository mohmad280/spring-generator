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

import static com.mas.spring_generator.DTO.DatabaseType.MYSQL;
import static com.mas.spring_generator.DTO.DatabaseType.POSTGRES;

@Service
@RequiredArgsConstructor
public class ProjectGeneratorServiceImpl implements ProjectGeneratorService {
    @Override
    public byte[] generate(ProjectRequest request) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(outputStream);

            // نسخ المشروع الأساسي
            copyBaseProject(zip, request);

            // توليد application.properties حسب قاعدة البيانات المختارة
            addFile(
                    zip,
                    request.getProjectName() + "/src/main/resources/application.properties",
                    generateApplicationProperties(request)
            );

            // إضافة User Feature إذا مختارة
            if (request.isUserFeature()) {
                addUserFeature(zip, request);
            }

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

                        // لا تنسخ application.properties من base-project
                        // لأننا رح نولده ديناميكياً حسب نوع قاعدة البيانات
                        if (relativePath.equals("src/main/resources/application.properties")) {
                            return;
                        }

                        String targetPath = request.getProjectName() + "/" + relativePath;

                        String content = Files.readString(path);

                        content = content.replace("{{projectName}}", request.getProjectName());
                        content = content.replace("{{packageName}}", request.getPackageName());
                        content = content.replace("{{dependencies}}", getDependencies(request));
                        content = content.replace("com.example.demo", request.getPackageName());

                        addFile(zip, targetPath, content);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }


    private void addUserFeature(ZipOutputStream zip, ProjectRequest request) throws IOException {
        String packagePath = request.getPackageName().replace(".", "/");
        String basePath = request.getProjectName() + "/src/main/java/" + packagePath;

        addFile(zip, basePath + "/entity/User.java", generateUserEntity(request));
        addFile(zip, basePath + "/repository/UserRepository.java", generateUserRepository(request));
        addFile(zip, basePath + "/dto/UserDto.java", generateUserDto(request));
        addFile(zip, basePath + "/service/UserService.java", generateUserService(request));
        addFile(zip, basePath + "/service/impl/UserServiceImpl.java", generateUserServiceImpl(request));
        addFile(zip, basePath + "/controller/UserController.java", generateUserController(request));
    }


    private String generateUserEntity(ProjectRequest request) {
        return """
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
    }

    private String generateUserRepository(ProjectRequest request) {
        return """
            package %s.repository;

            import %s.entity.User;
            import org.springframework.data.jpa.repository.JpaRepository;

            public interface UserRepository extends JpaRepository<User, Long> {
            }
            """.formatted(request.getPackageName(), request.getPackageName());
    }

    private String generateUserDto(ProjectRequest request) {
        return """
            package %s.dto;

            import lombok.*;

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            public class UserDto {
                private Long id;
                private String name;
                private String email;
            }
            """.formatted(request.getPackageName());
    }

    private String generateUserService(ProjectRequest request) {
        return """
            package %s.service;

            import %s.dto.UserDto;
            import java.util.List;

            public interface UserService {
                UserDto createUser(UserDto userDto);
                UserDto getUserById(Long id);
                List<UserDto> getAllUsers();
                UserDto updateUser(Long id, UserDto userDto);
                void deleteUser(Long id);
            }
            """.formatted(request.getPackageName(), request.getPackageName());
    }



    private String generateUserServiceImpl(ProjectRequest request) {
        return """
            package %s.service.impl;

            import %s.dto.UserDto;
            import %s.entity.User;
            import %s.repository.UserRepository;
            import %s.service.UserService;
            import lombok.RequiredArgsConstructor;
            import org.springframework.stereotype.Service;

            import java.util.List;

            @Service
            @RequiredArgsConstructor
            public class UserServiceImpl implements UserService {

                private final UserRepository userRepository;

                @Override
                public UserDto createUser(UserDto userDto) {
                    User user = User.builder()
                            .name(userDto.getName())
                            .email(userDto.getEmail())
                            .build();

                    User savedUser = userRepository.save(user);
                    return mapToDto(savedUser);
                }

                @Override
                public UserDto getUserById(Long id) {
                    User user = userRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    return mapToDto(user);
                }

                @Override
                public List<UserDto> getAllUsers() {
                    return userRepository.findAll()
                            .stream()
                            .map(this::mapToDto)
                            .toList();
                }

                @Override
                public UserDto updateUser(Long id, UserDto userDto) {
                    User user = userRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    user.setName(userDto.getName());
                    user.setEmail(userDto.getEmail());

                    User updatedUser = userRepository.save(user);
                    return mapToDto(updatedUser);
                }

                @Override
                public void deleteUser(Long id) {
                    User user = userRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    userRepository.delete(user);
                }

                private UserDto mapToDto(User user) {
                    return UserDto.builder()
                            .id(user.getId())
                            .name(user.getName())
                            .email(user.getEmail())
                            .build();
                }
            }
            """.formatted(
                request.getPackageName(),
                request.getPackageName(),
                request.getPackageName(),
                request.getPackageName(),
                request.getPackageName()
        );
    }


    private String generateUserController(ProjectRequest request) {
        return """
                package %s.controller;

                import %s.dto.UserDto;
                import %s.service.UserService;
                import lombok.RequiredArgsConstructor;
                import org.springframework.web.bind.annotation.*;

                import java.util.List;

                @RestController
                @RequestMapping("/api/users")
                @RequiredArgsConstructor
                public class UserController {

                    private final UserService userService;

                    @PostMapping
                    public UserDto createUser(@RequestBody UserDto userDto) {
                        return userService.createUser(userDto);
                    }

                    @GetMapping("/{id}")
                    public UserDto getUserById(@PathVariable Long id) {
                        return userService.getUserById(id);
                    }

                    @GetMapping
                    public List<UserDto> getAllUsers() {
                        return userService.getAllUsers();
                    }

                    @PutMapping("/{id}")
                    public UserDto updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
                        return userService.updateUser(id, userDto);
                    }

                    @DeleteMapping("/{id}")
                    public void deleteUser(@PathVariable Long id) {
                        userService.deleteUser(id);
                    }
                }
                """.formatted(
                request.getPackageName(),
                request.getPackageName(),
                request.getPackageName()
        );
    }


    private String getDependencies(ProjectRequest request) {
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

        return dependencies.toString();

    }




        private void addFile(ZipOutputStream zip, String path, String content) throws IOException {
        ZipEntry entry = new ZipEntry(path);
        zip.putNextEntry(entry);
        zip.write(content.getBytes());
        zip.closeEntry();
    }






    private String generateApplicationProperties(ProjectRequest request) {
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
