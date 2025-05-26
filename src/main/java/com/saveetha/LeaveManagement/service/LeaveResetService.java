package com.saveetha.LeaveManagement.service;

import com.saveetha.LeaveManagement.entity.Employee;
import com.saveetha.LeaveManagement.entity.EmployeeLeaveBalance;
import com.saveetha.LeaveManagement.entity.LeaveType;
import com.saveetha.LeaveManagement.repository.EmployeeLeaveBalanceRepository;
import com.saveetha.LeaveManagement.repository.EmployeeRepository;
import com.saveetha.LeaveManagement.repository.LeaveTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
public class LeaveResetService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private EmployeeLeaveBalanceRepository balanceRepository;

    public void resetAllEmployeeLeaveBalances(String newAcademicYear) {
        List<Employee> activeEmployees = employeeRepository.findByactiveTrue(); // Only active employees
        List<LeaveType> leaveTypes = leaveTypeRepository.findAll();

        for (Employee employee : activeEmployees) {
            for (LeaveType leaveType : leaveTypes) {

                EmployeeLeaveBalance balance = balanceRepository
                        .findByEmployeeAndLeaveType(employee, leaveType)
                        .orElseGet(() -> new EmployeeLeaveBalance(
                                employee,
                                leaveType,
                                newAcademicYear,
                                0.0 // initial balance will be calculated below
                        ));

                // Calculate carry forward leave only if leaveType allows it
                double carryForward = 0.0;
                if (leaveType.getCanBeCarriedForward() != null && leaveType.getCanBeCarriedForward()) {
                    {
                    double leftover = balance.getBalanceLeave() - balance.getUsedLeaves();
                    carryForward = leftover > 0 ? leftover : 0;
                }

                // Reset used leaves to zero for new academic year
                balance.setUsedLeaves(0.0);

                // Set the carry forward leave
                balance.setCarryForwardLeave(carryForward);

                // New balance is carry forward + max allowed leave for the new year
                double newBalance = carryForward + leaveType.getMaxAllowedPerYear();
                balance.setBalanceLeave(newBalance);

                // Update academic year
                balance.setCurrentYear(newAcademicYear);

                balanceRepository.save(balance);
            }
        }

        System.out.println("Leave balances reset completed for academic year: " + newAcademicYear);
    }}}





