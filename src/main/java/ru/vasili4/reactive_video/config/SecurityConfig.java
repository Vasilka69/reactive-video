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
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import ru.vasili4.reactive_video.web.security.filters.AuthenticationFilter;
import ru.vasili4.reactive_video.web.security.DefaultPermissionEvaluator;
import ru.vasili4.reactive_video.web.security.DefaultSecurityContextRepository;
import ru.vasili4.reactive_video.web.security.filters.JWTLoginFilter;

@RequiredArgsConstructor
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final ReactiveUserDetailsService userDetailsService;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http,
                                                      ServerCodecConfigurer serverCodecConfigurer,
                                                      ReactiveAuthenticationManager authenticationManager) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers(HttpMethod.POST, "/api/v1/reactive-user/register").permitAll()
                        .anyExchange().authenticated())
                .securityContextRepository(new DefaultSecurityContextRepository())
                .addFilterAt(new JWTLoginFilter(HttpMethod.POST, "/login", authenticationManager, serverCodecConfigurer), SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterAt(new AuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }

    @Bean
    public UserDetailsRepositoryReactiveAuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
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

}
