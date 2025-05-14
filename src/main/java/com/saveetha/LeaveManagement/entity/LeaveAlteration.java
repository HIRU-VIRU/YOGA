package com.saveetha.LeaveManagement.entity;

import com.saveetha.LeaveManagement.enums.AlterationType;
import com.saveetha.LeaveManagement.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "LeaveAlteration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveAlteration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alteration_id", nullable = false, unique = true)
    private Integer alterationId; // Primary Key

    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    private LeaveRequest leaveRequest; // Foreign Key reference to LeaveRequest

    @ManyToOne
    @JoinColumn(name = "emp_id", nullable = false)
    private Employee employee; // Foreign Key reference to Employee (who requested alteration)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlterationType alterationType; // Enum for alteration type
    @Column(nullable = true)
    private String moodleActivityLink;

    @ManyToOne
    @JoinColumn(name = "replacement_emp_id")
    private Employee replacementEmployee; // Foreign Key reference to replacement employee

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private NotificationStatus notificationStatus = NotificationStatus.PENDING; // Default status: Pending
    @Column(nullable = true)
    private String classPeriod;
    @Column(nullable = true)
    private LocalDate classDate;
    @Column(nullable = true)
    private String subjectName;
    @Column(nullable = true)
    private String subjectCode;
    @Column(nullable = false)
    private Boolean active = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}