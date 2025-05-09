package com.saveetha.LeaveManagement.service;

import com.saveetha.LeaveManagement.dto.LeaveRequestDTO;
import com.saveetha.LeaveManagement.entity.Employee;
import com.saveetha.LeaveManagement.entity.EmployeeLeaveBalance;
import com.saveetha.LeaveManagement.entity.LeaveRequest;
import com.saveetha.LeaveManagement.entity.LeaveType;
import com.saveetha.LeaveManagement.enums.LeaveStatus;
import com.saveetha.LeaveManagement.repository.EmployeeLeaveBalanceRepository;
import com.saveetha.LeaveManagement.repository.EmployeeRepository;
import com.saveetha.LeaveManagement.repository.LeaveRequestRepository;
import com.saveetha.LeaveManagement.repository.LeaveTypeRepository;
import com.saveetha.LeaveManagement.utility.AcademicMonthCycleUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class LeaveValidationService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository;
    private final AcademicMonthCycleUtil academicMonthCycleUtil ;

    public LeaveValidationService(LeaveRequestRepository leaveRequestRepository,AcademicMonthCycleUtil academicMonthCycleUtil, EmployeeRepository employeeRepository, LeaveTypeRepository leaveTypeRepository, EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.academicMonthCycleUtil = academicMonthCycleUtil;
        this.employeeRepository = employeeRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.employeeLeaveBalanceRepository = employeeLeaveBalanceRepository;
    }

    public void validatePermissionLeave(LeaveRequestDTO leaveRequestdto) {

    }

    public void validateMedicalLeave(LeaveRequestDTO dto) {
        // Step 1: Ensure at least 3 consecutive days
        long requestedDays = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        if (requestedDays < 3) {
            throw new RuntimeException("Medical leave must be taken for 3 or more consecutive days.");
        }

        // Step 2: Ensure a medical certificate is uploaded
        if (dto.getFileUpload() == null || dto.getFileUpload().isEmpty()) {
            throw new RuntimeException("Medical leave requires a medical certificate to be uploaded.");
        }

        // Step 3: Retrieve Employee and LeaveType
        Employee employee = employeeRepository.findByEmpId(dto.getEmpId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LeaveType mlLeaveType = leaveTypeRepository.findByTypeNameIgnoreCase("ml")
                .orElseThrow(() -> new RuntimeException("Medical Leave type not found"));

        // Step 4: Get current leave balance
        EmployeeLeaveBalance balance = employeeLeaveBalanceRepository
                .findByEmployeeAndLeaveType(employee, mlLeaveType)
                .orElseThrow(() -> new RuntimeException("Medical Leave balance not found"));

        // Step 5: Prevent drafting if balance is zero
        if (balance.getBalanceLeave() <= 0) {
            throw new RuntimeException("You have no remaining Medical Leave balance.");
        }

        // Step 6: Check for overlapping approved/pending ML requests
        List<LeaveRequest> overlappingRequests = leaveRequestRepository.findOverlappingLeaveRequests(
                dto.getEmpId(),
                "ml",
                Arrays.asList(LeaveStatus.PENDING, LeaveStatus.APPROVED),
                dto.getStartDate(),
                dto.getEndDate()
        );
        if (!overlappingRequests.isEmpty()) {
            throw new RuntimeException("Overlapping Medical Leave request exists for the selected dates.");
        }

        // Step 7: Total used + pending leave must not exceed 6
        int totalUsedOrPendingML = leaveRequestRepository.sumTotalDaysOfPendingAndApprovedML(dto.getEmpId(), "ml");
        int futureTotal = totalUsedOrPendingML + (int) requestedDays;

        if (futureTotal > 6) {
            throw new RuntimeException("This request exceeds the allowed Medical Leave limit (6 days). " +
                    "Currently used/pending: " + totalUsedOrPendingML + ", Requested: " + requestedDays);
        }
    }


    public void validateEarnedLeave(LeaveRequestDTO dto) {
        // Step 1: Ensure at least 3 consecutive days
        long requestedDays = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        if (requestedDays < 3) {
            throw new RuntimeException("Earned Leave must be taken for 3 or more consecutive days.");
        }

        // Step 2: Retrieve Employee and LeaveType
        Employee employee = employeeRepository.findByEmpId(dto.getEmpId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LeaveType elLeaveType = leaveTypeRepository.findByTypeNameIgnoreCase("el")
                .orElseThrow(() -> new RuntimeException("Earned Leave type not found"));

        // Step 3: Get current leave balance
        EmployeeLeaveBalance balance = employeeLeaveBalanceRepository
                .findByEmployeeAndLeaveType(employee, elLeaveType)
                .orElseThrow(() -> new RuntimeException("Earned Leave balance not found"));

        if (balance.getBalanceLeave() <= 0) {
            throw new RuntimeException("You have no remaining Earned Leave balance.");
        }

        // Step 4: Check for overlapping EL requests
        List<LeaveRequest> overlappingRequests = leaveRequestRepository.findOverlappingLeaveRequests(
                dto.getEmpId(),
                "el",
                Arrays.asList(LeaveStatus.PENDING, LeaveStatus.APPROVED),
                dto.getStartDate(),
                dto.getEndDate()
        );
        if (!overlappingRequests.isEmpty()) {
            throw new RuntimeException("Overlapping Earned Leave request exists for the selected dates.");
        }

        // Step 5: Total used + pending must not exceed 12
        int totalUsedOrPendingEL = leaveRequestRepository.sumTotalDaysOfPendingAndApprovedML(dto.getEmpId(), "el");
        int futureTotal = totalUsedOrPendingEL + (int) requestedDays;

        if (futureTotal > 12) {
            throw new RuntimeException("This request exceeds the allowed Earned Leave limit (12 days). " +
                    "Currently used/pending: " + totalUsedOrPendingEL + ", Requested: " + requestedDays);
        }
    }
    public void validateCompOff(LeaveRequestDTO dto) {
        // Step 1: Ensure earned date is provided
        if (dto.getEarnedDate() == null) {
            throw new RuntimeException("Comp Off leave requires an earned date.");
        }

        // Step 2: Earned date must be before the leave start date
        if (!dto.getEarnedDate().isBefore(dto.getStartDate())) {
            throw new RuntimeException("Earned Date must be before the leave Start Date.");
        }

        // Step 3: Retrieve the leave type ID for "comp off" from the database
        LeaveType compOffType = leaveTypeRepository.findByTypeNameIgnoreCase("comp off")
                .orElseThrow(() -> new RuntimeException("Comp Off leave type not found."));

        Integer compOffTypeId = compOffType.getLeaveTypeId();  // Get leaveTypeId (Integer)

        // Step 4: Check if the earned date is already used for another Comp Off request (Pending, Approved, or Draft)
        List<LeaveRequest> existingEarnedDateRequests = leaveRequestRepository.findCompOffRequestsByEarnedDate(
                dto.getEmpId(),
                compOffTypeId,  // Use the correct leaveTypeId for 'Comp Off'
                Arrays.asList(LeaveStatus.PENDING, LeaveStatus.APPROVED, LeaveStatus.DRAFT), // Checking for PENDING, APPROVED, or DRAFT status
                dto.getEarnedDate()
        );

        // If any requests exist, throw an exception to prevent using the same earned date again
        if (!existingEarnedDateRequests.isEmpty()) {
            throw new RuntimeException("The earned date you have already used");
        }

        // Step 5: Check for overlapping Comp Off leave requests (same leave type, start date, end date)
        List<LeaveRequest> overlappingRequests = leaveRequestRepository.findOverlappingLeaveRequestsByTypeId(
                dto.getEmpId(),
                compOffTypeId,  // Pass the Integer leaveTypeId here
                Arrays.asList(LeaveStatus.PENDING, LeaveStatus.APPROVED, LeaveStatus.DRAFT), // Checking for PENDING, APPROVED, or DRAFT status
                dto.getStartDate(),
                dto.getEndDate()
        );

        // If any overlapping requests are found, throw an exception
        if (!overlappingRequests.isEmpty()) {
            throw new RuntimeException("Overlapping Comp Off leave request exists for the selected dates.");
        }
    }
    public void validateCasualLeave(LeaveRequestDTO dto) {
        String empId = dto.getEmpId();
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();

        // Step 1: Get CL type
        LeaveType clType = leaveTypeRepository.findByTypeNameIgnoreCase("CL")
                .orElseThrow(() -> new RuntimeException("CL leave type not found"));
        Integer clTypeId = clType.getLeaveTypeId();

        // Step 2: Academic Year Start
        LocalDate academicStart = academicMonthCycleUtil.getAcademicYearStart();
        LocalDate now = LocalDate.now();

        // Step 3: Build all academic month ranges from academicStart to now
        List<MonthRange> academicMonths = buildAcademicMonthRanges(academicStart, now);

        // Step 4: Get employee joining date
        Employee employee = employeeRepository.findByEmpId(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        LocalDate joiningDate = employee.getJoiningDate();

        // Step 5: For each academic month, check if CL was used. If not, it's available
        int totalAvailableCL = 0;

        for (MonthRange month : academicMonths) {
            // 👇 Skip the joining month if employee joined after academic year started
            if (joiningDate != null &&
                    !joiningDate.isBefore(month.getStart()) &&
                    !joiningDate.isAfter(month.getEnd())) {
                continue;
            }

            boolean usedThisMonth = leaveRequestRepository.existsCLUsedInMonth(
                    empId, clTypeId,
                    Arrays.asList(LeaveStatus.DRAFT, LeaveStatus.PENDING, LeaveStatus.APPROVED),
                    month.getStart(), month.getEnd()
            );
            if (!usedThisMonth) {
                totalAvailableCL++;
            }
        }

        // Step 6: Check total CL used so far
        int usedCLs = leaveRequestRepository.countCLsUsed(
                empId, clTypeId,
                Arrays.asList(LeaveStatus.DRAFT, LeaveStatus.PENDING, LeaveStatus.APPROVED),
                academicStart, now
        );

        int clRemaining = totalAvailableCL - usedCLs;
        if (clRemaining <= 0) {
            throw new RuntimeException("You have used all your available CLs.");
        }

        // Step 7: Prevent overlap
        List<LeaveRequest> overlappingRequests = leaveRequestRepository.findOverlappingLeaveRequestsByTypeId(
                empId, clTypeId,
                Arrays.asList(LeaveStatus.DRAFT, LeaveStatus.PENDING, LeaveStatus.APPROVED),
                startDate, endDate
        );
        if (!overlappingRequests.isEmpty()) {
            throw new RuntimeException("A CL request already exists for the selected date range.");
        }
    }

    public class MonthRange {
        private LocalDate start;
        private LocalDate end;

        public MonthRange(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
        }

        public LocalDate getStart() {
            return start;
        }

        public LocalDate getEnd() {
            return end;
        }
    }
    private List<MonthRange> buildAcademicMonthRanges(LocalDate academicStart, LocalDate now) {
        List<MonthRange> ranges = new ArrayList<>();

        // Academic cycle is fixed: May 26 to May 25 of next year
        int year = academicStart.getYear();
        boolean isLeap = Year.of(year + 1).isLeap(); // Feb in next calendar year

        LocalDate[] startDates = new LocalDate[]{
                LocalDate.of(year, 5, 26),
                LocalDate.of(year, 6, 25),
                LocalDate.of(year, 7, 26),
                LocalDate.of(year, 8, 26),
                LocalDate.of(year, 9, 25),
                LocalDate.of(year, 10, 26),
                LocalDate.of(year, 11, 25),
                LocalDate.of(year, 12, 26),
                LocalDate.of(year + 1, 1, 26),
                isLeap ? LocalDate.of(year + 1, 2, 24) : LocalDate.of(year + 1, 2, 23),
                LocalDate.of(year + 1, 3, 26),
                LocalDate.of(year + 1, 4, 25)
        };

        LocalDate[] endDates = new LocalDate[]{
                LocalDate.of(year, 6, 24),
                LocalDate.of(year, 7, 25),
                LocalDate.of(year, 8, 25),
                LocalDate.of(year, 9, 24),
                LocalDate.of(year, 10, 25),
                LocalDate.of(year, 11, 24),
                LocalDate.of(year, 12, 25),
                LocalDate.of(year + 1, 1, 25),
                isLeap ? LocalDate.of(year + 1, 2, 23) : LocalDate.of(year + 1, 2, 22),
                LocalDate.of(year + 1, 3, 25),
                LocalDate.of(year + 1, 4, 24),
                LocalDate.of(year + 1, 5, 25)
        };

        for (int i = 0; i < startDates.length; i++) {
            LocalDate start = startDates[i];
            LocalDate end = endDates[i];

            if (start.isAfter(now)) break;
            if (end.isAfter(now)) end = now;

            ranges.add(new MonthRange(start, end));
        }

        return ranges;
    }



    public void validatelop(LeaveRequestDTO leaveRequestDTO){

    }
    public void validatevacation(LeaveRequestDTO leaveRequestDTO){

    }
    public void validatelate(LeaveRequestDTO leaveRequestDTO){

    }
}
