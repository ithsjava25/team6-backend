package org.example.team6backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/api/me")
    public Map<String, Object> me(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name", principal.getAttribute("name"));
        response.put("login", principal.getAttribute("login"));
        response.put("email", principal.getAttribute("email"));
        response.put("attributes", principal.getAttributes());
        return response;
    }
}