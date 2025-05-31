package com.saveetha.LeaveManagement.dto;

import com.saveetha.LeaveManagement.enums.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaveAlterationNotificationDto {
    private Integer alterationId;
    private String replacementEmpId;
    private String classDate;      // String for easy JSON format
    private String classPeriod;
    private String subjectCode;
    private String subjectName;
    private NotificationStatus notificationStatus;
}
