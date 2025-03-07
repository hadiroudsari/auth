package org.hadilta.com.resourceserver.controller;

import org.hadilta.com.resourceserver.service.KeycloakUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final KeycloakUserService keycloakUserService;

    public AdminController(KeycloakUserService keycloakUserService) {
        this.keycloakUserService = keycloakUserService;
    }

    @PreAuthorize("hasRole('client_admin')")
    @GetMapping()
    public String adminPage(Model model) {
        getUsers();
        List<Map<String, Object>> users = keycloakUserService.getAllUsers();
        model.addAttribute("users", users);  // Pass users to Thymeleaf
        return "adminpage";
    }


    public void getUsers() {
        System.out.println("✅ Request received at /admin/users");  // Print to console
    var x=keycloakUserService.getAllUsers();
        System.out.println(keycloakUserService.getAllUsers().toString());
        keycloakUserService.getAllUsers().stream().forEach(user -> System.out.println(user.getClass()));
    }
}