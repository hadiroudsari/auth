package org.hadilta.com.bffserver;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class BffServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffServerApplication.class, args);
    }

    @Autowired
    private EurekaClient discoveryClient;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        InstanceInfo ResourceServer = discoveryClient.getNextServerFromEureka("RESOURCESERVER", false);
        System.out.println(ResourceServer.getHomePageUrl());
        return builder.routes()
                .route("gateway", r -> r.path("/api/test")
                        .uri("http://localHost:8083"))
                .route("ResourceServer", r -> r.path("/greeting/**")
                        .filters(GatewayFilterSpec::tokenRelay)
                        .uri(ResourceServer.getHomePageUrl()))
                .build();
    }

}

