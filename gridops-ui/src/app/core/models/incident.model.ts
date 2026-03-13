export interface Incident {
  id: number;
  incidentNumber: string;
  title: string;
  description: string;
  severity: string;
  status: string;
  assetId: number | null;
  assetTag: string | null;
  reportedBy: number;
  reportedByUsername: string;
  assignedTo: number | null;
  assignedToUsername: string | null;
  resolutionNotes: string | null;
  createdAt: string;
  updatedAt: string;
  resolvedAt: string | null;
  closedAt: string | null;
}

export interface IncidentHistory {
  id: number;
  incidentId: number;
  changedBy: number;
  changedByUsername: string;
  fieldChanged: string;
  oldValue: string | null;
  newValue: string | null;
  changeNote: string | null;
  createdAt: string;
}

export interface IncidentCreateRequest {
  title: string;
  description?: string;
  severity: string;
  assetId?: number | null;
}

export interface IncidentStatusRequest {
  status: string;
  resolutionNotes?: string;
}

export interface IncidentAssignRequest {
  assignedToUserId: number;
}
