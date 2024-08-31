package org.hadilta.com.bffserver.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Welcome {

    @GetMapping("/welcome")
    public String test() {
        return "hello world";
    }

}
