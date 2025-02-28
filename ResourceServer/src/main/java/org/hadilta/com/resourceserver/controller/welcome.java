package org.hadilta.com.resourceserver.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class welcome {
    @PreAuthorize("hasRole('client_user')")
    @GetMapping("/greeting")
    public String greeting() {
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        String username = ((UserDetails)principal).getUsername();
        return "hello to everyone";
    }

    @PreAuthorize("hasRole('client_admin')")
    @GetMapping("/greeting/admin")
    public String greetingAdmin() {
        return "hello to admin";

    }
}
