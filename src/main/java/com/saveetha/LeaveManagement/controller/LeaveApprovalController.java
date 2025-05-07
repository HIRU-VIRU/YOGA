package com.saveetha.LeaveManagement.controller;

import com.saveetha.LeaveManagement.dto.ApprovalRequestDTO;
import com.saveetha.LeaveManagement.entity.LeaveRequest;
import com.saveetha.LeaveManagement.repository.LeaveApprovalRepository;
import com.saveetha.LeaveManagement.service.LeaveApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<LeaveRequest>> getPendingRequests() {
        // Fetch the employee ID from the SecurityContext (JWT token)
        String loggedInEmpId = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("Fetching pending requests for: " + loggedInEmpId);

        List<LeaveRequest> pendingRequests = leaveApprovalRepository.findPendingRequestsForApprover(loggedInEmpId);
        return ResponseEntity.ok(pendingRequests);
    }
}
