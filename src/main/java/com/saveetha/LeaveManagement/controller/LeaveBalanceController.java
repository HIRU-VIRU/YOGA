package com.saveetha.LeaveManagement.controller;

import com.saveetha.LeaveManagement.dto.LeaveBalanceDto;
import com.saveetha.LeaveManagement.entity.Employee;
import com.saveetha.LeaveManagement.security.JwtUtil;
import com.saveetha.LeaveManagement.service.LeaveBalanceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class LeaveBalanceController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @GetMapping("/leave-balance")
    public Object getLeaveBalance(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7); // remove "Bearer "
        String empId = jwtUtil.extractEmpId(token);
        String role = jwtUtil.extractRole(token);

        if ("ADMIN".equalsIgnoreCase(role)) {
            return leaveBalanceService.getAllEmployeesLeaveBalance();
        } else {
            Employee employee = leaveBalanceService.getEmployeeById(empId);
            return leaveBalanceService.getEmployeeLeaveBalance(employee);
        }
    }
    @GetMapping("/leave-balance/me")
    public Object getMyLeaveBalance(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String empId = jwtUtil.extractEmpId(token);

        Employee employee = leaveBalanceService.getEmployeeById(empId);
        return leaveBalanceService.getEmployeeLeaveBalance(employee);
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/leave-balance/{empId}")
    public Map<String, LeaveBalanceDto> getLeaveBalanceByEmpId(@PathVariable String empId, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7); // remove "Bearer "
        String role = jwtUtil.extractRole(token);

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access Denied: Only Admin can access this endpoint");
        }

        Employee employee = leaveBalanceService.getEmployeeById(empId);
        return leaveBalanceService.getEmployeeLeaveBalance(employee);
}

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PatchMapping("/leave-balance/{empId}")
    public ResponseEntity<Map<String, LeaveBalanceDto>> updateLeaveBalanceByEmpId(
            @PathVariable String empId,
            @RequestBody Map<String, LeaveBalanceDto> leaveBalances,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7); // remove "Bearer "
        String role = jwtUtil.extractRole(token);

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access Denied: Only Admin can update leave balances.");
        }

        Map<String, LeaveBalanceDto> updated = leaveBalanceService.updateEmployeeLeaveBalances(empId, leaveBalances);
        return ResponseEntity.ok(updated);
}
}