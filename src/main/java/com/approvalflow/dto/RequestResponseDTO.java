package com.approvalflow.dto;

import com.approvalflow.model.RequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO returned to the client after creating or updating a request.
 *
 * Using a dedicated response DTO (rather than returning the raw model) gives us:
 *   - Control over exactly what fields are exposed in the API response.
 *   - Freedom to rename or reshape fields without touching the database model.
 *   - A clear contract between the backend and any frontend/consumer.
 */
@Data
@Builder
public class RequestResponseDTO {

    private String requestId;
    private String description;
    private RequestStatus status;
    private String employeeId;
    private String employeeName;  
    private String managerId;       // null if not yet acted on
    private LocalDateTime createdAt;
}
