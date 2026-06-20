package com.mas.spring_generator.service.generator;

import com.mas.spring_generator.DTO.ProjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
public class SecurityFeatureGenerator {

    private final ZipHelper zipHelper;

    public void addSecurityFeature(ZipOutputStream zip, ProjectRequest request) throws IOException {
        String packagePath = request.getPackageName().replace(".", "/");
        String basePath = request.getProjectName() + "/src/main/java/" + packagePath;

        zipHelper.addFile(zip, basePath + "/config/SecurityConfig.java", generateSecurityConfig(request));
    }


    public String generateSecurityConfig(ProjectRequest request) {
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


}
