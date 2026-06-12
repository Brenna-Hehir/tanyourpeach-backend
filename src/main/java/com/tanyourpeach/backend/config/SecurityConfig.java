package com.tanyourpeach.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanyourpeach.backend.service.CustomUserDetailsService;
import com.tanyourpeach.backend.service.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

import static com.tanyourpeach.backend.security.JsonAuthHandlers.authenticationEntryPoint;
import static com.tanyourpeach.backend.security.JsonAuthHandlers.accessDeniedHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ObjectMapper om) throws Exception {
        http
            .csrf(csrf -> csrf.disable())

            // CORS: inline config (Java 8 friendly). This makes preflight OPTIONS return 200.
            .cors(cors -> cors.configurationSource(request -> {
                org.springframework.web.cors.CorsConfiguration c = new org.springframework.web.cors.CorsConfiguration();
                // For tests: allow all origins & methods; no credentials when using "*"
                c.setAllowedOrigins(java.util.Collections.singletonList("*"));
                c.setAllowedMethods(java.util.Arrays.asList("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
                c.setAllowedHeaders(java.util.Arrays.asList(
                    "Authorization","Content-Type","Accept","Origin",
                    "X-Correlation-Id","X-Requested-With",
                    "Access-Control-Request-Method","Access-Control-Request-Headers"
                ));
                c.setExposedHeaders(java.util.Collections.singletonList("X-Correlation-Id"));
                c.setAllowCredentials(false);
                return c;
            }))

            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(b -> b.disable())
            .formLogin(f -> f.disable())
            .logout(l -> l.disable())
            .requestCache(rc -> rc.disable())

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/actuator/health", "/error", "/assets/**", "/static/**").permitAll()
                .requestMatchers("/", "/home", "/about", "/contact").permitAll()

                .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/services/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/availabilities/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/appointments").permitAll()

                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers("/api/inventory/**").hasRole("ADMIN")
                .requestMatchers("/api/financial-log/**").hasRole("ADMIN")
                .requestMatchers("/api/service-usage/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/services/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/services/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/services/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/availabilities/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/availabilities/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/availabilities/**").hasRole("ADMIN")

                .requestMatchers("/api/appointments/**").authenticated()
                .requestMatchers("/api/user/**").authenticated()

                .anyRequest().authenticated()
            )

            .exceptionHandling(e -> e
                .authenticationEntryPoint(authenticationEntryPoint(om)) // 401 JSON
                .accessDeniedHandler(accessDeniedHandler(om))           // 403 JSON
            )

            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}