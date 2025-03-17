package org.hadilta.com.resourceserver.controller;

import org.hadilta.com.resourceserver.service.KeycloakUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class AdminController {

    private final KeycloakUserService keycloakUserService;

    public AdminController(KeycloakUserService keycloakUserService) {
        this.keycloakUserService = keycloakUserService;
    }


    @PreAuthorize("hasRole('client_admin')")
    @GetMapping("/resadmin/info")
    public ResponseEntity<Map<String, Object>> greetingAdmin() {
        List<Map<String, Object>> users = keycloakUserService.getAllUsers();
        List<Map<String, Object>> clients = keycloakUserService.getAllClients();

        Map<String, Object> response = Map.of(
                "users", users,
                "clients", clients
        );
        System.out.println(response);
        return ResponseEntity.ok(response);
    }

//    @PreAuthorize( "hasRole('client_admin')")
//    @GetMapping("/clients/{clientId}/roles")
//    @ResponseBody
//    public List<Map<String, Object>> getClientRoles(@PathVariable String clientId) {
//        return keycloakUserService.getClientRoles(clientId);
//    }
//
//    @PreAuthorize("hasRole('client_admin')")
//    @PostMapping("/clients/{clientId}/roles")
//    @ResponseBody
//    public ResponseEntity<String> addNewRole(@PathVariable String clientId, @RequestBody Map<String, String> request) {
//        String roleName = request.get("roleName");
//        String description = request.getOrDefault("description", "No description provided");
//
//        if (roleName == null || roleName.isEmpty()) {
//            return ResponseEntity.badRequest().body("Role name is required");
//        }
//
//        boolean success = keycloakUserService.addRoleToClient(clientId, roleName, description);
//        return success ? ResponseEntity.ok("Role added successfully") : ResponseEntity.status(500).body("Failed to add role");
//    }

}