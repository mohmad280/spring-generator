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

            addMainApplicationClass(zip, request);

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

            if(request.isSecurityFeature()) {
                addSecurityFeature(zip, request);
            }

            if(request.isJwtFeature()) {
                addJwtFeature(zip, request);
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

                        if (relativePath.contains("BaseProjectApplication.java")
                                || relativePath.contains("BaseProjectApplicationTests.java")) {
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

    private void addMainApplicationClass(ZipOutputStream zip, ProjectRequest request) throws IOException {
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

        addFile(
                zip,
                request.getProjectName() + "/src/main/java/" + packagePath + "/BaseProjectApplication.java",
                content
        );
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
                import java.util.Optional;
                import org.springframework.data.jpa.repository.JpaRepository;

                public interface UserRepository extends JpaRepository<User, Long> {
                    Optional<User> findByEmail(String email);
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


    private String generateJwtService(ProjectRequest request) {
        return """
            package %s.service;

            import io.jsonwebtoken.Claims;
            import io.jsonwebtoken.Jwts;
            import io.jsonwebtoken.security.Keys;
            import org.springframework.stereotype.Service;

            import javax.crypto.SecretKey;
            import java.nio.charset.StandardCharsets;
            import java.util.Date;
            import java.util.function.Function;

            @Service
            public class JwtService {

                private static final String SECRET_KEY = "my-super-secret-key-my-super-secret-key-123456";

                private SecretKey getSigningKey() {
                    return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
                }

                public String generateToken(String email) {
                    return Jwts.builder()
                            .subject(email)
                            .issuedAt(new Date(System.currentTimeMillis()))
                            .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                            .signWith(getSigningKey())
                            .compact();
                }

                public String extractEmail(String token) {
                    return extractClaim(token, Claims::getSubject);
                }

                public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
                    final Claims claims = Jwts.parser()
                            .verifyWith(getSigningKey())
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

                    return claimsResolver.apply(claims);
                }

                public boolean isTokenValid(String token, String email) {
                    final String tokenEmail = extractEmail(token);
                    return tokenEmail.equals(email) && !isTokenExpired(token);
                }

                private boolean isTokenExpired(String token) {
                    return extractClaim(token, Claims::getExpiration).before(new Date());
                }
            }
            """.formatted(request.getPackageName());
    }


    private void addJwtFeature(ZipOutputStream zip, ProjectRequest request) throws IOException {
        String packagePath = request.getPackageName().replace(".", "/");
        String basePath = request.getProjectName() + "/src/main/java/" + packagePath;

        addFile(zip, basePath + "/service/JwtService.java", generateJwtService(request));
        addFile(zip, basePath + "/service/CustomUserDetailsService.java", generateCustomUserDetailsService(request));
        addFile(zip, basePath + "/config/JwtAuthenticationFilter.java", generateJwtAuthenticationFilter(request));

        addFile(zip, basePath + "/dto/LoginRequest.java", generateLoginRequest(request));
        addFile(zip, basePath + "/dto/RegisterRequest.java", generateRegisterRequest(request));
        addFile(zip, basePath + "/dto/AuthResponse.java", generateAuthResponse(request));
        addFile(zip, basePath + "/controller/AuthController.java", generateAuthController(request));
    }


    private String generateCustomUserDetailsService(ProjectRequest request) {
        return """
            package %s.service;

            import %s.entity.User;
            import %s.repository.UserRepository;
            import lombok.RequiredArgsConstructor;
            import org.springframework.security.core.userdetails.UserDetails;
            import org.springframework.security.core.userdetails.UserDetailsService;
            import org.springframework.security.core.userdetails.UsernameNotFoundException;
            import org.springframework.stereotype.Service;

            import java.util.Collections;

            @Service
            @RequiredArgsConstructor
            public class CustomUserDetailsService implements UserDetailsService {

                private final UserRepository userRepository;

                @Override
                public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                    return new org.springframework.security.core.userdetails.User(
                            user.getEmail(),
                            user.getPassword(),
                            Collections.emptyList()
                    );
                }
            }
            """.formatted(
                request.getPackageName(),
                request.getPackageName(),
                request.getPackageName()
        );
    }

    private String generateJwtAuthenticationFilter(ProjectRequest request) {
        return """
            package %s.config;

            import %s.service.JwtService;
            import jakarta.servlet.FilterChain;
            import jakarta.servlet.ServletException;
            import jakarta.servlet.http.HttpServletRequest;
            import jakarta.servlet.http.HttpServletResponse;
            import lombok.RequiredArgsConstructor;
            import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
            import org.springframework.security.core.context.SecurityContextHolder;
            import org.springframework.security.core.userdetails.UserDetails;
            import org.springframework.security.core.userdetails.UserDetailsService;
            import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
            import org.springframework.stereotype.Component;
            import org.springframework.web.filter.OncePerRequestFilter;

            import java.io.IOException;

            @Component
            @RequiredArgsConstructor
            public class JwtAuthenticationFilter extends OncePerRequestFilter {

                private final JwtService jwtService;
                private final UserDetailsService userDetailsService;

                @Override
                protected void doFilterInternal(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain filterChain
                ) throws ServletException, IOException {

                    final String authHeader = request.getHeader("Authorization");

                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        filterChain.doFilter(request, response);
                        return;
                    }

                    final String jwt = authHeader.substring(7);
                    final String email = jwtService.extractEmail(jwt);

                    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                        if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities()
                                    );

                            authToken.setDetails(
                                    new WebAuthenticationDetailsSource().buildDetails(request)
                            );

                            SecurityContextHolder.getContext().setAuthentication(authToken);
                        }
                    }

                    filterChain.doFilter(request, response);
                }
            }
            """.formatted(
                request.getPackageName(),
                request.getPackageName()
        );
    }


    private String generateLoginRequest(ProjectRequest request) {
        return """
            package %s.dto;

            import lombok.Data;

            @Data
            public class LoginRequest {
                private String email;
                private String password;
            }
            """.formatted(request.getPackageName());
    }

    private String generateRegisterRequest(ProjectRequest request) {
        return """
            package %s.dto;

            import lombok.Data;

            @Data
            public class RegisterRequest {
                private String name;
                private String email;
                private String password;
            }
            """.formatted(request.getPackageName());
    }

    private String generateAuthResponse(ProjectRequest request) {
        return """
            package %s.dto;

            import lombok.AllArgsConstructor;
            import lombok.Data;

            @Data
            @AllArgsConstructor
            public class AuthResponse {
                private String token;
            }
            """.formatted(request.getPackageName());
    }


    private String generateAuthController(ProjectRequest request) {
        return """
            package %s.controller;

            import %s.dto.AuthResponse;
            import %s.dto.LoginRequest;
            import %s.dto.RegisterRequest;
            import %s.entity.User;
            import %s.repository.UserRepository;
            import %s.service.JwtService;
            import lombok.RequiredArgsConstructor;
            import org.springframework.security.crypto.password.PasswordEncoder;
            import org.springframework.web.bind.annotation.*;

            @RestController
            @RequestMapping("/api/auth")
            @RequiredArgsConstructor
            public class AuthController {

                private final UserRepository userRepository;
                private final PasswordEncoder passwordEncoder;
                private final JwtService jwtService;

                @PostMapping("/register")
                public AuthResponse register(@RequestBody RegisterRequest request) {
                    User user = User.builder()
                            .name(request.getName())
                            .email(request.getEmail())
                            .password(passwordEncoder.encode(request.getPassword()))
                            .build();

                    userRepository.save(user);

                    String token = jwtService.generateToken(user.getEmail());
                    return new AuthResponse(token);
                }

                @PostMapping("/login")
                public AuthResponse login(@RequestBody LoginRequest request) {
                    User user = userRepository.findByEmail(request.getEmail())
                            .orElseThrow(() -> new RuntimeException("Invalid email or password"));

                    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        throw new RuntimeException("Invalid email or password");
                    }

                    String token = jwtService.generateToken(user.getEmail());
                    return new AuthResponse(token);
                }
            }
            """.formatted(
                request.getPackageName(),
                request.getPackageName(),
                request.getPackageName(),
                request.getPackageName(),
                request.getPackageName(),
                request.getPackageName(),
                request.getPackageName()
        );
    }



        private void addFile(ZipOutputStream zip, String path, String content) throws IOException {
        ZipEntry entry = new ZipEntry(path);
        zip.putNextEntry(entry);
        zip.write(content.getBytes());
        zip.closeEntry();
    }


    private void addSecurityFeature(ZipOutputStream zip, ProjectRequest request) throws IOException {
        String packagePath = request.getPackageName().replace(".", "/");
        String basePath = request.getProjectName() + "/src/main/java/" + packagePath;

        addFile(zip, basePath + "/config/SecurityConfig.java", generateSecurityConfig(request));
    }



    private String generateSecurityConfig(ProjectRequest request) {
        return """
            package %s.config;

            import lombok.RequiredArgsConstructor;
            import org.springframework.context.annotation.Bean;
            import org.springframework.context.annotation.Configuration;
            import org.springframework.security.authentication.AuthenticationManager;
            import org.springframework.security.authentication.AuthenticationProvider;
            import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
            import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
            import org.springframework.security.config.annotation.web.builders.HttpSecurity;
            import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
            import org.springframework.security.config.http.SessionCreationPolicy;
            import org.springframework.security.core.userdetails.UserDetailsService;
            import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
            import org.springframework.security.crypto.password.PasswordEncoder;
            import org.springframework.security.web.SecurityFilterChain;
            import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

            @Configuration
            @EnableWebSecurity
            @RequiredArgsConstructor
            public class SecurityConfig {

                private final JwtAuthenticationFilter jwtAuthenticationFilter;
                private final UserDetailsService userDetailsService;

                @Bean
                public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                    http
                            .csrf(csrf -> csrf.disable())
                            .authorizeHttpRequests(auth -> auth
                                    .requestMatchers("/api/auth/**").permitAll()
                                    .anyRequest().authenticated()
                            )
                            .sessionManagement(session -> session
                                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                            )
                            .authenticationProvider(authenticationProvider())
                            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                    return http.build();
                }

                @Bean
                public AuthenticationProvider authenticationProvider() {
                    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
                    provider.setPasswordEncoder(passwordEncoder());
                    return provider;
                }

                @Bean
                public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                    return config.getAuthenticationManager();
                }

                @Bean
                public PasswordEncoder passwordEncoder() {
                    return new BCryptPasswordEncoder();
                }
            }
            """.formatted(request.getPackageName());
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
