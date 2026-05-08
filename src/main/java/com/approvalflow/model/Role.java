package com.approvalflow.model;

/**
 * Defines the two roles in the system.
 *
 * EMPLOYEE – can create requests.
 * MANAGER  – can approve or reject requests.
 *
 * Spring Security expects role strings to start with "ROLE_"
 * (e.g. "ROLE_EMPLOYEE"). We handle that prefix in SecurityConfig
 * so the enum values stay clean here.
 */
public enum Role {
    EMPLOYEE,
    MANAGER,
    REVIEWER
}
