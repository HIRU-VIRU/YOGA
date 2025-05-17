package com.saveetha.LeaveManagement.controller;

import com.saveetha.LeaveManagement.dto.LeaveAlterationDto;
import com.saveetha.LeaveManagement.service.LeaveAlterationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-alteration")
public class LeaveAlterationController {

    @Autowired
    private LeaveAlterationService leaveAlterationService;

    // Step 1: Assign alteration (either moodle link or staff)
    @PostMapping("/assign")
    public ResponseEntity<String> assignAlteration(@RequestBody LeaveAlterationDto leaveAlterationDto) {
        try {
            String message = leaveAlterationService.assignAlteration(leaveAlterationDto);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error assigning alteration: " + e.getMessage());
        }
    }

    // Step 2: Approve staff replacement by the replacement employee
    @PatchMapping("/approve/{id}")
    public ResponseEntity<String> approveAlteration(@PathVariable("id") Integer id) {
       leaveAlterationService.approveAlteration(id);
        return ResponseEntity.ok("Alteration approved successfully!");
    }
    @GetMapping("/notification-status/{requestId}")
    public ResponseEntity<List<String>> getNotificationStatus(@PathVariable Integer requestId) {
        List<String> statuses = leaveAlterationService.getNotificationStatuses(requestId);
        return ResponseEntity.ok(statuses);
    }

}
