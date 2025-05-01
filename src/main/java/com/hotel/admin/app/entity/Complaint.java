package com.hotel.admin.app.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.hotel.admin.app.entity.enums.ComplaintPriority;
import com.hotel.admin.app.entity.enums.ComplaintStatus;

@Entity
@Table(name = "complaint")
@Getter @Setter @ToString(exclude = {"user", "assignedStaff", "actions", "category"}) @NoArgsConstructor
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Optional unique Complaint ID
    @Column(unique = true, length = 20)
    private String complaintId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Who submitted (Customer) (US022)
    private User user;

    @Column(nullable = false) // US022
    private LocalDateTime submissionDate;

    // Option 1: Use Category Entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false) // US022
    private ComplaintCategory category;

    // Option 2: Use String if category entity is overkill
    // @Column(nullable = false, length = 100)
    // private String category;

    @Column(nullable = false, columnDefinition = "TEXT") // US022
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false) // US022, US023
    private ComplaintStatus status;

    // Staff member assigned to handle it (US022, US023)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id", nullable = true)
    private User assignedStaff;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = true) // US023
    private ComplaintPriority priorityLevel;

    // Bidirectional relationship: Complaint owns the actions
    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ComplaintAction> actions = new ArrayList<>();

    // Convenience method
    public void addAction(ComplaintAction action) {
        actions.add(action);
        action.setComplaint(this);
    }

    @Override
    public boolean equals(Object o) { /* ID based equals */
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Complaint complaint = (Complaint) o;
        return id != null && id.equals(complaint.id);
    }
    @Override
    public int hashCode() { /* ID based hashCode */
         return id != null ? Objects.hash(id) : super.hashCode();
    }
}