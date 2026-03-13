import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { AssetService } from '../../../core/services/asset.service';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { RelativeTimePipe } from '../../../shared/pipes/relative-time.pipe';
import { Asset, Telemetry, Inspection, Page } from '../../../core/models/asset.model';

@Component({
  selector: 'app-asset-detail',
  standalone: true,
  imports: [RouterLink, DatePipe, StatusBadgeComponent, RelativeTimePipe],
  templateUrl: './asset-detail.component.html',
  styleUrl: './asset-detail.component.css'
})
export class AssetDetailComponent implements OnInit {
  asset: Asset | null = null;
  telemetry: Telemetry | null = null;
  inspections: Inspection[] = [];

  loadingAsset = true;
  loadingTelemetry = true;
  loadingInspections = true;

  assetError = '';
  telemetryError = '';
  inspectionError = '';

  constructor(private route: ActivatedRoute, private assetService: AssetService) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadAsset(id);
    this.loadTelemetry(id);
    this.loadInspections(id);
  }

  refreshTelemetry(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadTelemetry(id);
  }

  private loadAsset(id: number): void {
    this.assetService.getById(id).subscribe({
      next: (data) => {
        this.asset = data;
        this.loadingAsset = false;
      },
      error: (err) => {
        this.assetError = err.status === 404 ? 'Asset not found' : 'Failed to load asset';
        this.loadingAsset = false;
      }
    });
  }

  private loadTelemetry(id: number): void {
    this.loadingTelemetry = true;
    this.telemetryError = '';
    this.telemetry = null;
    this.assetService.getTelemetry(id).subscribe({
      next: (data) => {
        this.telemetry = data;
        this.loadingTelemetry = false;
      },
      error: (err) => {
        this.telemetryError = err.status === 503
          ? 'Telemetry service is currently unavailable'
          : 'Failed to load telemetry data';
        this.loadingTelemetry = false;
      }
    });
  }

  private loadInspections(id: number): void {
    this.assetService.getInspections(id, 0, 50).subscribe({
      next: (data: Page<Inspection>) => {
        this.inspections = data.content;
        this.loadingInspections = false;
      },
      error: () => {
        this.inspectionError = 'Failed to load inspections';
        this.loadingInspections = false;
      }
    });
  }
}
