package com.saveetha.LeaveManagement.repository;

import com.saveetha.LeaveManagement.entity.LeaveApproval;
import com.saveetha.LeaveManagement.entity.LeaveRequest;
import com.saveetha.LeaveManagement.entity.Employee;
import com.saveetha.LeaveManagement.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveApprovalRepository extends JpaRepository<LeaveApproval, Integer> {

    List<LeaveApproval> findByLeaveRequest_RequestIdOrderByApprovalFlowLevel_SequenceAsc(Integer leaveRequestId);

    @Modifying
    @Query("DELETE FROM LeaveApproval la WHERE la.leaveRequest = :leaveRequest")
    void deleteByLeaveRequest(@Param("leaveRequest") LeaveRequest leaveRequest);

    List<LeaveApproval> findByLeaveRequest_RequestId(Integer requestId);

    boolean existsByLeaveRequestAndApproverAndStatus(LeaveRequest leaveRequest, Employee approver, ApprovalStatus status);
    @Query("SELECT a FROM LeaveApproval a WHERE a.approver.empId = :empId AND a.status = 'PENDING'")
    List<LeaveApproval> findPendingApprovalsForApprover(@Param("empId") String empId);
    List<LeaveApproval> findByLeaveRequestRequestId(Integer leaveRequestId);

}
