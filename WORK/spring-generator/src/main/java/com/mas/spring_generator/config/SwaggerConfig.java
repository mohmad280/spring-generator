package com.mas.spring_generator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Spring Generator API")
                                .version("1.0")
                                .description("API for generating Spring Boot projects")
                                .contact(
                                        new Contact()
                                                .name("Mohmad Ghanem")
                                                .email("mohmadghanem18@gmail.com")
                                )
                );
    }


}
