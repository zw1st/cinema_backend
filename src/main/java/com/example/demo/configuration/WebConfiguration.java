package com.example.demo.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.validation.constraints.NotNull;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
        @Override
        public void addCorsMappings(@NotNull CorsRegistry registry) {
                registry
                                // Ваш API
                                .addMapping(Constants.API_URL + "/**")
                                .allowedMethods("GET", "POST", "PUT", "DELETE")
                                .allowedOrigins(Constants.DEV_ORIGIN);

                registry.addMapping("/swagger-ui/**")
                                .allowedMethods("GET")
                                .allowedOrigins("*");

                // 🔹 OpenAPI docs (отдельный вызов)
                registry.addMapping("/v3/api-docs/**")
                                .allowedMethods("GET")
                                .allowedOrigins("*");
        }
}
