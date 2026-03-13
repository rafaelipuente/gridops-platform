import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { IncidentService } from '../../../core/services/incident.service';
import { AssetService } from '../../../core/services/asset.service';
import { IncidentCreateRequest } from '../../../core/models/incident.model';
import { INCIDENT_SEVERITIES } from '../../../core/models/incident-transitions';
import { Asset, Page } from '../../../core/models/asset.model';

@Component({
  selector: 'app-incident-create',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './incident-create.component.html',
  styleUrl: './incident-create.component.css'
})
export class IncidentCreateComponent implements OnInit {
  severities = INCIDENT_SEVERITIES;
  assets: Asset[] = [];

  title = '';
  description = '';
  severity = 'MEDIUM';
  assetId: number | null = null;

  submitting = false;
  error = '';

  constructor(
    private incidentService: IncidentService,
    private assetService: AssetService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.assetService.list(0, 100).subscribe({
      next: (data: Page<Asset>) => this.assets = data.content,
      error: () => {}
    });
  }

  get canSubmit(): boolean {
    return this.title.trim().length > 0 && !this.submitting;
  }

  submit(): void {
    if (!this.canSubmit) return;
    this.submitting = true;
    this.error = '';

    const body: IncidentCreateRequest = {
      title: this.title.trim(),
      severity: this.severity
    };
    if (this.description.trim()) body.description = this.description.trim();
    if (this.assetId) body.assetId = this.assetId;

    this.incidentService.create(body).subscribe({
      next: incident => this.router.navigate(['/incidents', incident.id]),
      error: err => {
        this.submitting = false;
        this.error = err.error?.message || err.error?.error || 'Failed to create incident';
      }
    });
  }
}
