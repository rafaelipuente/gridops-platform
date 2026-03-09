package com.gridops.incident.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "incident_history")
public class IncidentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_id", nullable = false, updatable = false)
    private Long incidentId;

    @Column(name = "changed_by", nullable = false, updatable = false)
    private Long changedBy;

    @Column(name = "field_changed", nullable = false, length = 50, updatable = false)
    private String fieldChanged;

    @Column(name = "old_value", columnDefinition = "TEXT", updatable = false)
    private String oldValue;

    @Column(name = "new_value", nullable = false, columnDefinition = "TEXT", updatable = false)
    private String newValue;

    @Column(name = "change_note", columnDefinition = "TEXT", updatable = false)
    private String changeNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected IncidentHistory() {
    }

    public IncidentHistory(Long incidentId, Long changedBy, String fieldChanged,
                           String oldValue, String newValue, String changeNote) {
        this.incidentId = incidentId;
        this.changedBy = changedBy;
        this.fieldChanged = fieldChanged;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changeNote = changeNote;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getIncidentId() {
        return incidentId;
    }

    public Long getChangedBy() {
        return changedBy;
    }

    public String getFieldChanged() {
        return fieldChanged;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getChangeNote() {
        return changeNote;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IncidentHistory other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "IncidentHistory{id=" + id + ", incidentId=" + incidentId
                + ", field='" + fieldChanged + "', " + oldValue + " -> " + newValue + "}";
    }
}
