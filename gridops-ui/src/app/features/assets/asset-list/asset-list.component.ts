import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { AssetService } from '../../../core/services/asset.service';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { Asset, Page } from '../../../core/models/asset.model';

const ASSET_TYPES = ['SUBSTATION', 'TRANSFORMER', 'LINE_SEGMENT', 'SWITCH', 'METER'];
const ASSET_STATUSES = ['OPERATIONAL', 'DEGRADED', 'OFFLINE', 'MAINTENANCE'];

@Component({
  selector: 'app-asset-list',
  standalone: true,
  imports: [FormsModule, RouterLink, StatusBadgeComponent],
  templateUrl: './asset-list.component.html',
  styleUrl: './asset-list.component.css'
})
export class AssetListComponent implements OnInit {
  assets: Asset[] = [];
  loading = true;
  error = '';

  page = 0;
  totalPages = 0;
  totalElements = 0;
  size = 20;

  typeFilter = '';
  statusFilter = '';

  assetTypes = ASSET_TYPES;
  assetStatuses = ASSET_STATUSES;

  constructor(
    private assetService: AssetService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.typeFilter = params['type'] || '';
      this.statusFilter = params['status'] || '';
      this.page = params['page'] ? +params['page'] : 0;
      this.loadAssets();
    });
  }

  onFilterChange(): void {
    this.page = 0;
    this.updateUrl();
  }

  clearFilters(): void {
    this.typeFilter = '';
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
    if (this.typeFilter) queryParams['type'] = this.typeFilter;
    if (this.statusFilter) queryParams['status'] = this.statusFilter;
    if (this.page > 0) queryParams['page'] = this.page.toString();
    this.router.navigate([], { queryParams, queryParamsHandling: '' });
  }

  private loadAssets(): void {
    this.loading = true;
    this.error = '';
    this.assetService.list(
      this.page, this.size,
      this.typeFilter || undefined,
      this.statusFilter || undefined
    ).subscribe({
      next: (data: Page<Asset>) => {
        this.assets = data.content;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load assets';
        this.loading = false;
      }
    });
  }
}
