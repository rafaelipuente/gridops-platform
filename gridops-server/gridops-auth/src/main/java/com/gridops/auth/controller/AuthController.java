package com.gridops.auth.controller;

import com.gridops.auth.dto.LoginRequest;
import com.gridops.auth.dto.LoginResponse;
import com.gridops.auth.dto.UserSummaryDto;
import com.gridops.auth.service.AuthService;
import com.gridops.auth.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserSummaryDto> me(@AuthenticationPrincipal UserDetails userDetails) {
        UserSummaryDto user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/engineers")
    public ResponseEntity<List<UserSummaryDto>> engineers() {
        return ResponseEntity.ok(userService.findEngineers());
    }
}
