package br.lab.testesubmissao.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            UserDetailsService userDetailsService,
            RestAuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth

                       
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        
                        .requestMatchers("/auth/register", "/auth/login").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()

                     
                        .requestMatchers(HttpMethod.GET, "/problems/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/problems/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/problems/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/problems/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/problems/**").hasRole("ADMIN")

                        
                        .requestMatchers(HttpMethod.GET, "/tags/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/tags/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/tags/**").hasRole("ADMIN")

                        
                        .requestMatchers(HttpMethod.GET, "/submissions/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/submissions").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/submissions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/submissions/**").hasRole("ADMIN")

                        
                        .requestMatchers("/submissions/*/verdicts/**").authenticated()

                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                       
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
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
