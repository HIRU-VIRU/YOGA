package com.saveetha.LeaveManagement.dto;

import java.util.List;

public class LeaveRequestSummaryDTO {
    private Integer requestId;
    private String empId;
    private String empName;
    private String leaveType;
    private String startDate;
    private String endDate;
    private String reason;
    private String status;

    private List<LeaveAlterationDto> alterations;

    public List<LeaveAlterationDto> getAlterations() {
        return alterations;
    }

    public void setAlterations(List<LeaveAlterationDto> alterations) {
        this.alterations = alterations;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    // getters and setters
}