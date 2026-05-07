package com.approvalflow.service;

import com.approvalflow.dto.CreateRequestDTO;
import com.approvalflow.dto.RequestResponseDTO;
import com.approvalflow.dto.UpdateStatusDTO;
import com.approvalflow.exception.BadRequestException;
import com.approvalflow.exception.ResourceNotFoundException;
import com.approvalflow.model.ApprovalRequest;
import com.approvalflow.model.RequestStatus;
import com.approvalflow.repository.ApprovalRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Business logic for the Approval Flow system.
 *
 * The service layer sits between the controller (HTTP) and repository (DB).
 * It is responsible for:
 *   - Enforcing business rules (e.g. manager cannot re-PENDING a request)
 *   - Mapping between models and DTOs
 *   - Orchestrating calls to repositories
 *
 * @Slf4j            → Lombok generates a `log` field for us (uses SLF4J).
 * @Service          → Spring registers this class as a singleton bean.
 * @RequiredArgsConstructor → Lombok generates a constructor that injects
 *                    all `final` fields (ApprovalRequestRepository here).
 *                    This is the recommended way to do constructor injection.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalRequestService {

    private final ApprovalRequestRepository requestRepository;

    // ─────────────────────────────────────────────────────────────────────
    //  CREATE REQUEST  (called by EMPLOYEE)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Creates a new approval request with status = PENDING.
     *
     * @param dto            the incoming request body (description only)
     * @param authentication Spring Security's current-user object
     * @return               the saved request as a DTO
     */
    public RequestResponseDTO createRequest(CreateRequestDTO dto, Authentication authentication) {

        // The authenticated username acts as the employeeId.
        // In a production system you'd look up the userId from the DB.
        String employeeId = authentication.getName();   // "employee"

        log.info("Employee '{}' is creating a new request: {}", employeeId, dto.getDescription());

        // Build the MongoDB document using the builder pattern
        ApprovalRequest request = ApprovalRequest.builder()
                .description(dto.getDescription())
                .status(RequestStatus.PENDING)          // always starts as PENDING
                .employeeId(employeeId)
                .managerId(null)                        // no manager has acted yet
                .createdAt(LocalDateTime.now())
                .build();
        // Note: requestId is NOT set here; MongoDB generates it automatically.

        ApprovalRequest saved = requestRepository.save(request);
        log.info("Request saved with id: {}", saved.getRequestId());

        return toResponseDTO(saved);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  UPDATE STATUS  (called by MANAGER)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Updates an existing request to APPROVED or REJECTED.
     *
     * Business rules enforced here:
     *   1. Request must exist.
     *   2. New status must be APPROVED or REJECTED (not PENDING again).
     *
     * @param requestId      the MongoDB _id of the request
     * @param dto            contains the new status
     * @param authentication Spring Security's current-user object
     * @return               the updated request as a DTO
     */
    public RequestResponseDTO updateStatus(String requestId,
                                           UpdateStatusDTO dto,
                                           Authentication authentication) {

        String managerId = authentication.getName();   // "manager"

        log.info("Manager '{}' is updating request '{}' to '{}'",
                managerId, requestId, dto.getStatus());

        // ── Rule 1: request must exist ──
        ApprovalRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Request not found with id: " + requestId));

        // ── Rule 2: status must be APPROVED or REJECTED ──
        if (dto.getStatus() == RequestStatus.PENDING) {
            throw new BadRequestException(
                    "Status cannot be set back to PENDING. Use APPROVED or REJECTED.");
        }

        // Apply the update
        request.setStatus(dto.getStatus());
        request.setManagerId(managerId);

        ApprovalRequest updated = requestRepository.save(request);
        log.info("Request '{}' updated to '{}'", requestId, updated.getStatus());

        return toResponseDTO(updated);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  PRIVATE HELPER
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Converts an ApprovalRequest domain model into a RequestResponseDTO.
     *
     * Keeping this mapping in the service (not the controller or model) is
     * a clean-architecture best practice — each layer has a single concern.
     */
    private RequestResponseDTO toResponseDTO(ApprovalRequest request) {
        return RequestResponseDTO.builder()
                .requestId(request.getRequestId())
                .description(request.getDescription())
                .status(request.getStatus())
                .employeeId(request.getEmployeeId())
                .managerId(request.getManagerId())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
