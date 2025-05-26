package com.saveetha.LeaveManagement.controller;

import com.saveetha.LeaveManagement.entity.LeaveType;
import com.saveetha.LeaveManagement.repository.LeaveTypeRepository;
import com.saveetha.LeaveManagement.service.AcademicYearScheduler;
import com.saveetha.LeaveManagement.service.LeaveResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/year-reset")
public class YearResetController {

    private final LeaveResetService leaveResetService;
    private final LeaveTypeRepository leaveTypeRepository;

    public YearResetController(LeaveResetService leaveResetService, LeaveTypeRepository leaveTypeRepository) {
        this.leaveResetService = leaveResetService;
        this.leaveTypeRepository = leaveTypeRepository;
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetAcademicYear() {
        String currentAcademicYear = getCurrentAcademicYear();

        // Update LeaveType academic year dates as well (optional but recommended)
        updateLeaveTypesAcademicYear();

        // Reset leave balances for that academic year
        leaveResetService.resetAllEmployeeLeaveBalances(currentAcademicYear);

        return ResponseEntity.ok("Leave balances reset for academic year: " + currentAcademicYear);
    }

    private String getCurrentAcademicYear() {
        int year = LocalDate.now().getYear();
        // Just return year range without checking month
        return year + "-" + (year + 1);
    }


    private void updateLeaveTypesAcademicYear() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusYears(1).minusDays(1);

        List<LeaveType> leaveTypes = leaveTypeRepository.findAll();
        for (LeaveType lt : leaveTypes) {
            lt.setAcademicYearStart(start);
            lt.setAcademicYearEnd(end);
            leaveTypeRepository.save(lt);
        }
    }
}

