package com.approvalflow.dto;

import com.approvalflow.model.RequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class RequestResponseDTO {

    private String requestId;
    private String description;
    private RequestStatus status;
    private String employeeId;
    private String employeeName;
    private String managerId;
    private LocalDateTime createdAt;
    private Map<String, String> reviewerDecisions;
}