package com.hotel.admin.app.entity.enums;

public enum ChargeType {
    ROOM,        // Charge related to room stay
    SERVICE,     // Charge for a predefined extra service (Spa, Room Service)
    ADDITIONAL,  // Manual miscellaneous charges (e.g., damages, snacks)
    TAX,         // Taxes
    DISCOUNT     // Discounts (could be negative amount)
}