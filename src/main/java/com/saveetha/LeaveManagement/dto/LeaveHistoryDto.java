package com.saveetha.LeaveManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
public class LeaveHistoryDto {
    private Integer requestId;
    private String leaveTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String reason;
    private LocalDateTime createdAt;
    public LeaveHistoryDto(Integer requestId, String leaveTypeName,
                           LocalDate startDate, LocalDate endDate,
                           String status, String reason, LocalDateTime createdAt) {
        this.requestId = requestId;
        this.leaveTypeName = leaveTypeName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
    }

}
