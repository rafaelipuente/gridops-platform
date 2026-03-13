export interface Asset {
  id: number;
  assetTag: string;
  name: string;
  assetType: string;
  status: string;
  location: string;
  latitude: number;
  longitude: number;
  installedDate: string;
  createdBy: number;
  createdByUsername: string;
  createdAt: string;
  updatedAt: string;
}

export interface Telemetry {
  assetTag: string;
  timestamp: string;
  temperatureCelsius: number;
  loadPercent: number;
  voltageKv: number;
  powerOutputMw: number;
  status: string;
}

export interface Inspection {
  id: number;
  assetId: number;
  inspectedBy: number;
  inspectedByUsername: string;
  inspectionDate: string;
  notes: string;
  condition: string;
  createdAt: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
