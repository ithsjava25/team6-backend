package org.example.team6backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String home() {
        return "Inloggad i applikationen";
    }

    @GetMapping("/api/test")
    public String apiTest() {
        return "API fungerar";
    }
}