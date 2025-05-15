package com.saveetha.LeaveManagement.service;

import com.saveetha.LeaveManagement.dto.LeaveBalanceDto;
import com.saveetha.LeaveManagement.entity.Employee;
import com.saveetha.LeaveManagement.entity.EmployeeLeaveBalance;
import com.saveetha.LeaveManagement.repository.EmployeeLeaveBalanceRepository;
import com.saveetha.LeaveManagement.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LeaveBalanceService {

    @Autowired
    private EmployeeLeaveBalanceRepository balanceRepo;

    @Autowired
    private EmployeeRepository employeeRepo;

    public Map<String, LeaveBalanceDto> getEmployeeLeaveBalance(Employee employee) {
        List<EmployeeLeaveBalance> balances = balanceRepo.findAll(); // or create a custom query for employee
        Map<String, LeaveBalanceDto> result = new HashMap<>();

        for (EmployeeLeaveBalance balance : balances) {
            if (!balance.getEmployee().getEmpId().equals(employee.getEmpId())) continue;
            String type = balance.getLeaveType().getTypeName();
            double total = balance.getUsedLeaves() + balance.getBalanceLeave();
            double used = balance.getUsedLeaves();
            result.put(type, new LeaveBalanceDto(total, used));
        }
        return result;
    }

    public Map<String, Map<String, LeaveBalanceDto>> getAllEmployeesLeaveBalance() {
        List<EmployeeLeaveBalance> balances = balanceRepo.findAll();
        Map<String, Map<String, LeaveBalanceDto>> result = new HashMap<>();

        for (EmployeeLeaveBalance balance : balances) {
            String empId = balance.getEmployee().getEmpId();
            String type = balance.getLeaveType().getTypeName();
            double total = balance.getUsedLeaves() + balance.getBalanceLeave();
            double used = balance.getUsedLeaves();

            result
                    .computeIfAbsent(empId, k -> new HashMap<>())
                    .put(type, new LeaveBalanceDto(total, used));
        }
        return result;
    }

    public Employee getEmployeeById(String empId) {
        return employeeRepo.findById(empId).orElseThrow(() -> new RuntimeException("Employee not found"));
    }
}
