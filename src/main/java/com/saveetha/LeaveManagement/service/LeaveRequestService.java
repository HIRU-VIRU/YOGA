package com.saveetha.LeaveManagement.service;

import com.saveetha.LeaveManagement.dto.LeaveHistoryDTO;
import com.saveetha.LeaveManagement.dto.LeaveRequestDTO;
import com.saveetha.LeaveManagement.entity.Employee;
import com.saveetha.LeaveManagement.entity.LeaveAlteration;
import com.saveetha.LeaveManagement.entity.LeaveRequest;
import com.saveetha.LeaveManagement.entity.LeaveType;
import com.saveetha.LeaveManagement.enums.AlterationType;
import com.saveetha.LeaveManagement.enums.LeaveStatus;
import com.saveetha.LeaveManagement.enums.NotificationStatus;
import com.saveetha.LeaveManagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveAlterationRepository leaveAlterationRepository;
    private final LeaveApprovalService leaveApprovalService;
    private final LeaveValidationService leaveValidationService;

    public LeaveRequest createDraftLeaveRequest(LeaveRequestDTO leaveRequestdto) {
        Employee employee = employeeRepository.findByEmpId(leaveRequestdto.getEmpId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LeaveType leaveType = leaveTypeRepository.findById(leaveRequestdto.getLeaveTypeId())
                .orElseThrow(() -> new RuntimeException("LeaveType not found"));
        // -----CALL APPROPRIATE VALIDATION BASED ON LEAVE TYPE ------------
        String leaveName = leaveType.getTypeName().toLowerCase();
        System.out.println("Leave Type Name: " + leaveName);
        switch (leaveName) {
            case "cl":
                leaveValidationService.validateCasualLeave(leaveRequestdto);
                break;
            case "permission":
                leaveValidationService.validatePermissionLeave(leaveRequestdto);
                break;
            case "ml":
                leaveValidationService.validateMedicalLeave(leaveRequestdto);
                break;
            case "el":
                leaveValidationService.validateEarnedLeave(leaveRequestdto);
                break;
            case "comp off":
                leaveValidationService.validateCompOff(leaveRequestdto);
                break;
            case "lop":
                leaveValidationService.validatelop(leaveRequestdto);
                break;
            case "vacation":
                leaveValidationService.validatevacation(leaveRequestdto);
                break;
            case "late":
                leaveValidationService.validatelate(leaveRequestdto);
                break;
            default:
                throw new RuntimeException("Unknown Leave Type: " + leaveName);
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(leaveRequestdto.getStartDate());
        leaveRequest.setEndDate(leaveRequestdto.getEndDate());
        leaveRequest.setStartTime(leaveRequestdto.getStartTime());
        leaveRequest.setEndTime(leaveRequestdto.getEndTime());
        leaveRequest.setReason(leaveRequestdto.getReason());
        leaveRequest.setEarnedDate(leaveRequestdto.getEarnedDate());
        leaveRequest.setFileUpload(leaveRequestdto.getFileUpload());
        if (leaveRequestdto.getHasClass() != null && leaveRequestdto.getHasClass()) {
            leaveRequest.setStatus(LeaveStatus.DRAFT);
            return leaveRequestRepository.save(leaveRequest);
        } else {
            leaveRequest.setStatus(LeaveStatus.PENDING);
            LeaveRequest savedLeaveRequest = leaveRequestRepository.save(leaveRequest);
            leaveApprovalService.initiateApprovalFlow(savedLeaveRequest.getRequestId());
            return savedLeaveRequest;
        }
    }

    public String submitLeaveRequest(Integer requestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Leave Request not found"));

        if (!leaveRequest.getStatus().equals(LeaveStatus.DRAFT)) {
            throw new RuntimeException("Only DRAFT leave requests can be submitted");
        }

        // Get alterations linked to this leave request
        List<LeaveAlteration> alterations = leaveAlterationRepository.findByLeaveRequest_RequestId(requestId);

        boolean hasAlteration = !alterations.isEmpty();

        if (hasAlteration) {
            for (LeaveAlteration alt : alterations) {
                if (alt.getAlterationType() == AlterationType.STAFF_ALTERATION) {
                    if (alt.getNotificationStatus() != NotificationStatus.APPROVED) {
                        throw new RuntimeException("All staff alterations must be approved before submission.");
                    }
                }
            }
        }

        // If no alteration or all alterations are valid, proceed
        leaveRequest.setStatus(LeaveStatus.PENDING);
        leaveRequestRepository.save(leaveRequest);
        leaveApprovalService.initiateApprovalFlow(requestId);
        return "Leave request submitted successfully!";
    }


    public String withdrawLeaveRequest(Integer requestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (leaveRequest.getStatus() == LeaveStatus.PENDING || leaveRequest.getStatus() == LeaveStatus.APPROVED) {
            leaveRequest.setStatus(LeaveStatus.WITHDRAWN);
            leaveRequestRepository.save(leaveRequest);
            return "Leave request withdrawn successfully.";
        } else {
            throw new RuntimeException("Only PENDING or APPROVED leave requests can be withdrawn.");
        }
    }
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    public LeaveRequest getLeaveRequestById(Integer id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave Request not found with ID: " + id));
    }

    public void deleteLeaveRequest(Integer id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave Request not found with ID: " + id));
        leaveRequestRepository.delete(leaveRequest);
    }
    private String getCurrentUserEmpId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername(); // assuming empId is used as username
        } else {
            throw new RuntimeException("Unable to extract user details from security context");
        }
    }
    public List<LeaveHistoryDTO> getLeaveHistoryForCurrentUser() {
        String empId = getCurrentUserEmpId();
        List<Object[]> rawData = leaveRequestRepository.getLeaveHistoryForEmployee(empId);

        return rawData.stream().map(row -> new LeaveHistoryDTO(
                (Integer) row[0],
                (String) row[1],
                ((java.sql.Date) row[2]).toLocalDate(),
                ((java.sql.Date) row[3]).toLocalDate(),
                (String) row[4],
                (String) row[5],
                ((java.sql.Timestamp) row[6]).toLocalDateTime()
        )).toList();
    }



}
