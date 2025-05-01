package com.hotel.admin.app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "complaint_action")
@Getter @Setter @ToString(exclude = {"complaint", "user"}) @NoArgsConstructor
public class ComplaintAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint; // Link back to the main complaint

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Who took the action (Admin/Staff)
    private User user;

    @Column(nullable = false)
    private LocalDateTime timestamp; // When the action was taken (US023)

    @Column(nullable = false, columnDefinition = "TEXT") // Or length = 1000 etc.
    private String actionDescription; // What was done (US023)

    @Column(columnDefinition = "TEXT")
    private String internalNotes; // Notes visible only internally (US023)

    @Override
    public boolean equals(Object o) { /* ID based equals */
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplaintAction that = (ComplaintAction) o;
        return id != null && id.equals(that.id);
    }
    @Override
    public int hashCode() { /* ID based hashCode */
        return id != null ? Objects.hash(id) : super.hashCode();
    }
}