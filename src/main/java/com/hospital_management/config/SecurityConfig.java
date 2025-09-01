package com.hospital_management.config;


import com.hospital_management.services.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final AuthTokenFilter authTokenFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth
                                // Public endpoints - no authentication required
                                // NOTE: Context path '/api' is stripped, so patterns start without '/api'
                                .requestMatchers("/auth/**").permitAll()
                                .requestMatchers("/public/**").permitAll()
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                                .requestMatchers("/actuator/health").permitAll()
                                .requestMatchers("/error").permitAll()

                                // Public user registration endpoints - MUST come before /users/** restrictions
                                .requestMatchers("/users/register/patient").permitAll()
                                .requestMatchers("/users/register/super-admin").permitAll()
                                .requestMatchers("/users/username/*/available").permitAll()
                                .requestMatchers("/users/email/*/available").permitAll()

                                // Patient endpoints
                                .requestMatchers("/patients/register").permitAll()
                                .requestMatchers("/patients/**").permitAll()

                                // Admin endpoints
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/users/*/admin/**").hasRole("ADMIN")

                                // Doctor endpoints
                                .requestMatchers("/doctors/register").hasAnyRole("ADMIN")
                                .requestMatchers("/doctors/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")



                                // Nurse endpoints
                                .requestMatchers("/nurses/register").hasRole("ADMIN")
                                .requestMatchers("/nurses/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")

                                // Lab Technician endpoints
                                .requestMatchers("/lab-technicians/register").hasRole("ADMIN")
                                .requestMatchers("/lab-technicians/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE", "LAB_TECHNICIAN")

                                // Receptionist endpoints
                                .requestMatchers("/receptionists/register").hasRole("ADMIN")
                                .requestMatchers("/receptionists/**").hasAnyRole("ADMIN", "RECEPTIONIST")

                                // Role and Permission management
                                .requestMatchers("/roles/**").hasRole("ADMIN")
                                .requestMatchers("/permissions/**").hasRole("ADMIN")

                                // Audit logs
                                .requestMatchers("/audit/**").hasRole("ADMIN")

                                // User profile management (authenticated users can access their own profile)
                                .requestMatchers("/users/profile").authenticated()

                                // User management (protected endpoints - MUST come after public registration endpoints)
                                .requestMatchers("/users/register").hasAnyRole("ADMIN", "RECEPTIONIST")
                                .requestMatchers("/users/register/staff").hasAnyRole("ADMIN", "RECEPTIONIST")
                                .requestMatchers("/users/register/admin").hasRole("SUPER_ADMIN")
                                .requestMatchers("/users/**").hasAnyRole("ADMIN", "RECEPTIONIST")

                                .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
