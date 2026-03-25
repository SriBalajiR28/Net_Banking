package com.cmrit.demo.security;

import com.cmrit.demo.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomUserDetailsService userDetailsService;

    public WebSecurityConfig(JwtFilter jwtFilter,
                             CustomUserDetailsService userDetailsService) {
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ Allow React frontend
        configuration.setAllowedOriginPatterns(
                List.of("http://localhost:3000"));

        // ✅ Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE",
                "OPTIONS", "PATCH", "HEAD"));

        // ✅ Allow all headers
        configuration.setAllowedHeaders(List.of("*"));

        // ✅ Expose Authorization header to frontend
        configuration.setExposedHeaders(
                List.of("Authorization"));

        // ✅ Allow credentials
        configuration.setAllowCredentials(true);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        http
            // ✅ Wire CORS configuration source
            .cors(cors -> cors
                    .configurationSource(
                            corsConfigurationSource()))

            // ✅ Disable CSRF — not needed for stateless JWT
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // ✅ Allow all OPTIONS preflight requests
                .requestMatchers(
                        HttpMethod.OPTIONS, "/**").permitAll()

                // ✅ Public endpoints
                .requestMatchers(
                        "/auth/register",
                        "/auth/register-admin",
                        "/auth/login",
                        "/auth/validate-otp").permitAll()

                // ✅ Role based
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**")
                        .hasAnyRole("USER", "ADMIN")
                .requestMatchers("/session/**").authenticated()
                .requestMatchers("/alerts/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            .sessionManagement(session -> session
                    .sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS))

            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}