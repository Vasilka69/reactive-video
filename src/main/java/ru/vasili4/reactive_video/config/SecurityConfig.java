package ru.vasili4.reactive_video.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import ru.vasili4.reactive_video.security.DefaultPermissionEvaluator;
import ru.vasili4.reactive_video.security.JwtAuthenticationManager;
import ru.vasili4.reactive_video.security.filters.AuthenticationFilter;
import ru.vasili4.reactive_video.security.filters.JWTLoginFilter;

@RequiredArgsConstructor
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final ReactiveUserDetailsService userDetailsService;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http,
                                                      ServerCodecConfigurer serverCodecConfigurer,
                                                      ReactiveAuthenticationManager authenticationManager,
                                                      JwtAuthenticationManager jwtAuthenticationManager) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .httpBasic((httpBasicSpec) -> {})
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers(getSwaggerPatterns()).permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/reactive/user/register").permitAll()
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

    private String[] getSwaggerPatterns() {
        return new String[] {
                "/webjars/**",
                "/v3/api-docs/**",
                "/swagger-ui.html",
                "/swagger-ui/**"
        };
    }
}
