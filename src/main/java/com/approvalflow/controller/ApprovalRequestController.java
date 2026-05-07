package com.approvalflow.controller;

import com.approvalflow.dto.CreateRequestDTO;
import com.approvalflow.dto.RequestResponseDTO;
import com.approvalflow.dto.UpdateStatusDTO;
import com.approvalflow.service.ApprovalRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for approval request endpoints.
 *
 * Responsibilities (ONLY these — business logic stays in the service):
 *   - Map HTTP methods/paths to service calls.
 *   - Extract data from the request (body, path variables, auth principal).
 *   - Return the appropriate HTTP status code.
 *
 * @RestController = @Controller + @ResponseBody
 *   → every method return value is serialised to JSON automatically.
 * @RequestMapping("/requests") → all endpoints start with /requests.
 * @RequiredArgsConstructor → constructor injection for ApprovalRequestService.
 */
@Slf4j
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ApprovalRequestController {

    private final ApprovalRequestService requestService;

    // ─────────────────────────────────────────────────────────────────────
    //  POST /requests
    //  Accessible by: EMPLOYEE
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Creates a new approval request.
     *
     * @param dto            Request body — only `description` is required.
     *                       @Valid triggers the @NotBlank constraint.
     * @param authentication Injected by Spring — holds the logged-in user's info.
     * @return               HTTP 201 Created + the created request as JSON.
     *
     * Example request:
     *   POST /requests
     *   Authorization: Basic ZW1wbG95ZWU6cGFzc3dvcmQ=   (employee:password)
     *   Content-Type: application/json
     *
     *   { "description": "Request for 3 days leave" }
     *
     * Example response (201):
     *   {
     *     "requestId": "6641abc123...",
     *     "description": "Request for 3 days leave",
     *     "status": "PENDING",
     *     "employeeId": "employee",
     *     "managerId": null,
     *     "createdAt": "2024-05-15T10:30:00"
     *   }
     */
    @PostMapping
    public ResponseEntity<RequestResponseDTO> createRequest(
            @Valid @RequestBody CreateRequestDTO dto,
            Authentication authentication) {

        log.info("POST /requests called by '{}'", authentication.getName());
        RequestResponseDTO response = requestService.createRequest(dto, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  PUT /requests/{requestId}/status
    //  Accessible by: MANAGER
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Updates the status of an existing request to APPROVED or REJECTED.
     *
     * @param requestId      Path variable — the MongoDB document id.
     * @param dto            Request body — contains the new status.
     * @param authentication Injected by Spring — holds the logged-in user's info.
     * @return               HTTP 200 OK + the updated request as JSON.
     *
     * Example request:
     *   PUT /requests/6641abc123.../status
     *   Authorization: Basic bWFuYWdlcjpwYXNzd29yZA==   (manager:password)
     *   Content-Type: application/json
     *
     *   { "status": "APPROVED" }
     *
     * Example response (200):
     *   {
     *     "requestId": "6641abc123...",
     *     "description": "Request for 3 days leave",
     *     "status": "APPROVED",
     *     "employeeId": "employee",
     *     "managerId": "manager",
     *     "createdAt": "2024-05-15T10:30:00"
     *   }
     */
    @PutMapping("/{requestId}/status")
    public ResponseEntity<RequestResponseDTO> updateStatus(
            @PathVariable String requestId,
            @Valid @RequestBody UpdateStatusDTO dto,
            Authentication authentication) {

        log.info("PUT /requests/{}/status called by '{}'", requestId, authentication.getName());
        RequestResponseDTO response = requestService.updateStatus(requestId, dto, authentication);
        return ResponseEntity.ok(response);
    }
}
