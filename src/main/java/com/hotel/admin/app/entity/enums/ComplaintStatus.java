package com.hotel.admin.app.entity.enums;

public enum ComplaintStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED, // Awaiting confirmation?
    CLOSED,   // Final state
    REOPENED // Optional
}