package com.saveetha.LeaveManagement.dto;

import com.saveetha.LeaveManagement.enums.AlterationType;
import com.saveetha.LeaveManagement.enums.NotificationStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveAlterationDto {

    private Integer requestId;
    private String empId;

    private AlterationType alterationType;
    private String moodleActivityLink;

    private String replacementEmpId;
    private NotificationStatus notificationStatus; // NotificationStatus
    private String classPeriod;
    private LocalDate classDate;
    private String subjectName;
    private String subjectCode;
}
