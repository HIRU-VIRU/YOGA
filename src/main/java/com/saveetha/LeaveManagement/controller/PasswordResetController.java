package com.saveetha.LeaveManagement.controller;

import com.saveetha.LeaveManagement.service.PasswordResetService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody EmailRequest request) {
        Optional<String> tokenOpt = passwordResetService.createPasswordResetToken(request.getEmail());

        if (tokenOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Email not found");
        }

        // ✅ Only return the plain token string
        String token = tokenOpt.get();

        return ResponseEntity.ok(new TokenResponse(token, "Use this token to reset your password."));
    }


    @PostMapping("/reset-password")
        public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
            boolean valid = passwordResetService.validatePasswordResetToken(request.getToken());
            if (!valid) {
                return ResponseEntity.badRequest().body("Invalid or expired token");
            }

            boolean success = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            if (success) {
                return ResponseEntity.ok("Password reset successful");
            } else {
                return ResponseEntity.badRequest().body("Password reset failed");
            }
        }

        // DTO classes
        @Data
        static class EmailRequest {
            private String email;
        }

        @Data
        static class TokenResponse {
            private final String token;
            private final String message;
        }

        @Data
        static class ResetPasswordRequest {
            private String token;
            private String newPassword;
        }
    }