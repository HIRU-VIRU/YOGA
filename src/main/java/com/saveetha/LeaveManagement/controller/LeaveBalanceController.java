package com.saveetha.LeaveManagement.controller;

import com.saveetha.LeaveManagement.dto.LeaveBalanceDto;
import com.saveetha.LeaveManagement.entity.Employee;
import com.saveetha.LeaveManagement.security.JwtUtil;
import com.saveetha.LeaveManagement.service.LeaveBalanceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
