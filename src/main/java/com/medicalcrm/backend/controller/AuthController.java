package com.medicalcrm.backend.controller;

import com.medicalcrm.backend.dto.request.LoginRequest;
import com.medicalcrm.backend.dto.response.LoginResponse;
import com.medicalcrm.backend.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Valid
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String role = authentication.getAuthorities()
                .iterator().next().getAuthority();

        String token = jwtService.generateToken(
                request.getUsername(),
                role
        );

        return ResponseEntity.ok(new LoginResponse(token));
    }
}