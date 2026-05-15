package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security.config;

import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security.CurrentSessionClearFilter;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security.SessionAwareJwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevSecurityConfig {

    private final SessionAwareJwtAuthenticationConverter sessionConverter;
    private final CurrentSessionClearFilter currentSessionClearFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll() // Endpoints de autenticación no requieren JWT
                .requestMatchers("/api/**").authenticated() // Endpoints
                .anyRequest().permitAll()
            )
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(sessionConverter))
            )
            .addFilterAfter(currentSessionClearFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
