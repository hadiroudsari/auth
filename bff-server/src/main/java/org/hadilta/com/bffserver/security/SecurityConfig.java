package org.hadilta.com.bffserver.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(Customizer.withDefaults())
                .authorizeExchange(exchanges -> exchanges
//                        .requestMatchers("/greeting").permitAll()
                                .anyExchange()
                                .authenticated()
                )
                .oauth2Client(Customizer.withDefaults())
                .oauth2Login(Customizer.withDefaults());

        return http.build();

    }

}