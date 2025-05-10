package com.saveetha.LeaveManagement.controller;

import com.saveetha.LeaveManagement.dto.LeaveHistoryDTO;
import com.saveetha.LeaveManagement.dto.LeaveRequestDTO;
import com.saveetha.LeaveManagement.entity.LeaveRequest;
import com.saveetha.LeaveManagement.security.JwtUtil;
import com.saveetha.LeaveManagement.service.LeaveRequestService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-request")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @PostMapping("/create-draft")
    public ResponseEntity<?> createDraft(@RequestBody LeaveRequestDTO leaveRequestdto) {
        LeaveRequest saved = leaveRequestService.createDraftLeaveRequest(leaveRequestdto);
        return ResponseEntity.ok("Draft Leave Request created with ID: " + saved.getRequestId());
    }
    @PostMapping("/submit/{id}")
    public ResponseEntity<String> submitLeaveRequest(@PathVariable("id") Integer requestId) {
        String response = leaveRequestService.submitLeaveRequest(requestId);
        return ResponseEntity.ok(response);
    }
    // PATCH endpoint to withdraw a leave request
    @PatchMapping("/withdraw/{requestId}")
    public ResponseEntity<String> withdrawLeaveRequest(@PathVariable Integer requestId) {
        String response = leaveRequestService.withdrawLeaveRequest(requestId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/leave-history")
    public ResponseEntity<List<LeaveHistoryDTO>> getLeaveHistory() {
        return ResponseEntity.ok(leaveRequestService.getLeaveHistoryForCurrentUser());
    }




}
