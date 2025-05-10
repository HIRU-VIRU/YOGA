package com.saveetha.LeaveManagement.repository;

import com.saveetha.LeaveManagement.entity.LeaveRequest;
import com.saveetha.LeaveManagement.entity.Employee;
import com.saveetha.LeaveManagement.entity.LeaveType;
import com.saveetha.LeaveManagement.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {

    // Get all leave requests by employee
    List<LeaveRequest> findByEmployeeEmpId(String empId);

    // Optional: Get all active requests
    List<LeaveRequest> findByActiveTrue();


    @Query("SELECT COALESCE(SUM(DATEDIFF(lr.endDate, lr.startDate) + 1), 0) FROM LeaveRequest lr " +
            "WHERE lr.employee.empId = :empId " +
            "AND LOWER(lr.leaveType.typeName) = LOWER(:leaveTypeName) " +
            "AND lr.status IN ('PENDING', 'APPROVED')")
    int sumTotalDaysOfPendingAndApprovedML(
            @Param("empId") String empId,
            @Param("leaveTypeName") String leaveTypeName
    );

    @Query("SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.employee.empId = :empId " +
            "AND LOWER(lr.leaveType.typeName) = LOWER(:leaveType) " +
            "AND lr.status IN :statuses " +
            "AND lr.startDate <= :endDate " +
            "AND lr.endDate >= :startDate")
    List<LeaveRequest> findOverlappingLeaveRequests(
            @Param("empId") String empId,
            @Param("leaveType") String leaveType,
            @Param("statuses") List<LeaveStatus> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
            "lr.employee.empId = :empId AND " +
            "lr.leaveType.leaveTypeId = :leaveTypeId AND " +
            "lr.status IN :statuses AND " +
            "((lr.startDate BETWEEN :startDate AND :endDate) OR " +
            "(lr.endDate BETWEEN :startDate AND :endDate))")
    List<LeaveRequest> findOverlappingLeaveRequestsByTypeId(
            @Param("empId") String empId,
            @Param("leaveTypeId") Integer leaveTypeId,
            @Param("statuses") List<LeaveStatus> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
            "lr.employee.empId = :empId AND " +
            "lr.leaveType.leaveTypeId = :leaveTypeId AND " +
            "lr.status IN :statuses AND " +
            "lr.earnedDate = :earnedDate")
    List<LeaveRequest> findCompOffRequestsByEarnedDate(
            @Param("empId") String empId,
            @Param("leaveTypeId") Integer leaveTypeId,
            @Param("statuses") List<LeaveStatus> statuses,
            @Param("earnedDate") LocalDate earnedDate);

    @Query("SELECT COUNT(l) > 0 FROM LeaveRequest l WHERE l.employee.empId = :empId AND l.leaveType.leaveTypeId = :leaveTypeId AND l.status IN :statuses AND l.startDate BETWEEN :startDate AND :endDate")
    boolean existsCLUsedInMonth(@Param("empId") String empId,
                                @Param("leaveTypeId") Integer leaveTypeId,
                                @Param("statuses") List<LeaveStatus> statuses,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);


    @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.employee.empId = :empId AND l.leaveType.leaveTypeId = :leaveTypeId AND l.status IN :statuses AND l.startDate BETWEEN :startDate AND :endDate")
    int countCLsUsed(@Param("empId") String empId,
                     @Param("leaveTypeId") Integer leaveTypeId,
                     @Param("statuses") List<LeaveStatus> statuses,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate);

    @Query(value = """
        SELECT 
            lr.request_id,
            lt.type_name AS leave_type,
            lr.start_date,
            lr.end_date,
            lr.status,
            lr.reason,
            lr.created_at
        FROM 
            leave_request lr
        JOIN 
            leave_type lt ON lr.leave_type_id = lt.leave_type_id
        WHERE 
            lr.emp_id = :empId AND lr.active = true
        ORDER BY 
            lr.created_at DESC
        """, nativeQuery = true)
    List<Object[]> getLeaveHistoryForEmployee(@Param("empId") String empId);
}
