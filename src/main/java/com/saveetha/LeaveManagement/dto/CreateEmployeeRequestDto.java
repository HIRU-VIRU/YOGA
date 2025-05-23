package com.saveetha.LeaveManagement.dto;

import com.saveetha.LeaveManagement.enums.Role;
import com.saveetha.LeaveManagement.enums.StaffType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateEmployeeRequestDto {
    private String empId; // Required since it's the primary key
    private String empName;
    private String email;
    private String password;
    private LocalDate joiningDate;
    private String designation;
    private Long departmentId;
    private Role role = Role.EMPLOYEE; // Optional in request, default to EMPLOYEE
    private StaffType staffType;       // Optional (TEACHING / NON_TEACHING)
    private boolean active = true;     // Optional, default to true
    private Long approvalFlowId;       // Optional, if you assign it at creation
}
