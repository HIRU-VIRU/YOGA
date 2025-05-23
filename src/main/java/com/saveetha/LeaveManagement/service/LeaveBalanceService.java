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

    public Map<String, LeaveBalanceDto> updateEmployeeLeaveBalances(String empId, Map<String, LeaveBalanceDto> leaveBalances) {
        Employee employee = getEmployeeById(empId);
        List<EmployeeLeaveBalance> existingBalances = balanceRepo.findByEmployee(employee); // You may need this method in repo

        Map<String, EmployeeLeaveBalance> balanceMap = new HashMap<>();
        for (EmployeeLeaveBalance bal : existingBalances) {
            balanceMap.put(bal.getLeaveType().getTypeName(), bal);
        }

        for (Map.Entry<String, LeaveBalanceDto> entry : leaveBalances.entrySet()) {
            String leaveType = entry.getKey();
            LeaveBalanceDto dto = entry.getValue();

            if (balanceMap.containsKey(leaveType)) {
                EmployeeLeaveBalance bal = balanceMap.get(leaveType);
                bal.setUsedLeaves(dto.getUsed());
                bal.setBalanceLeave(dto.getTotal() - dto.getUsed());
                balanceRepo.save(bal); // Save updated balance
            } else {
                throw new RuntimeException("Leave type " + leaveType + " not found for employee " + empId);
            }
        }
        return leaveBalances;
    }



}