package com.saveetha.LeaveManagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignApprovalFlowDTO {

    @NotBlank
    private String empId;

    private Integer approvalFlowId;
}
