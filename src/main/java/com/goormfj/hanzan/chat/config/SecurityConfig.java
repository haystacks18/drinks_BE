package com.goormfj.hanzan.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors  // CORS 설정 활성화
                        .configurationSource(request -> {
                            CorsConfiguration configuration = new CorsConfiguration();
                            configuration.setAllowedOriginPatterns(List.of("*")); // 모든 출처 허용
                            configuration.setAllowCredentials(true);  // 크레덴셜 허용
                            configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                            configuration.setAllowedHeaders(List.of("*"));
                            return configuration;
                        }))
                .csrf(csrf -> csrf.disable())  // CSRF 보호 비활성화
                .authorizeRequests(authz -> authz
                        .anyRequest().permitAll())  // 모든 요청 허용
                .httpBasic(withDefaults());  // HTTP Basic 인증 활성화

        return http.build();
    }
}
