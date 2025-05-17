package com.saveetha.LeaveManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApprovalStatusDTO {
    private String empId;
    private String empName;
    private String status;
    private String reason;
    private LocalDateTime actionTimestamp;
    private Integer approvalFlowLevel;



}
