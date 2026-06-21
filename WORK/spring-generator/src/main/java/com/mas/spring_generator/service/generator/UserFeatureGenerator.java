package com.mas.spring_generator.service.generator;

import com.mas.spring_generator.DTO.ProjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
public class UserFeatureGenerator {

    private final ZipHelper zipHelper;

    public void addUserFeature(ZipOutputStream zip, ProjectRequest request) throws IOException {
        String packagePath = request.getPackageName().replace(".", "/");
        String basePath = request.getProjectName() + "/src/main/java/" + packagePath;

        zipHelper.addFile(zip, basePath + "/entity/User.java", generateUserEntity(request));
        zipHelper.addFile(zip, basePath + "/repository/UserRepository.java", generateUserRepository(request));
        zipHelper.addFile(zip, basePath + "/dto/UserDto.java", generateUserDto(request));
        zipHelper.addFile(zip, basePath + "/service/UserService.java", generateUserService(request));
        zipHelper.addFile(zip, basePath + "/service/impl/UserServiceImpl.java", generateUserServiceImpl(request));
        zipHelper.addFile(zip, basePath + "/controller/UserController.java", generateUserController(request));
    }

    public String generateUserEntity(ProjectRequest request) {
        String roleField = "";

        if (request.isRoleFeature()) {
            roleField = """
                
                @Enumerated(EnumType.STRING)
                private Role role;
                """;
        }

        String roleImport = request.isRoleFeature()
                ? "import " + request.getPackageName() + ".enums.Role;\n"
                : "";

        return """
            package %s.entity;

            import jakarta.persistence.*;
            import lombok.*;
            %s
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
                %s
            }
            """.formatted(request.getPackageName(), roleImport, roleField);
    }

    public String generateUserRepository(ProjectRequest request) {
        return """
                package %s.repository;

                import %s.entity.User;
                import java.util.Optional;
                import org.springframework.data.jpa.repository.JpaRepository;

                public interface UserRepository extends JpaRepository<User, Long> {
                    Optional<User> findByEmail(String email);
                }
                """.formatted(request.getPackageName(), request.getPackageName());
    }

    public String generateUserDto(ProjectRequest request) {
        String roleImport = request.isRoleFeature()
                ? "import " + request.getPackageName() + ".enums.Role;\n"
                : "";

        String roleField = request.isRoleFeature()
                ? "    private Role role;\n"
                : "";

        return """
            package %s.dto;

            import lombok.*;
            %s
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            public class UserDto {
                private Long id;
                private String name;
                private String email;
            %s
            }
            """.formatted(request.getPackageName(), roleImport, roleField);
    }

    public String generateUserService(ProjectRequest request) {
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


    public String generateUserServiceImpl(ProjectRequest request) {

        String createRoleSetter = request.isRoleFeature()
                ? "                        .role(userDto.getRole())\n"
                : "";

        String updateRoleSetter = request.isRoleFeature()
                ? "        user.setRole(userDto.getRole());\n"
                : "";

        String dtoRoleSetter = request.isRoleFeature()
                ? "                            .role(user.getRole())\n"
                : "";

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
           %s
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
           %s
                    User updatedUser = userRepository.save(user);
                    return mapToDto(updatedUser);
                }

                @Override
                public void deleteUser(Long id) {
                    userRepository.deleteById(id);
                }

                private UserDto mapToDto(User user) {
                    return UserDto.builder()
                            .id(user.getId())
                            .name(user.getName())
                            .email(user.getEmail())
            %s
                            .build();
                }
            }
            """.formatted(
                request.getPackageName(),
                request.getPackageName(),
                request.getPackageName(),
                request.getPackageName(),
                request.getPackageName(),
                createRoleSetter,
                updateRoleSetter,
                dtoRoleSetter
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


}
