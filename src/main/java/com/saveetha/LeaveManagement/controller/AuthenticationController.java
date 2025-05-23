package com.saveetha.LeaveManagement.controller;

import com.saveetha.LeaveManagement.dto.LoginRequest;
import com.saveetha.LeaveManagement.dto.LoginResponse;
import com.saveetha.LeaveManagement.dto.RegisterRequest;
import com.saveetha.LeaveManagement.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authService;

    public AuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        // Validate input fields
        if (request.getEmpId() == null || request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body("EmpId, Email, and Password are required.");
        }

        try {
            String token = authService.register(request);
            return ResponseEntity.ok("User registered successfully. Please log in.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Call the authService to authenticate the user and generate a token
            LoginResponse response = authService.login(request);

            // Return a successful JSON response with the token
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Return an error response in JSON format
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Optionally log the logout event
        return ResponseEntity.ok("Logged out successfully");
    }
}
