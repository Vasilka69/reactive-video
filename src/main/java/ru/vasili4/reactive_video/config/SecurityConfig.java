package ru.vasili4.reactive_video.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import ru.vasili4.reactive_video.security.DefaultPermissionEvaluator;
import ru.vasili4.reactive_video.security.JwtAuthenticationManager;
import ru.vasili4.reactive_video.security.filters.AuthenticationFilter;
import ru.vasili4.reactive_video.security.filters.JWTLoginFilter;

import java.util.List;

@RequiredArgsConstructor
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
  // todo нужен рефакторинг
    private final ReactiveUserDetailsService userDetailsService;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http,
                                                      ServerCodecConfigurer serverCodecConfigurer,
                                                      ReactiveAuthenticationManager authenticationManager,
                                                      JwtAuthenticationManager jwtAuthenticationManager) {
        http
//                .cors(ServerHttpSecurity.CorsSpec::disable)
                .cors(Customizer.withDefaults())
                .cors(cors -> cors.configurationSource(corsWebFilterSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .httpBasic(Customizer.withDefaults())
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers(getSwaggerPatterns()).permitAll()
                        .pathMatchers("/api/v1/reactive/user/register").permitAll()
                        .pathMatchers("/api/v1/reactive/user/login").permitAll()
                        .anyExchange().authenticated())
                .addFilterAt(new JWTLoginFilter(HttpMethod.POST, "/api/v1/reactive/user/login", authenticationManager, serverCodecConfigurer), SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterAt(new AuthenticationFilter(jwtAuthenticationManager), SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        UserDetailsRepositoryReactiveAuthenticationManager manager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        manager.setPasswordEncoder(passwordEncoder);
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PermissionEvaluator defaultPermissionEvaluator(DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler) {
        DefaultPermissionEvaluator defaultPermissionEvaluator = new DefaultPermissionEvaluator();
        methodSecurityExpressionHandler.setPermissionEvaluator(defaultPermissionEvaluator);

        return defaultPermissionEvaluator;
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }

    @Bean
    public CorsConfigurationSource corsWebFilterSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    private String[] getSwaggerPatterns() {
        return new String[] {
                "/webjars/**",
                "/v3/api-docs/**",
                "/swagger-ui.html",
                "/swagger-ui/**"
        };
    }
}
