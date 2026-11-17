package com.jesse.taskapp.core.domain.model

enum class PendingOperationStatus {
    AWAITING_USER_APPROVAL,
    AWAITING_BIOMETRIC,
    COMPLETED,
    CANCELLED,
    EXPIRED,
    STALE,
    FAILED,
}