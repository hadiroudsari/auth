package org.hadilta.com.resourceserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakUserService {
    private final String keycloakUrl = "http://localhost:8080";
    private final String realm = "hadilka";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KeycloakAuthService keycloakAuthService;

    public KeycloakUserService(KeycloakAuthService keycloakAuthService) {
        this.keycloakAuthService = keycloakAuthService;
    }

    public List<Map<String, Object>> getAllUsers() {
        String adminToken = keycloakAuthService.getAdminAccessToken();
        if (adminToken == null) {
            throw new RuntimeException("Failed to get Keycloak admin token.");
        }

        try {
            String usersEndpoint = keycloakUrl + "/admin/realms/" + realm + "/users?first=0&max=100";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(usersEndpoint))
                    .header("Authorization", "Bearer " + adminToken)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), new TypeReference<List<Map<String, Object>>>() {});
            } else {
                throw new RuntimeException("Failed to fetch users. Status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error fetching users from Keycloak", e);
        }
    }
}


