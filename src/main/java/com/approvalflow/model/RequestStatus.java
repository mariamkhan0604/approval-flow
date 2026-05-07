package com.approvalflow.model;

/**
 * The lifecycle states a Request can be in.
 *
 * PENDING  – just created, awaiting a manager's decision.
 * APPROVED – a manager approved it.
 * REJECTED – a manager rejected it.
 */
public enum RequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}
