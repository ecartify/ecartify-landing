package com.ecartify.stores;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class ResourceController {
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(path = "/secure")
    public String secure(Principal p) {
        return String.format("Secure Hello %s from Web Service 1", p.getName());
    }
}
