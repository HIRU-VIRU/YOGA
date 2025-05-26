package com.saveetha.LeaveManagement.service;

import com.saveetha.LeaveManagement.dto.AssignApprovalFlowDTO;
import com.saveetha.LeaveManagement.dto.CreateEmployeeRequestDto;
import com.saveetha.LeaveManagement.dto.EmployeeUpdateDTO;
import com.saveetha.LeaveManagement.entity.ApprovalFlow;
import com.saveetha.LeaveManagement.entity.Department;
import com.saveetha.LeaveManagement.entity.Employee;
import com.saveetha.LeaveManagement.entity.LeaveType;
import com.saveetha.LeaveManagement.enums.StaffType;
import com.saveetha.LeaveManagement.repository.ApprovalFlowRepository;
import com.saveetha.LeaveManagement.repository.DepartmentRepository;
import com.saveetha.LeaveManagement.repository.EmployeeRepository;
import com.saveetha.LeaveManagement.repository.LeaveTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveResetService leaveResetService;
    private final ApprovalFlowRepository approvalFlowRepository;
    private LeaveTypeRepository leaveTypeRepository;
    private  InitializeLeaveBalanceService initializeLeaveBalanceService;


    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           LeaveResetService leaveResetService,
                           LeaveTypeRepository leaveTypeRepository,
                           InitializeLeaveBalanceService initializeLeaveBalanceService,
                           ApprovalFlowRepository approvalFlowRepository) {
        this.employeeRepository = employeeRepository;
        this.leaveResetService = leaveResetService;
        this.initializeLeaveBalanceService = initializeLeaveBalanceService;
        this.leaveTypeRepository = leaveTypeRepository;
        this.departmentRepository = departmentRepository;
        this.approvalFlowRepository = approvalFlowRepository;
    }

    public boolean updateEmployee(String empId, EmployeeUpdateDTO employeeUpdateDTO) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmpId(empId);

        if (optionalEmployee.isPresent()) {
            Employee employee = optionalEmployee.get();


            // ✅ Update fields only if they are not null
            if (employeeUpdateDTO.getEmpName() != null) {
                employee.setEmpName(employeeUpdateDTO.getEmpName());
            }
            if (employeeUpdateDTO.getDesignation() != null) {
                employee.setDesignation(employeeUpdateDTO.getDesignation());
            }
            if (employeeUpdateDTO.getDepartmentId() != null) {
                Department department = departmentRepository.findById(employeeUpdateDTO.getDepartmentId())
                        .orElseThrow(() -> new RuntimeException("Department not found"));
                employee.setDepartment(department);
            }



            if (employeeUpdateDTO.getStaffType() != null) {
                try {
                    StaffType staffType = StaffType.valueOf(employeeUpdateDTO.getStaffType().toUpperCase());
                    employee.setStaffType(staffType);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Invalid staff type provided");
                }
            }


            // Check if the joining date is updated
            if (employeeUpdateDTO.getJoiningDate() != null && !employeeUpdateDTO.getJoiningDate().equals(employee.getJoiningDate())) {
                employee.setJoiningDate(employeeUpdateDTO.getJoiningDate());
                System.out.println("Initializing leave balance for Employee ID: " + empId);
                System.out.println("New Joining Date: " + employeeUpdateDTO.getJoiningDate());

                // Get the list of LeaveTypes and determine the academic year based on joining date
                List<LeaveType> leaveTypes = leaveTypeRepository.findAll();  // Assuming this method is available
                String academicYear = determineAcademicYear(LocalDate.now());  // e.g., "2024-2025"
                // Pass only the joining date

                // Call the leave balance initialization function from LeaveResetService
                initializeLeaveBalanceService.initializeLeaveBalance(employee, leaveTypes, academicYear);
            }

            employeeRepository.save(employee);
            return true;
        }

        return false;
    }
    public void updateProfilePic(String empId, String imageUrl) {
        Employee employee = employeeRepository.findByEmpId(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + empId));

        employee.setProfilePicture(imageUrl);
        employeeRepository.save(employee);
    }
    public String determineAcademicYear(LocalDate date) {
        int year = date.getYear();
        LocalDate academicYearStart = LocalDate.of(year, 5, 26); // 26 May of the current year

        if (date.isBefore(academicYearStart)) {
            return (year - 1) + "-" + year;
        } else {
            return year + "-" + (year + 1);
        }
    }


    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(String empId) {
        return employeeRepository.findByEmpId(empId);
    }

    // Soft delete (set active = false)
    public boolean deactivateEmployee(String empId) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmpId(empId);
        if (optionalEmployee.isPresent()) {
            Employee employee = optionalEmployee.get();
            employee.setActive(false);
            employeeRepository.save(employee);
            return true;
        }
        return false;
    }
    // calculate the current year
    private String calculateCurrentAcademicYear() {
        LocalDate now = LocalDate.now();
        return now.getYear() + "-" + (now.getYear() + 1); // Example: "2025-2026"
    }

    //  Reactivate (set active = true)
    public boolean activateEmployee(String empId) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmpId(empId);
        if (optionalEmployee.isPresent()) {
            Employee employee = optionalEmployee.get();
            employee.setActive(true);
            employeeRepository.save(employee);
            return true;
        }
        return false;
    }
    //  Optional: Hard delete (not recommended)
    public boolean deleteEmployee(String empId) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmpId(empId);
        if (optionalEmployee.isPresent()) {
            employeeRepository.delete(optionalEmployee.get());
            return true;
        }
        return false;
    }

    public List<Employee> getActiveEmployees() {
        return employeeRepository.findByactiveTrue();
    }

    public boolean updateEmployeeWithApprovalFlow(AssignApprovalFlowDTO dto) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmpId(dto.getEmpId());
        Optional<ApprovalFlow> flowOpt = approvalFlowRepository.findById(dto.getApprovalFlowId());

        if (employeeOpt.isPresent() && flowOpt.isPresent()) {
            Employee employee = employeeOpt.get();

            employee.setEmpName(dto.getEmpName());
            employee.setDesignation(dto.getDesignation());
            employee.setProfilePicture(dto.getProfilePicture());

            // Update department
            if (dto.getDepartmentId() != null) {
                Department department = departmentRepository.findById(dto.getDepartmentId())
                        .orElseThrow(() -> new RuntimeException("Department not found"));
                employee.setDepartment(department);
            }

            // Update staff type
            if (dto.getStaffType() != null) {
                try {
                    StaffType staffType = StaffType.valueOf(dto.getStaffType().toUpperCase());
                    employee.setStaffType(staffType);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Invalid staff type provided");
                }
            }

            // Update joining date & reinitialize leave balance if needed
            if (dto.getJoiningDate() != null && !dto.getJoiningDate().equals(employee.getJoiningDate())) {
                employee.setJoiningDate(dto.getJoiningDate());

                List<LeaveType> leaveTypes = leaveTypeRepository.findAll();
                String academicYear = determineAcademicYear(LocalDate.now());

                initializeLeaveBalanceService.initializeLeaveBalance(employee, leaveTypes, academicYear);
            }

            // Assign approval flow
            employee.setApprovalFlow(flowOpt.get());

            employeeRepository.save(employee);
            return true;
        }

        return false;
    }


    public Employee createEmployee(CreateEmployeeRequestDto dto) {
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Employee with this email already exists");
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        Employee employee = new Employee();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        employee.setPassword(passwordEncoder.encode(dto.getPassword()));

        employee.setEmpName(dto.getEmpName());
        employee.setEmpId(dto.getEmpId());
        employee.setEmail(dto.getEmail());
        employee.setJoiningDate(dto.getJoiningDate());
        employee.setDesignation(dto.getDesignation());
        employee.setRole(dto.getRole());
        employee.setDepartment(department);
        employee.setActive(true);

        employeeRepository.save(employee);

        // ✅ Fetch leave types and determine academic year
        List<LeaveType> leaveTypes = leaveTypeRepository.findAll();
        String academicYear = determineAcademicYear(dto.getJoiningDate());

        // ✅ Initialize leave balance
        initializeLeaveBalanceService.initializeLeaveBalance(employee, leaveTypes, academicYear);

        return employee;
    }
    public boolean assignApprovalFlow(String empId, Integer approvalFlowId) {
        Optional<Employee> employeeOpt = employeeRepository.findById(empId);
        Optional<ApprovalFlow> flowOpt = approvalFlowRepository.findById(approvalFlowId);

        if (employeeOpt.isPresent() && flowOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.setApprovalFlow(flowOpt.get());
            employeeRepository.save(employee);
            return true;
        }
        return false;
    }



}
