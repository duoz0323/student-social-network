package com.stu.edu.vn.backend.security;

import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Cấu hình bảo mật stateless cho API, dùng JWT Access Token thay cho session server-side.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ProfileCompletionFilter profileCompletionFilter;
    private final SecurityErrorResponseWriter errorResponseWriter;
    private final AuthRateLimitFilter authRateLimitFilter;
    private final com.stu.edu.vn.backend.analytics.tracking.UserActivityTrackingFilter userActivityTrackingFilter;
    private final SecurityCorsProperties securityCorsProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(securityCorsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(securityCorsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(securityCorsProperties.getAllowedHeaders());
        configuration.setExposedHeaders(securityCorsProperties.getExposedHeaders());
        configuration.setAllowCredentials(securityCorsProperties.isAllowCredentials());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                errorResponseWriter.write(response, request, ErrorCode.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                errorResponseWriter.write(response, request, ErrorCode.FORBIDDEN))
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                HttpMethod.POST,
                                SecurityPaths.PUBLIC_POST_AUTH_ENDPOINTS.toArray(String[]::new)
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                SecurityPaths.PUBLIC_GET_AUTH_ENDPOINTS.toArray(String[]::new)
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Browser không gửi Bearer header ở WebSocket handshake; STOMP CONNECT xác thực riêng.
                        .requestMatchers("/ws").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(profileCompletionFilter, JwtAuthenticationFilter.class)
                .addFilterAfter(authRateLimitFilter, ProfileCompletionFilter.class)
                .addFilterAfter(userActivityTrackingFilter, AuthRateLimitFilter.class)
                .build();
    }

}
