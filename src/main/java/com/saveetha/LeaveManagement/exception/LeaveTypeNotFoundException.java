package com.saveetha.LeaveManagement.exception;

public class LeaveTypeNotFoundException extends RuntimeException {
    public LeaveTypeNotFoundException(String message) {
        super(message);
    }
}
