package org.hadilta.com.bffserver.controller;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/admin")
public class AdminController {

    private final WebClient webClient;

    public AdminController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8082").build(); // Resource Server URL
    }

    @GetMapping
    public Mono<String> getAdminPage(Model model, Authentication authentication, @RequestHeader HttpHeaders headers) {
        System.out.println("📢 Request Headers: " + headers);

        return webClient.method(HttpMethod.GET)
                .uri("/resadmin/info") // ✅ Goes through Gateway
                .headers(httpHeaders -> httpHeaders.addAll(headers)) // ✅ Copies request headers
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    System.err.println("❌ API Error Response: " + errorBody);
                                    return Mono.error(new RuntimeException("API returned error: " + errorBody));
                                })
                )
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                }) // ✅ Expect JSON as Map
                .onErrorResume(error -> {
                    System.err.println("❌ API Request Failed: " + error.getMessage());
                    model.addAttribute("error", "Failed to load admin data. Please try again later.");

                    // ✅ Return an empty map if an error occurs to avoid type mismatch
                    return Mono.just(new HashMap<>());
                })
                .doOnSuccess(response -> {
                    System.out.println("✔ Received JSON Response: " + response);

                    Object users = response.get("users");
                    Object clients = response.get("clients");

                    System.out.println("✔ Users Data Type: " + (users != null ? users.getClass() : "null"));
                    System.out.println("✔ Clients Data Type: " + (clients != null ? clients.getClass() : "null"));

                    model.addAttribute("users", users);
                    model.addAttribute("clients", clients);
                })

                .thenReturn("adminpage"); // ✅ Always return "adminpage" on success or error
    }


}
