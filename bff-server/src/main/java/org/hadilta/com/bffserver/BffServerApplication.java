package org.hadilta.com.bffserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BffServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffServerApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("gateway", r -> r.path("/api/test")
                        .uri("http://localHost:8083"))
                .route("ResourceServer", r -> r.path("/greeting/**")
                        .filters(GatewayFilterSpec::tokenRelay)
                        .uri("http://localHost:8081"))
                .build();
    }

}

