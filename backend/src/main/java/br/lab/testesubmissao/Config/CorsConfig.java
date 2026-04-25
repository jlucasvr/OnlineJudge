package br.lab.testesubmissao.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:}")
    private String allowedOriginsProperty;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] allowedOrigins = Arrays.stream(allowedOriginsProperty.split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .toArray(String[]::new);

                if (allowedOrigins.length == 0) {
                    return;
                }

                register(registry, "/auth/**", allowedOrigins);
                register(registry, "/problems/**", allowedOrigins);
                register(registry, "/tags/**", allowedOrigins);
                register(registry, "/submissions/**", allowedOrigins);
                register(registry, "/users/**", allowedOrigins);
                register(registry, "/verdicts/**", allowedOrigins);
                register(registry, "/swagger-ui/**", allowedOrigins);
                register(registry, "/v3/api-docs/**", allowedOrigins);
            }
        };
    }

    private void register(CorsRegistry registry, String pathPattern, String[] allowedOrigins) {
        registry.addMapping(pathPattern)
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
