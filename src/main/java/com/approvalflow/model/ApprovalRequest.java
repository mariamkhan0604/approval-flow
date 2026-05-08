package com.approvalflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "requests")
public class ApprovalRequest {

    @Id
    private String requestId;

    private String description;
    private RequestStatus status;
    private String employeeId;
    private String managerId;
    private LocalDateTime createdAt;

    // ── NEW: tracks each reviewer's vote (username → "APPROVED" or "REJECTED")
    // Only populated after manager approves and request enters GROUP_APPROVAL_PENDING
    private Map<String, String> reviewerDecisions;
}