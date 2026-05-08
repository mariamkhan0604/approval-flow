package com.approvalflow.service;

import com.approvalflow.dto.CreateRequestDTO;
import com.approvalflow.dto.RequestResponseDTO;
import com.approvalflow.dto.UpdateStatusDTO;
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

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalRequestService {

    private final ApprovalRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final GroupApprovalService groupApprovalService;

    public RequestResponseDTO createRequest(
            CreateRequestDTO dto,
            Authentication authentication) {

        String employeeId = authentication.getName();

        log.info(
                "Employee '{}' is creating a new request: {}",
                employeeId,
                dto.getDescription()
        );

        ApprovalRequest request = ApprovalRequest.builder()
                .description(dto.getDescription())
                .status(RequestStatus.PENDING)
                .employeeId(employeeId)
                .managerId(null)
                .createdAt(LocalDateTime.now())
                .build();

        ApprovalRequest saved = requestRepository.save(request);

        log.info("Request saved with id: {}", saved.getRequestId());

        return toResponseDTO(saved);
    }

    public RequestResponseDTO updateStatus(
            String requestId,
            UpdateStatusDTO dto,
            Authentication authentication) {

        String managerId = authentication.getName();

        log.info(
                "Manager '{}' is updating request '{}' to '{}'",
                managerId,
                requestId,
                dto.getStatus()
        );

        ApprovalRequest request = requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Request not found with id: " + requestId
                        )
                );

        if (dto.getStatus() == RequestStatus.PENDING) {
            throw new BadRequestException(
                    "Status cannot be set back to PENDING."
            );
        }

        request.setManagerId(managerId);

        if (dto.getStatus() == RequestStatus.APPROVED) {

            // Save manager assignment first
            requestRepository.save(request);

            // Move request to group approval stage
            groupApprovalService.moveToGroupApproval(request);

            // Fetch updated request after group stage update
            ApprovalRequest updatedRequest = requestRepository
                    .findById(requestId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Request not found with id: " + requestId
                            )
                    );

            log.info(
                    "Request '{}' moved to group approval stage",
                    requestId
            );

            return toResponseDTO(updatedRequest);
        }

        // Direct rejection by manager
        request.setStatus(RequestStatus.REJECTED);

        ApprovalRequest updated = requestRepository.save(request);

        log.info(
                "Request '{}' rejected directly by manager",
                requestId
        );

        return toResponseDTO(updated);
    }

    public List<RequestResponseDTO> getAllRequests() {

        return requestRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .sorted(
                        Comparator.comparing(
                                RequestResponseDTO::getCreatedAt
                        ).reversed()
                )
                .collect(Collectors.toList());
    }

    public List<RequestResponseDTO> getMyRequests(String username) {

        return requestRepository.findByEmployeeId(username)
                .stream()
                .map(this::toResponseDTO)
                .sorted(
                        Comparator.comparing(
                                RequestResponseDTO::getCreatedAt
                        ).reversed()
                )
                .collect(Collectors.toList());
    }

    public Map<String, Long> getStats(Authentication authentication) {

        List<ApprovalRequest> requests;

        if (authentication.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_MANAGER"))) {

            requests = requestRepository.findAll();

        } else {

            requests = requestRepository.findByEmployeeId(
                    authentication.getName()
            );
        }

        return Map.of(
                "total",
                (long) requests.size(),

                "pending",
                requests.stream()
                        .filter(r ->
                                r.getStatus() == RequestStatus.PENDING)
                        .count(),

                "approved",
                requests.stream()
                        .filter(r ->
                                r.getStatus() == RequestStatus.APPROVED)
                        .count(),

                "rejected",
                requests.stream()
                        .filter(r ->
                                r.getStatus() == RequestStatus.REJECTED)
                        .count()
        );
    }

    private RequestResponseDTO toResponseDTO(ApprovalRequest request) {

        String employeeName = userRepository
                .findByUsername(request.getEmployeeId())
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
}