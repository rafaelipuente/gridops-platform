import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { IncidentService } from '../../../core/services/incident.service';
import { AuthService } from '../../../core/services/auth.service';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { RelativeTimePipe } from '../../../shared/pipes/relative-time.pipe';
import { Incident, IncidentHistory } from '../../../core/models/incident.model';
import { UserSummary } from '../../../core/models/user.model';
import {
  getValidTransitions,
  statusLabel,
  INCIDENT_STATUSES
} from '../../../core/models/incident-transitions';

@Component({
  selector: 'app-incident-detail',
  standalone: true,
  imports: [FormsModule, RouterLink, DatePipe, StatusBadgeComponent, RelativeTimePipe],
  templateUrl: './incident-detail.component.html',
  styleUrl: './incident-detail.component.css'
})
export class IncidentDetailComponent implements OnInit {
  incident: Incident | null = null;
  history: IncidentHistory[] = [];
  engineers: UserSummary[] = [];

  loading = true;
  error = '';
  actionError = '';
  actionSuccess = '';
  actionLoading = false;

  validTransitions: string[] = [];
  selectedTransition = '';
  resolutionNotes = '';

  selectedEngineerId: number | null = null;

  statuses = INCIDENT_STATUSES;
  statusLabel = statusLabel;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private incidentService: IncidentService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadIncident(id);
    this.loadHistory(id);

    if (this.auth.getRole() === 'ADMIN') {
      this.incidentService.getEngineers().subscribe({
        next: list => this.engineers = list,
        error: () => {}
      });
    }
  }

  get isAdmin(): boolean {
    return this.auth.getRole() === 'ADMIN';
  }

  get requiresResolutionNotes(): boolean {
    return this.selectedTransition === 'RESOLVED';
  }

  get canSubmitTransition(): boolean {
    if (!this.selectedTransition || this.actionLoading) return false;
    if (this.requiresResolutionNotes && !this.resolutionNotes.trim()) return false;
    return true;
  }

  get canSubmitAssign(): boolean {
    return this.selectedEngineerId !== null && !this.actionLoading;
  }

  submitTransition(): void {
    if (!this.incident || !this.canSubmitTransition) return;
    this.actionLoading = true;
    this.clearMessages();

    const body: { status: string; resolutionNotes?: string } = { status: this.selectedTransition };
    if (this.resolutionNotes.trim()) body.resolutionNotes = this.resolutionNotes.trim();

    this.incidentService.transitionStatus(this.incident.id, body).subscribe({
      next: updated => {
        this.incident = updated;
        this.refreshTransitions();
        this.loadHistory(updated.id);
        this.selectedTransition = '';
        this.resolutionNotes = '';
        this.actionSuccess = `Status changed to ${statusLabel(updated.status)}`;
        this.actionLoading = false;
      },
      error: err => {
        this.actionError = err.error?.message || err.error?.error || 'Status transition failed';
        this.actionLoading = false;
      }
    });
  }

  submitAssign(): void {
    if (!this.incident || !this.canSubmitAssign) return;
    this.actionLoading = true;
    this.clearMessages();

    this.incidentService.assign(this.incident.id, {
      assignedToUserId: this.selectedEngineerId!
    }).subscribe({
      next: updated => {
        this.incident = updated;
        this.refreshTransitions();
        this.loadHistory(updated.id);
        this.selectedEngineerId = null;
        this.actionSuccess = `Assigned to ${updated.assignedToUsername}`;
        this.actionLoading = false;
      },
      error: err => {
        this.actionError = err.error?.message || err.error?.error || 'Assignment failed';
        this.actionLoading = false;
      }
    });
  }

  private loadIncident(id: number): void {
    this.incidentService.getById(id).subscribe({
      next: inc => {
        this.incident = inc;
        this.refreshTransitions();
        this.loading = false;
      },
      error: err => {
        this.error = err.status === 404 ? 'Incident not found' : 'Failed to load incident';
        this.loading = false;
      }
    });
  }

  private loadHistory(id: number): void {
    this.incidentService.getHistory(id).subscribe({
      next: h => this.history = h,
      error: () => {}
    });
  }

  private refreshTransitions(): void {
    if (this.incident) {
      this.validTransitions = getValidTransitions(this.incident.status);
      this.selectedTransition = '';
      this.resolutionNotes = '';
    }
  }

  private clearMessages(): void {
    this.actionError = '';
    this.actionSuccess = '';
  }
}
