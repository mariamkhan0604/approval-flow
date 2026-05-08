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

import java.util.List;
import java.util.Map;

/**
 * REST controller for approval request endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ApprovalRequestController {

    private final ApprovalRequestService requestService;

    // ─────────────────────────────────────────────────────────────
    // POST /requests
    // EMPLOYEE creates a request
    // ─────────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<RequestResponseDTO> createRequest(
            @Valid @RequestBody CreateRequestDTO dto,
            Authentication authentication) {

        log.info("POST /requests called by '{}'", authentication.getName());

        RequestResponseDTO response =
                requestService.createRequest(dto, authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ─────────────────────────────────────────────────────────────
    // PUT /requests/{requestId}/status
    // MANAGER updates request status
    // ─────────────────────────────────────────────────────────────
    @PutMapping("/{requestId}/status")
    public ResponseEntity<RequestResponseDTO> updateStatus(
            @PathVariable String requestId,
            @Valid @RequestBody UpdateStatusDTO dto,
            Authentication authentication) {

        log.info(
                "PUT /requests/{}/status called by '{}'",
                requestId,
                authentication.getName()
        );

        RequestResponseDTO response =
                requestService.updateStatus(
                        requestId,
                        dto,
                        authentication
                );

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────
    // GET /requests
    // MANAGER sees all requests
    // ─────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<RequestResponseDTO>> getAllRequests() {

        log.info("GET /requests called");

        List<RequestResponseDTO> allRequests =
                requestService.getAllRequests();

        return ResponseEntity.ok(allRequests);
    }

    // ─────────────────────────────────────────────────────────────
    // GET /requests/my
    // EMPLOYEE sees only their requests
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/my")
    public ResponseEntity<List<RequestResponseDTO>> getMyRequests(
            Authentication authentication) {

        log.info(
                "GET /requests/my called by '{}'",
                authentication.getName()
        );

        List<RequestResponseDTO> myRequests =
                requestService.getMyRequests(
                        authentication.getName()
                );

        return ResponseEntity.ok(myRequests);
    }

    // ─────────────────────────────────────────────────────────────
    // GET /requests/stats
    // Dashboard statistics
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats(
            Authentication authentication) {

        log.info(
                "GET /requests/stats called by '{}'",
                authentication.getName()
        );

        Map<String, Long> stats =
                requestService.getStats(authentication);

        return ResponseEntity.ok(stats);
    }
}