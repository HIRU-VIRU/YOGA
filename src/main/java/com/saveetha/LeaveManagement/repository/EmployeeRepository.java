package com.saveetha.LeaveManagement.repository;

import com.saveetha.LeaveManagement.entity.Employee;
import com.saveetha.LeaveManagement.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
    Optional<Employee> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Employee> findByEmpId(String empId);
    boolean existsByEmpId(String empId);
    List<Employee> findByactiveTrue();



}
