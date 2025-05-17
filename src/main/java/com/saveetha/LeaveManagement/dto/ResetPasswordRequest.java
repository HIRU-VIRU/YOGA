// ResetPasswordRequest.java
package com.saveetha.LeaveManagement.dto;

import jakarta.validation.constraints.NotBlank;

public class ResetPasswordRequest {
    @NotBlank(message = "Employee ID is required")
    private String empId;

    @NotBlank(message = "New password is required")
    private String newPassword;

    @NotBlank(message = "Reset token is required")
    private String token;

    // getters and setters
    public String getEmpId() {
        return empId;
    }
    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getNewPassword() {
        return newPassword;
    }
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
}
