package com.approvalflow.controller;

import com.approvalflow.dto.GroupReviewDTO;
import com.approvalflow.dto.RequestResponseDTO;
import com.approvalflow.service.GroupApprovalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class GroupReviewController {

    private final GroupApprovalService groupApprovalService;

    /**
     * GET /reviews/pending
     * Returns all GROUP_APPROVAL_PENDING requests the current reviewer
     * has NOT yet voted on.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<RequestResponseDTO>> getPendingReviews(
            Authentication authentication) {

        log.info("GET /reviews/pending called by '{}'", authentication.getName());
        return ResponseEntity.ok(
            groupApprovalService.getPendingReviewsForMe(authentication));
    }

    /**
     * GET /reviews/all
     * Returns ALL requests currently in GROUP_APPROVAL_PENDING,
     * including ones the reviewer has already voted on (for dashboard view).
     */
    @GetMapping("/all")
    public ResponseEntity<List<RequestResponseDTO>> getAllGroupPending(
            Authentication authentication) {

        log.info("GET /reviews/all called by '{}'", authentication.getName());
        return ResponseEntity.ok(
            groupApprovalService.getAllGroupPendingRequests());
    }

    /**
     * POST /reviews/{requestId}/decision
     * Reviewer submits APPROVED or REJECTED.
     */
    @PostMapping("/{requestId}/decision")
    public ResponseEntity<RequestResponseDTO> submitDecision(
            @PathVariable String requestId,
            @Valid @RequestBody GroupReviewDTO dto,
            Authentication authentication) {

        log.info("POST /reviews/{}/decision called by '{}'",
            requestId, authentication.getName());

        return ResponseEntity.ok(
            groupApprovalService.submitReview(requestId, dto, authentication));
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<RequestResponseDTO>> getReviewHistory(
            Authentication authentication) {

        log.info("GET /reviews/history called by '{}'", authentication.getName());
    return ResponseEntity.ok(
        groupApprovalService.getGroupReviewHistory());
}
}