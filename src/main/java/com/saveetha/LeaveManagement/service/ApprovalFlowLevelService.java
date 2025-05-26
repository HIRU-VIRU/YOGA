package com.saveetha.LeaveManagement.service;

import com.saveetha.LeaveManagement.dto.ApprovalFlowLevelDTO;
import com.saveetha.LeaveManagement.entity.ApprovalFlow;
import com.saveetha.LeaveManagement.entity.ApprovalFlowLevel;
import com.saveetha.LeaveManagement.entity.Employee;
import com.saveetha.LeaveManagement.repository.ApprovalFlowLevelRepository;
import com.saveetha.LeaveManagement.repository.ApprovalFlowRepository;
import com.saveetha.LeaveManagement.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApprovalFlowLevelService {

    @Autowired
    private ApprovalFlowLevelRepository approvalFlowLevelRepository;

    // Get all approval flow levels
    public List<ApprovalFlowLevel> getAllApprovalFlowLevels() {
        return approvalFlowLevelRepository.findAll();
    }

    // Get approval levels by Approval Flow ID
    public List<ApprovalFlowLevel> getApprovalFlowLevelsByFlowId(Integer approvalFlowId) {
        return approvalFlowLevelRepository.findByApprovalFlowApprovalFlowId(approvalFlowId);
    }

    // Get a specific approval level by ID
    public Optional<ApprovalFlowLevel> getApprovalFlowLevelById(Integer id) {
        return approvalFlowLevelRepository.findById(id);
    }

    // Save a new approval flow level
    public ApprovalFlowLevel saveApprovalFlowLevel(ApprovalFlowLevel approvalFlowLevel) {
        return approvalFlowLevelRepository.save(approvalFlowLevel);
    }


    // Delete an approval flow level
    public void deleteApprovalFlowLevel(Integer id) {
        approvalFlowLevelRepository.deleteById(id);
    }

    @Autowired
    private ApprovalFlowRepository approvalFlowRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public ApprovalFlowLevel updateApprovalFlowLevel(Integer id, ApprovalFlowLevelDTO dto) {
        ApprovalFlowLevel existing = approvalFlowLevelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Approval Flow Level not found"));

        ApprovalFlow approvalFlow = approvalFlowRepository.findById(dto.getApprovalFlowId())
                .orElseThrow(() -> new RuntimeException("Approval Flow not found"));

        Employee approver = employeeRepository.findById(dto.getApproverId())
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        existing.setApprovalFlow(approvalFlow);
        existing.setApprover(approver);
        existing.setSequence(dto.getSequence());
        existing.setActive(dto.isActive());

        return approvalFlowLevelRepository.save(existing);
    }


    public ApprovalFlowLevel setActiveStatus(Integer id, boolean isActive) {
        ApprovalFlowLevel existing = approvalFlowLevelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Approval Flow Level not found"));
        existing.setActive(isActive);
        return approvalFlowLevelRepository.save(existing);
    }

}

