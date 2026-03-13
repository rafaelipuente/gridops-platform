import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { IncidentService } from '../../../core/services/incident.service';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { RelativeTimePipe } from '../../../shared/pipes/relative-time.pipe';
import { Incident } from '../../../core/models/incident.model';
import { Page } from '../../../core/models/asset.model';
import { INCIDENT_STATUSES } from '../../../core/models/incident-transitions';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-incident-list',
  standalone: true,
  imports: [FormsModule, RouterLink, DatePipe, StatusBadgeComponent, RelativeTimePipe],
  templateUrl: './incident-list.component.html',
  styleUrl: './incident-list.component.css'
})
export class IncidentListComponent implements OnInit {
  incidents: Incident[] = [];
  loading = true;
  error = '';

  page = 0;
  totalPages = 0;
  totalElements = 0;
  size = 20;

  statusFilter = '';
  statuses = INCIDENT_STATUSES;

  constructor(
    private incidentService: IncidentService,
    private route: ActivatedRoute,
    private router: Router,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.statusFilter = params['status'] || '';
      this.page = params['page'] ? +params['page'] : 0;
      this.loadIncidents();
    });
  }

  onFilterChange(): void {
    this.page = 0;
    this.updateUrl();
  }

  clearFilter(): void {
    this.statusFilter = '';
    this.page = 0;
    this.updateUrl();
  }

  goToPage(p: number): void {
    if (p < 0 || p >= this.totalPages) return;
    this.page = p;
    this.updateUrl();
  }

  private updateUrl(): void {
    const queryParams: Record<string, string> = {};
    if (this.statusFilter) queryParams['status'] = this.statusFilter;
    if (this.page > 0) queryParams['page'] = this.page.toString();
    this.router.navigate([], { queryParams, queryParamsHandling: '' });
  }

  private loadIncidents(): void {
    this.loading = true;
    this.error = '';
    this.incidentService.list(
      this.page, this.size,
      this.statusFilter || undefined
    ).subscribe({
      next: (data: Page<Incident>) => {
        this.incidents = data.content;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load incidents';
        this.loading = false;
      }
    });
  }
}
