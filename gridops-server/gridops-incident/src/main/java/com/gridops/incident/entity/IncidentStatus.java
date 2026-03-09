package com.gridops.incident.entity;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum IncidentStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    private static final Map<IncidentStatus, Set<IncidentStatus>> TRANSITIONS = Map.of(
            OPEN,        EnumSet.of(ASSIGNED, CLOSED),
            ASSIGNED,    EnumSet.of(IN_PROGRESS, CLOSED),
            IN_PROGRESS, EnumSet.of(RESOLVED, CLOSED),
            RESOLVED,    EnumSet.of(CLOSED),
            CLOSED,      EnumSet.noneOf(IncidentStatus.class)
    );

    public boolean canTransitionTo(IncidentStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public void validateTransition(IncidentStatus target) {
        if (!canTransitionTo(target)) {
            throw new IllegalStateException(
                    String.format("Cannot transition from %s to %s", this, target));
        }
    }
}
