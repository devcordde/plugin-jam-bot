package de.chojo.pluginjam.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;

import java.util.Map;

@Controller("/api/user")
public class UserController {

    @Get("/me")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    public Map<String, Object> getCurrentUser(Authentication authentication) {
        return Map.of(
                "name", authentication.getAttributes().getOrDefault("name", "Unknown"),
                "picture", authentication.getAttributes().getOrDefault("picture", ""),
                "id", authentication.getName()
        );
    }
}
