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

    public Integer getRequestId() {
        return requestId;
    }

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public AlterationType getAlterationType() {
        return alterationType;
    }

    public void setAlterationType(AlterationType alterationType) {
        this.alterationType = alterationType;
    }

    public String getMoodleActivityLink() {
        return moodleActivityLink;
    }

    public void setMoodleActivityLink(String moodleActivityLink) {
        this.moodleActivityLink = moodleActivityLink;
    }

    public String getReplacementEmpId() {
        return replacementEmpId;
    }

    public void setReplacementEmpId(String replacementEmpId) {
        this.replacementEmpId = replacementEmpId;
    }

    public NotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(NotificationStatus notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public String getClassPeriod() {
        return classPeriod;
    }

    public void setClassPeriod(String classPeriod) {
        this.classPeriod = classPeriod;
    }

    public LocalDate getClassDate() {
        return classDate;
    }

    public void setClassDate(LocalDate classDate) {
        this.classDate = classDate;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }
}
