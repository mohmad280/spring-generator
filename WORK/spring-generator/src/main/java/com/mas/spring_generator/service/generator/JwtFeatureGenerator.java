package com.mas.spring_generator.service.generator;

import com.mas.spring_generator.DTO.ProjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
public class JwtFeatureGenerator {

    private final ZipHelper zipHelper;

    public void addJwtFeature(ZipOutputStream zip, ProjectRequest request) throws IOException {
        String packagePath = request.getPackageName().replace(".", "/");
        String basePath = request.getProjectName() + "/src/main/java/" + packagePath;

        zipHelper.addFile(zip, basePath + "/service/JwtService.java", generateJwtService(request));
        zipHelper.addFile(zip, basePath + "/service/CustomUserDetailsService.java", generateCustomUserDetailsService(request));
        zipHelper.addFile(zip, basePath + "/config/JwtAuthenticationFilter.java", generateJwtAuthenticationFilter(request));

        zipHelper.addFile(zip, basePath + "/dto/LoginRequest.java", generateLoginRequest(request));
        zipHelper.addFile(zip, basePath + "/dto/RegisterRequest.java", generateRegisterRequest(request));
        zipHelper.addFile(zip, basePath + "/dto/AuthResponse.java", generateAuthResponse(request));
        zipHelper.addFile(zip, basePath + "/controller/AuthController.java", generateAuthController(request));
    }

    public String generateJwtService(ProjectRequest request) {
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

    public String generateCustomUserDetailsService(ProjectRequest request) {
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

    public String generateJwtAuthenticationFilter(ProjectRequest request) {
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


    public String generateLoginRequest(ProjectRequest request) {
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


    public String generateRegisterRequest(ProjectRequest request) {
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


    public String generateAuthResponse(ProjectRequest request) {
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

    public String generateAuthController(ProjectRequest request) {
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

}
