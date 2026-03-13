import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Asset, Telemetry, Inspection, Page } from '../models/asset.model';

@Injectable({ providedIn: 'root' })
export class AssetService {

  private baseUrl = `${environment.apiBaseUrl}/api/assets`;

  constructor(private http: HttpClient) {}

  list(page = 0, size = 20, type?: string, status?: string): Observable<Page<Asset>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (type) params = params.set('type', type);
    if (status) params = params.set('status', status);
    return this.http.get<Page<Asset>>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Asset> {
    return this.http.get<Asset>(`${this.baseUrl}/${id}`);
  }

  getTelemetry(id: number): Observable<Telemetry> {
    return this.http.get<Telemetry>(`${this.baseUrl}/${id}/telemetry`);
  }

  getInspections(id: number, page = 0, size = 10): Observable<Page<Inspection>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<Page<Inspection>>(`${this.baseUrl}/${id}/inspections`, { params });
  }
}
