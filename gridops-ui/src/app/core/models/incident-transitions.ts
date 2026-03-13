/**
 * Single source of truth for incident status transitions on the frontend.
 * Mirrors IncidentStatus.java TRANSITIONS map exactly.
 */
const TRANSITIONS: Record<string, string[]> = {
  OPEN:        ['ASSIGNED', 'CLOSED'],
  ASSIGNED:    ['IN_PROGRESS', 'CLOSED'],
  IN_PROGRESS: ['RESOLVED', 'CLOSED'],
  RESOLVED:    ['CLOSED'],
  CLOSED:      []
};

export const INCIDENT_STATUSES = ['OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];

export const INCIDENT_SEVERITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export function getValidTransitions(currentStatus: string): string[] {
  return TRANSITIONS[currentStatus] || [];
}

export function canTransition(from: string, to: string): boolean {
  return getValidTransitions(from).includes(to);
}

export function statusLabel(status: string): string {
  return status.replace(/_/g, ' ');
}
