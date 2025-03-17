package org.hadilta.com.resourceserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
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
                return objectMapper.readValue(response.body(), new TypeReference<List<Map<String, Object>>>() {
                });
            } else {
                throw new RuntimeException("Failed to fetch users. Status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error fetching users from Keycloak", e);
        }
    }

    public List<Map<String, Object>> getAllClients() {
        List<String> builtInClients = Arrays.asList(
                "account", "account-console", "admin-cli", "broker",
                "realm-management", "security-admin-console"
        );

        String adminToken = keycloakAuthService.getAdminAccessToken();
        if (adminToken == null) {
            throw new RuntimeException("Failed to get Keycloak admin token.");
        }

        try {
            String clientsEndpoint = keycloakUrl + "/admin/realms/" + realm + "/clients?first=0&max=100";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(clientsEndpoint))
                    .header("Authorization", "Bearer " + adminToken)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<Map<String, Object>> fetchedClients = objectMapper.readValue(
                        response.body(),
                        new TypeReference<List<Map<String, Object>>>() {}
                );

                List<Map<String, Object>> customClients = fetchedClients.stream()
                        .filter(client -> !builtInClients.contains(client.get("clientId")))
                        .toList();

                return customClients;
            } else {
                throw new RuntimeException("Failed to fetch clients. Status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error fetching clients from Keycloak", e);
        }
    }


    public List<Map<String, Object>> getClientRoles(String clientId) {
        String adminToken = keycloakAuthService.getAdminAccessToken();
        if (adminToken == null) {
            throw new RuntimeException("Failed to get Keycloak admin token.");
        }

        try {
            String rolesEndpoint = keycloakUrl + "/admin/realms/" + realm + "/clients/" + clientId + "/roles";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rolesEndpoint))
                    .header("Authorization", "Bearer " + adminToken)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), new TypeReference<List<Map<String, Object>>>() {
                });
            } else {
                throw new RuntimeException("Failed to fetch roles. Status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error fetching roles from Keycloak", e);
        }
    }

    public boolean addRoleToClient(String clientId, String roleName, String description) {
        String adminToken = keycloakAuthService.getAdminAccessToken();
        if (adminToken == null) {
            throw new RuntimeException("Failed to get Keycloak admin token.");
        }

        try {
            String rolesEndpoint = keycloakUrl + "/admin/realms/" + realm + "/clients/" + clientId + "/roles";

            Map<String, Object> rolePayload = Map.of(
                    "name", roleName,
                    "description", description,
                    "clientRole", true // ✅ Fix: Keycloak requires this for client roles
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rolesEndpoint))
                    .header("Authorization", "Bearer " + adminToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(rolePayload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {  // ✅ Fix: Accept 200 OK as success
                System.out.println("✅ Role added successfully: " + roleName);
                return true;
            } else {
                System.err.println("❌ Failed to add role: " + roleName);
                System.err.println("Response Code: " + response.statusCode());
                System.err.println("Response Body: " + response.body());  // ✅ Fix: Log full response body
                return false;
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("❌ Error adding role to Keycloak client: " + e.getMessage(), e);
        }
    }

}


