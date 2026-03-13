import { Component, Input } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [NgClass],
  template: `<span class="badge" [ngClass]="'badge-' + status.toLowerCase()">{{ status }}</span>`,
  styles: [`
    .badge {
      display: inline-block;
      padding: 2px 10px;
      border-radius: 4px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.3px;
    }
    .badge-operational, .badge-normal { background: #e8f5e9; color: #2e7d32; }
    .badge-degraded, .badge-warning   { background: #fff3e0; color: #e65100; }
    .badge-offline, .badge-critical    { background: #ffebee; color: #c62828; }
    .badge-maintenance                 { background: #e3f2fd; color: #1565c0; }
    .badge-open                        { background: #fff3e0; color: #e65100; }
    .badge-assigned                    { background: #e3f2fd; color: #1565c0; }
    .badge-in_progress                 { background: #e8f5e9; color: #2e7d32; }
    .badge-resolved                    { background: #f3e5f5; color: #6a1b9a; }
    .badge-closed                      { background: #f5f5f5; color: #616161; }
    .badge-good                        { background: #e8f5e9; color: #2e7d32; }
    .badge-fair                        { background: #fff3e0; color: #e65100; }
    .badge-poor                        { background: #ffebee; color: #c62828; }
  `]
})
export class StatusBadgeComponent {
  @Input() status = '';
}
