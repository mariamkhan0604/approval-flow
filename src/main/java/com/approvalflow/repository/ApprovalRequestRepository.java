package com.approvalflow.repository;

import com.approvalflow.model.ApprovalRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for the "requests" collection.
 *
 * MongoRepository<ApprovalRequest, String>:
 *   - ApprovalRequest → the document class
 *   - String          → the type of @Id (requestId)
 */
@Repository
public interface ApprovalRequestRepository extends MongoRepository<ApprovalRequest, String> {

    /**
     * Find all requests created by a specific employee.
     * Useful for letting an employee view their own request history.
     *
     * Spring Data translates this to:
     *   db.requests.find({ employeeId: <employeeId> })
     */
    List<ApprovalRequest> findByEmployeeId(String employeeId);
}
