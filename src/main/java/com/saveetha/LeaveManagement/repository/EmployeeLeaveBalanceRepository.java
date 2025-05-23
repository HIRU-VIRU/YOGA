package com.saveetha.LeaveManagement.repository;

import com.saveetha.LeaveManagement.entity.Employee;
import com.saveetha.LeaveManagement.entity.EmployeeLeaveBalance;
import com.saveetha.LeaveManagement.entity.EmployeeLeaveBalanceId;
import com.saveetha.LeaveManagement.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeLeaveBalanceRepository extends JpaRepository<EmployeeLeaveBalance, EmployeeLeaveBalanceId> {
    // Define the custom method to find EmployeeLeaveBalance by employee and leave type
    // Retrieve all leave balances for a given employee
    List<EmployeeLeaveBalance> findByEmployee(Employee employee);
    Optional<EmployeeLeaveBalance> findByEmployeeAndLeaveType(Employee employee, LeaveType leaveType);
}
