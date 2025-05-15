package com.saveetha.LeaveManagement.controller;

import com.saveetha.LeaveManagement.dto.LeaveRequestDTO;
import com.saveetha.LeaveManagement.entity.LeaveRequest;
import com.saveetha.LeaveManagement.service.CloudinaryService;
import com.saveetha.LeaveManagement.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/leave-request")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    @Autowired
    private CloudinaryService cloudinaryService;
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
    @PostMapping("/upload-medical-certificate")
    public ResponseEntity<String> uploadMedicalCertificate(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = cloudinaryService.uploadDocument(file); // Uploads to Cloudinary
            return ResponseEntity.ok(fileUrl); // Return just the URL to frontend
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed.");
        }
    }

}
