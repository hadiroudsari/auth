package org.hadilta.com.resourceserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class KeycloakAuthService {

    @Value("${keycloak.url}")
    private String keycloakUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.username}")
    private String adminUsername;

    @Value("${keycloak.password}")
    private String adminPassword;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ReentrantLock lock = new ReentrantLock();
    private String cachedAdminToken = null;
    private Instant tokenExpiryTime = Instant.MIN;

    public String getAdminAccessToken() {
        if (isTokenValid()) {
            return cachedAdminToken; //  Return immediately if valid
        }

        return fetchNewToken();
    }

    private boolean isTokenValid() {
        return cachedAdminToken != null && Instant.now().isBefore(tokenExpiryTime);
    }

    private String fetchNewToken() {
        lock.lock();
        try {
            if (isTokenValid()) {
                return cachedAdminToken; // ✅ Another thread might have already refreshed the token
            }
            HttpRequest tokenRequest = createTokenRequest();
            HttpResponse<String> response = httpClient.send(tokenRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> responseMap = objectMapper.readValue(response.body(), new TypeReference<>() {
                });
                cachedAdminToken = (String) responseMap.get("access_token");
                Integer expiresIn = (Integer) responseMap.get("expires_in");

                // Set token expiry time (refresh slightly before actual expiry)
                tokenExpiryTime = Instant.now().plusSeconds(expiresIn - 10);
                return cachedAdminToken;
            } else {
                throw new RuntimeException("Failed to get admin token. Status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error fetching admin token from Keycloak", e);
        } finally {
            lock.unlock(); //  Release the lock
        }
    }

    private HttpRequest createTokenRequest() {
        String tokenEndpoint = keycloakUrl + "/realms/master/protocol/openid-connect/token";
        String requestBody = "client_id=" + clientId +
                "&username=" + adminUsername +
                "&password=" + adminPassword +
                "&grant_type=password";

        return HttpRequest.newBuilder()
                .uri(URI.create(tokenEndpoint))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
    }

}
