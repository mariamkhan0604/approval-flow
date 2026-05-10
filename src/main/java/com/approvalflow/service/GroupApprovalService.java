package com.approvalflow.service;

import com.approvalflow.dto.GroupReviewDTO;
import com.approvalflow.dto.RequestResponseDTO;
import com.approvalflow.exception.BadRequestException;
import com.approvalflow.exception.ResourceNotFoundException;
import com.approvalflow.model.ApprovalRequest;
import com.approvalflow.model.RequestStatus;
import com.approvalflow.model.User;
import com.approvalflow.repository.ApprovalRequestRepository;
import com.approvalflow.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupApprovalService {

    private final ApprovalRequestRepository requestRepository;
    private final UserRepository userRepository;

    /**
     * Called by ApprovalRequestService when a manager approves a request.
     * Moves the request to GROUP_APPROVAL_PENDING with an empty decision map.
     * No reviewers are pre-assigned — all REVIEWERs can act.
     */
    public void moveToGroupApproval(ApprovalRequest request) {
        request.setStatus(RequestStatus.GROUP_APPROVAL_PENDING);
        request.setReviewerDecisions(new HashMap<>());
        requestRepository.save(request);
        log.info("Request '{}' moved to GROUP_APPROVAL_PENDING. Open to all reviewers.",
            request.getRequestId());
    }

    /**
     * A REVIEWER submits their decision on a request.
     *
     * Rules:
     *  - ANY ONE approves  → immediately APPROVED, done
     *  - Someone rejects   → record it, keep waiting
     *  - ALL reviewers reject → REJECTED
     */
    public RequestResponseDTO submitReview(
            String requestId,
            GroupReviewDTO dto,
            Authentication authentication) {

        String reviewerUsername = authentication.getName();

        // ── Fetch the request ────────────────────────────────────
        ApprovalRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Request not found with id: " + requestId));

        // ── Guard: must be awaiting group approval ───────────────
        if (request.getStatus() != RequestStatus.GROUP_APPROVAL_PENDING) {
            throw new BadRequestException(
                "Request '" + requestId + "' is not awaiting group approval. " +
                "Current status: " + request.getStatus());
        }

        // ── Guard: reviewer must not have already voted ──────────
        if (request.getReviewerDecisions().containsKey(reviewerUsername)) {
            throw new BadRequestException(
                "You have already submitted your decision for request '" + requestId + "'.");
        }

        // ── Validate decision value ──────────────────────────────
        String decision = dto.getDecision().toUpperCase();
        if (!decision.equals("APPROVED") && !decision.equals("REJECTED")) {
            throw new BadRequestException("Decision must be APPROVED or REJECTED.");
        }

        // ── Record this reviewer's vote ──────────────────────────
        request.getReviewerDecisions().put(reviewerUsername, decision);
        log.info("Reviewer '{}' voted '{}' on request '{}'",
            reviewerUsername, decision, requestId);

        // ── Apply the group approval rules ───────────────────────
        if (decision.equals("APPROVED")) {

            // Rule 1: Any one approves → immediately APPROVED
            request.setStatus(RequestStatus.APPROVED);
            log.info("Request '{}' APPROVED by reviewer '{}'.", requestId, reviewerUsername);

        } else {

            // Rule 2 & 3: This reviewer rejected — check if ALL reviewers have now rejected

            // Count total REVIEWERs in the system
            long totalReviewers = userRepository.findAll()
                    .stream()
                    .filter(u -> "REVIEWER".equals(u.getRole().name()))
                    .count();

            long totalRejections = request.getReviewerDecisions()
                    .values()
                    .stream()
                    .filter(v -> v.equals("REJECTED"))
                    .count();

            if (totalRejections >= totalReviewers) {
                // Rule 3: Every single reviewer rejected → final REJECTED
                request.setStatus(RequestStatus.REJECTED);
                log.info("Request '{}' REJECTED — all {} reviewers rejected.",
                    requestId, totalReviewers);
            } else {
                // Rule 2: Still waiting — some reviewers haven't voted yet
                log.info("Request '{}' still GROUP_APPROVAL_PENDING — {}/{} rejections so far.",
                    requestId, totalRejections, totalReviewers);
            }
        }

        ApprovalRequest saved = requestRepository.save(request);
        return toResponseDTO(saved);
    }

    /**
     * Returns all GROUP_APPROVAL_PENDING requests that the
     * current reviewer has NOT yet voted on.
     */
    public List<RequestResponseDTO> getPendingReviewsForMe(Authentication authentication) {

        String reviewerUsername = authentication.getName();

        return requestRepository.findAll()
                .stream()
                .filter(r ->
                    r.getStatus() == RequestStatus.GROUP_APPROVAL_PENDING &&
                    (r.getReviewerDecisions() == null ||
                     !r.getReviewerDecisions().containsKey(reviewerUsername))
                )
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Returns all GROUP_APPROVAL_PENDING requests — regardless of vote state.
     * Useful for a reviewer dashboard showing full picture.
     */
    public List<RequestResponseDTO> getAllGroupPendingRequests() {
        return requestRepository.findAll()
                .stream()
                .filter(r -> r.getStatus() == RequestStatus.GROUP_APPROVAL_PENDING)
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private RequestResponseDTO toResponseDTO(ApprovalRequest request) {
        String employeeName = userRepository.findByUsername(request.getEmployeeId())
                .map(User::getName)
                .orElse(request.getEmployeeId());

        return RequestResponseDTO.builder()
                .requestId(request.getRequestId())
                .description(request.getDescription())
                .status(request.getStatus())
                .employeeId(request.getEmployeeId())
                .employeeName(employeeName)
                .managerId(request.getManagerId())
                .createdAt(request.getCreatedAt())
                .reviewerDecisions(request.getReviewerDecisions())
                .build();
    }
    public List<RequestResponseDTO> getGroupReviewHistory() {
        return requestRepository.findAll()
                .stream()
                .filter(r ->
                    (r.getStatus() == RequestStatus.APPROVED ||
                     r.getStatus() == RequestStatus.REJECTED) &&
                    r.getReviewerDecisions() != null &&
                    !r.getReviewerDecisions().isEmpty()
                )
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}