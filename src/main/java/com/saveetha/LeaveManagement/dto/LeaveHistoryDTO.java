package com.saveetha.LeaveManagement.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Getter
@Setter
public class LeaveHistoryDTO {
    private Integer requestId;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String reason;
    private LocalDateTime createdAt;

    // Constructor
    public LeaveHistoryDTO(Integer requestId, String leaveType, LocalDate startDate, LocalDate endDate,
                           String status, String reason, LocalDateTime createdAt) {
        this.requestId = requestId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    // Getters and setters...
}
