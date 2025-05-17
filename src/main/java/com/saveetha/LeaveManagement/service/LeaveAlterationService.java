package com.saveetha.LeaveManagement.service;

import com.saveetha.LeaveManagement.dto.LeaveAlterationDto;
import com.saveetha.LeaveManagement.entity.*;
import com.saveetha.LeaveManagement.enums.*;
import com.saveetha.LeaveManagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveAlterationService {

    @Autowired
    private LeaveAlterationRepository leaveAlterationRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private NotificationService notificationService;


    public String assignAlteration(LeaveAlterationDto dto) {
        LeaveAlteration alteration = new LeaveAlteration();

        // Set LeaveRequest
        LeaveRequest leaveRequest = leaveRequestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found"));
        alteration.setLeaveRequest(leaveRequest);

        // Set Employee who is applying
        Employee employee = employeeRepository.findById(dto.getEmpId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        alteration.setEmployee(employee);

        // Set Type and Details
        alteration.setAlterationType(dto.getAlterationType());

        // Handle Moodle Link
        if (dto.getAlterationType() == AlterationType.MOODLE_LINK) {
            alteration.setMoodleActivityLink(dto.getMoodleActivityLink());
            alteration.setNotificationStatus(null); // No approval needed
        }

        // Handle Staff Alteration
        if (dto.getAlterationType() == AlterationType.STAFF_ALTERATION) {
            Employee replacement = employeeRepository.findById(dto.getReplacementEmpId())
                    .orElseThrow(() -> new RuntimeException("Replacement Employee not found"));

            alteration.setReplacementEmployee(replacement);

            alteration.setNotificationStatus(NotificationStatus.PENDING); // Approval needed
            String message = "You have been assigned to handle class alteration on " + dto.getClassDate()
                    + " (Period: " + dto.getClassPeriod() + ", Subject: " + dto.getSubjectName() + ")";
            notificationService.sendNotification(dto.getReplacementEmpId(), message);
        }

        alteration.setClassDate(dto.getClassDate());
        alteration.setClassPeriod(dto.getClassPeriod());
        alteration.setSubjectCode(dto.getSubjectCode());
        alteration.setSubjectName(dto.getSubjectName());
        System.out.println("Notification sent to replacement faculty (Emp ID: " + dto.getReplacementEmpId() + ")");
        LeaveAlteration saved =leaveAlterationRepository.save(alteration);

        return "Alteration created successfully with ID: " + saved.getAlterationId();
    }
    public void approveAlteration(Integer alterationId) {
        LeaveAlteration alteration = leaveAlterationRepository.findById(alterationId)
                .orElseThrow(() -> new RuntimeException("Alteration not found"));

        // Only allow approval if it's PENDING
        if (alteration.getNotificationStatus() != NotificationStatus.PENDING) {
            throw new IllegalStateException("Alteration already processed.");
        }
        String loggedInEmpId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!alteration.getReplacementEmployee().getEmpId().equals(loggedInEmpId)) {
            throw new RuntimeException("Only the assigned replacement faculty can approve this alteration.");
        }

        alteration.setNotificationStatus(NotificationStatus.APPROVED);
        leaveAlterationRepository.save(alteration);

        System.out.println("Alteration approved by replacement faculty (Emp ID: " +
                alteration.getReplacementEmployee().getEmpId() + ")");
    }
    public void updateAlteration(Integer id, LeaveAlterationDto dto) {
        LeaveAlteration alteration = leaveAlterationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alteration not found"));

        alteration.setClassDate(dto.getClassDate());
        alteration.setClassPeriod(dto.getClassPeriod());
        alteration.setSubjectName(dto.getSubjectName());
        alteration.setSubjectCode(dto.getSubjectCode());

        if (dto.getAlterationType() == AlterationType.MOODLE_LINK) {
            alteration.setMoodleActivityLink(dto.getMoodleActivityLink());
            alteration.setNotificationStatus(null);
            alteration.setReplacementEmployee(null);
        }

        if (dto.getAlterationType() == AlterationType.STAFF_ALTERATION) {
            Employee replacement = employeeRepository.findById(dto.getReplacementEmpId())
                    .orElseThrow(() -> new RuntimeException("Replacement employee not found"));
            alteration.setReplacementEmployee(replacement);
            alteration.setNotificationStatus(NotificationStatus.PENDING);
        }

        alteration.setAlterationType(dto.getAlterationType());
        leaveAlterationRepository.save(alteration);
    }

    public List<LeaveAlteration> getAllAlterations() {
        return leaveAlterationRepository.findAll();
    }
    public LeaveAlteration getAlterationById(Integer id) {
        return leaveAlterationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alteration not found"));
    }
    public List<String> getNotificationStatuses(Integer requestId) {
        return leaveAlterationRepository.findNotificationStatusesByRequestId(requestId);
    }
}


