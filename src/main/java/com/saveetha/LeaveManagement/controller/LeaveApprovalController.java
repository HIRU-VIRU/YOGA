package com.saveetha.LeaveManagement.controller;

import com.saveetha.LeaveManagement.dto.ApprovalRequestDTO;
import com.saveetha.LeaveManagement.dto.LeaveAlterationDto;
import com.saveetha.LeaveManagement.dto.LeaveApprovalStatusDTO;
import com.saveetha.LeaveManagement.dto.LeaveRequestSummaryDTO;
import com.saveetha.LeaveManagement.entity.LeaveApproval;
import com.saveetha.LeaveManagement.entity.LeaveRequest;
import com.saveetha.LeaveManagement.enums.ApprovalStatus;
import com.saveetha.LeaveManagement.service.LeaveApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import com.saveetha.LeaveManagement.repository.LeaveApprovalRepository;
@RestController
@RequestMapping("/api/leave-approval")
@RequiredArgsConstructor
public class LeaveApprovalController {


    private final LeaveApprovalService leaveApprovalService;
    private final LeaveApprovalRepository leaveApprovalRepository;


    // Call this after submitting the leave request
    @PostMapping("/initiate/{leaveRequestId}")
    public ResponseEntity<String> initiateApprovalFlow(@PathVariable Integer leaveRequestId) {
        leaveApprovalService.initiateApprovalFlow(leaveRequestId);
        return ResponseEntity.ok("Approval flow initiated successfully!");
    }

    // Call this when an approver approves or rejects
    @PatchMapping("/process/{approvalId}")
    public ResponseEntity<String> processApproval(
            @PathVariable Integer approvalId,
            @RequestBody ApprovalRequestDTO approvalRequestDTOdto
    ) {
        String loggedInEmpId = SecurityContextHolder.getContext().getAuthentication().getName();
System.out.println(loggedInEmpId);
        String result = leaveApprovalService.processApproval(
                approvalId,  // The ID of the approval record
                approvalRequestDTOdto.getStatus(),  // The approval status (APPROVED, REJECTED)
                approvalRequestDTOdto.getReason(),  // The reason for the approval or rejection
                loggedInEmpId  // The employee ID of the logged-in approver
        );
        return ResponseEntity.ok(result);
    }
    // Fetch pending requests for the logged-in approver
    @GetMapping("approver/pending-requests")
    public ResponseEntity<List<LeaveRequestSummaryDTO>> getPendingRequests() {
        String loggedInEmpId = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("Fetching pending requests for: " + loggedInEmpId);

        List<LeaveApproval> pendingApprovals = leaveApprovalRepository.findPendingApprovalsForApprover(loggedInEmpId);

        List<LeaveRequestSummaryDTO> summaryList = pendingApprovals.stream().map(approval -> {
            LeaveRequest request = approval.getLeaveRequest();

            LeaveRequestSummaryDTO dto = new LeaveRequestSummaryDTO();
            dto.setRequestId(request.getRequestId());
            dto.setApprovalId(approval.getApprovalId()); // 👈 Important for PATCH
            dto.setEmpId(request.getEmployee().getEmpId());
            dto.setEmpName(request.getEmployee().getEmpName());
            dto.setLeaveType(request.getLeaveType().getTypeName());
            dto.setStartDate(request.getStartDate().toString());
            dto.setEndDate(request.getEndDate().toString());
            dto.setReason(request.getReason());
            dto.setStatus(approval.getStatus().name()); // 👈 Current status of approval

            // Map LeaveAlteration -> LeaveAlterationDto
            List<LeaveAlterationDto> alterationDtos = request.getAlterations().stream().map(alt -> {
                LeaveAlterationDto altDto = new LeaveAlterationDto();
                altDto.setRequestId(request.getRequestId());
                altDto.setEmpId(request.getEmployee().getEmpId());
                altDto.setAlterationType(alt.getAlterationType());
                altDto.setMoodleActivityLink(alt.getMoodleActivityLink());

                if (alt.getReplacementEmployee() != null) {
                    altDto.setReplacementEmpId(alt.getReplacementEmployee().getEmpId());
                } else {
                    altDto.setReplacementEmpId(null);
                }

                altDto.setNotificationStatus(alt.getNotificationStatus());
                altDto.setClassPeriod(alt.getClassPeriod());
                altDto.setClassDate(alt.getClassDate());
                altDto.setSubjectName(alt.getSubjectName());
                altDto.setSubjectCode(alt.getSubjectCode());
                return altDto;
            }).collect(Collectors.toList());

            dto.setAlterations(alterationDtos);
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(summaryList);
    }
    @GetMapping("/status/{leaveRequestId}")
    public ResponseEntity<List<LeaveApprovalStatusDTO>> getApprovalStatusByLeaveRequestId(
            @PathVariable Integer leaveRequestId) {
        List<LeaveApprovalStatusDTO> approvalStatusList = leaveApprovalService.getApprovalStatusByLeaveRequestId(leaveRequestId);

        if (approvalStatusList.isEmpty()) {
            return ResponseEntity.noContent().build();  // 204 if no approval data found
        }

        return ResponseEntity.ok(approvalStatusList);
    }
}
