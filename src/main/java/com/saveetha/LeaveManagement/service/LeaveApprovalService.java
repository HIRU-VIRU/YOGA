package com.saveetha.LeaveManagement.service;

import com.saveetha.LeaveManagement.dto.LeaveApprovalStatusDTO;
import com.saveetha.LeaveManagement.entity.*;
import com.saveetha.LeaveManagement.enums.ApprovalStatus;
import com.saveetha.LeaveManagement.enums.LeaveStatus;
import com.saveetha.LeaveManagement.repository.ApprovalFlowLevelRepository;
import com.saveetha.LeaveManagement.repository.EmployeeRepository;
import com.saveetha.LeaveManagement.repository.LeaveApprovalRepository;
import com.saveetha.LeaveManagement.repository.LeaveRequestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveApprovalService {

    private final LeaveApprovalRepository leaveApprovalRepository;
    private final EmployeeRepository employeeRepository;
    private final ApprovalFlowLevelRepository approvalFlowLevelRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeLeaveBalanceService LeaveBalanceService;

    public List<LeaveApproval> initiateApprovalFlow(Integer leaveRequestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found"));

        Employee employee = leaveRequest.getEmployee();
        Integer approvalFlowId = employee.getApprovalFlow().getApprovalFlowId();

        // 👇 Get the sequence of approvers for this flow
        List<ApprovalFlowLevel> flowLevels = approvalFlowLevelRepository
                .findByApprovalFlow_ApprovalFlowIdOrderBySequenceAsc(approvalFlowId);
        System.out.println("Initiating approval flow for Leave Request ID: " + leaveRequestId);
        System.out.println("Approval Flow ID: " + approvalFlowId);
        System.out.println("Approvers in flow:");
        List<LeaveApproval> savedApprovals = new ArrayList<>();

        for (ApprovalFlowLevel level : flowLevels) {
            System.out.println(" - Approver: " + level.getApprover().getEmpId() +
                    ", Sequence: " + level.getSequence());
            LeaveApproval approval = new LeaveApproval();
            approval.setLeaveRequest(leaveRequest);
            approval.setApprovalFlowLevel(level);   // Which level of approval
            approval.setApprover(level.getApprover());  // Who is the approver
            approval.setStatus(ApprovalStatus.PENDING); // Initially pending
            LeaveApproval saved= leaveApprovalRepository.save(approval); //  This creates one row per level
            savedApprovals.add(saved);
        }
        return savedApprovals;
    }


    @Transactional
    public String processApproval(Integer approvalId, ApprovalStatus status, String reason, String loggedInEmpId) {
        // Retrieve the approval record
        LeaveApproval approval = leaveApprovalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval not found"));

        // Check if the approval is already processed
        if (approval.getStatus() != ApprovalStatus.PENDING)
            throw new RuntimeException("Already processed");

        // Get the current approval flow level
        ApprovalFlowLevel currentLevel = approval.getApprovalFlowLevel();
        int currentSequence = currentLevel.getSequence();

        // Retrieve the expected approver for this level
        Employee expectedApprover = currentLevel.getApprover();

        // Check if the logged-in user is the expected approver
        if (!loggedInEmpId.equals(expectedApprover.getEmpId())) {
            throw new RuntimeException("You are not the correct approver for this level.");
        }

        // Get all the leave approvals related to the leave request
        LeaveRequest request = approval.getLeaveRequest();
        List<LeaveApproval> approvals = leaveApprovalRepository.findByLeaveRequest_RequestId(request.getRequestId());

        // Check if the previous approval sequence is approved
        boolean isPreviousApproved = approvals.stream()
                .filter(a -> a.getApprovalFlowLevel().getSequence() == currentSequence - 1)
                .anyMatch(a -> a.getStatus() == ApprovalStatus.APPROVED);

        // If the current sequence is not the first sequence and the previous sequence is not approved, block the approval
        if (currentSequence > 1 && !isPreviousApproved) {
            throw new RuntimeException("Previous sequence approval is pending. You cannot approve this request yet.");
        }

        // Update the approval status and reason
        approval.setStatus(status);
        approval.setReason(reason);
        approval.setUpdatedAt(LocalDateTime.now());
        leaveApprovalRepository.save(approval);

        // If rejected, update the leave request status to REJECTED
        if (status == ApprovalStatus.REJECTED) {
            request.setStatus(LeaveStatus.REJECTED);
            leaveRequestRepository.save(request);
            return "Leave request rejected by " + approval.getApprover().getEmpId();
        }

        // If approved, check if current approver is the final approver
        boolean allApproved = approvals.stream()
                .allMatch(a -> a.getStatus() == ApprovalStatus.APPROVED);

        // If all approvals are completed, check for the final approver
        if (allApproved) {
            int maxSequence = approvals.stream()
                    .mapToInt(a -> a.getApprovalFlowLevel().getSequence())
                    .max().orElse(0);

            LeaveApproval finalApproval = approvals.stream()
                    .filter(a -> a.getApprovalFlowLevel().getSequence() == maxSequence)
                    .findFirst()
                    .orElse(null);

            if (finalApproval != null) {
                Employee expectedFinalApprover = request.getEmployee().getApprovalFlow().getFinalApprover();

                // Check if the final approver is the correct one
                if (finalApproval.getApprover().getEmpId().equals(expectedFinalApprover.getEmpId())) {
                    request.setStatus(LeaveStatus.APPROVED);
                    leaveRequestRepository.save(request);
                    LeaveBalanceService.deductLeaveBalanceOnApproval(request);
                    return "Leave request approved by final approver";
                }
            }
        }

        // If not yet fully approved, return a message about the current approval level
        return "Leave approved at current level and pending further approval";
    }

    public List<LeaveApprovalStatusDTO> getApprovalStatusByLeaveRequestId(Integer leaveRequestId) {
        List<LeaveApproval> approvals = leaveApprovalRepository.findByLeaveRequest_RequestIdOrderByApprovalFlowLevel_SequenceAsc(leaveRequestId);

        // Find the next approver (first one with status PENDING)
        boolean nextApproverFound = false;

        List<LeaveApprovalStatusDTO> result = new ArrayList<>();
        for (LeaveApproval approval : approvals) {
            String status = approval.getStatus().toString();
            if (!nextApproverFound && approval.getStatus() == ApprovalStatus.PENDING) {
                // Mark this one as PENDING (next approver)
                nextApproverFound = true;
            } else if (approval.getStatus() == ApprovalStatus.PENDING) {
                // Others after next approver remain as WAITING
                status = "WAITING";
            }

            result.add(new LeaveApprovalStatusDTO(
                    approval.getApprover().getEmpId(),
                    approval.getApprover().getEmpName(),
                    status,
                    approval.getReason(),
                    approval.getUpdatedAt(),
                    approval.getApprovalFlowLevel().getSequence()
            ));
        }

        return result;
    }


    // New method: Update approval
    @Transactional
    public String updateApproval(Integer approvalId, ApprovalStatus status, String reason) {
        LeaveApproval approval = leaveApprovalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval not found"));

        approval.setStatus(status);
        approval.setReason(reason);
        approval.setUpdatedAt(LocalDateTime.now());
        leaveApprovalRepository.save(approval);

        return "Approval updated successfully";
    }

    // New method: Delete approval
    @Transactional
    public void deleteApproval(Integer approvalId) {
        LeaveApproval approval = leaveApprovalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval not found"));

        leaveApprovalRepository.delete(approval);
    }

    // New method: Get approval by ID
    public LeaveApproval getApprovalById(Integer approvalId) {
        return leaveApprovalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval not found"));
    }

    // New method: Get all approvals
    public List<LeaveApproval> getAllApprovals() {
        return leaveApprovalRepository.findAll();
    }


}

