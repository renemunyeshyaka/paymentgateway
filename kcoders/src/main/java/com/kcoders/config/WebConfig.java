// WebConfig.java
package com.kcoders.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${kcoders.cors.allowed-origins:http://localhost:8080,http://127.0.0.1:8080}")
    //@Value("${kcoders.cors.allowed-origins:http://dpg-d3r70femcj7s73bmmfe0-a.oregon-postgres.render.com:8080,http://127.0.0.1:8080}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}