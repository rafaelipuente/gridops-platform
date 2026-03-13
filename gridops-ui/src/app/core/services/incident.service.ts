import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Incident, IncidentHistory,
  IncidentCreateRequest, IncidentStatusRequest, IncidentAssignRequest
} from '../models/incident.model';
import { Page } from '../models/asset.model';
import { UserSummary } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class IncidentService {

  private baseUrl = `${environment.apiBaseUrl}/api/incidents`;

  constructor(private http: HttpClient) {}

  list(page = 0, size = 20, status?: string): Observable<Page<Incident>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (status) params = params.set('status', status);
    return this.http.get<Page<Incident>>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Incident> {
    return this.http.get<Incident>(`${this.baseUrl}/${id}`);
  }

  create(request: IncidentCreateRequest): Observable<Incident> {
    return this.http.post<Incident>(this.baseUrl, request);
  }

  assign(id: number, request: IncidentAssignRequest): Observable<Incident> {
    return this.http.patch<Incident>(`${this.baseUrl}/${id}/assign`, request);
  }

  transitionStatus(id: number, request: IncidentStatusRequest): Observable<Incident> {
    return this.http.patch<Incident>(`${this.baseUrl}/${id}/status`, request);
  }

  getHistory(id: number): Observable<IncidentHistory[]> {
    return this.http.get<IncidentHistory[]>(`${this.baseUrl}/${id}/history`);
  }

  getEngineers(): Observable<UserSummary[]> {
    return this.http.get<UserSummary[]>(`${environment.apiBaseUrl}/api/auth/engineers`);
  }
}
