import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { LayoutComponent } from './layout/layout.component';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'assets',
        loadComponent: () =>
          import('./features/assets/asset-list/asset-list.component').then(m => m.AssetListComponent)
      },
      {
        path: 'assets/:id',
        loadComponent: () =>
          import('./features/assets/asset-detail/asset-detail.component').then(m => m.AssetDetailComponent)
      },
      {
        path: 'incidents',
        loadComponent: () =>
          import('./features/incidents/incident-list/incident-list.component').then(m => m.IncidentListComponent)
      },
      {
        path: 'incidents/new',
        loadComponent: () =>
          import('./features/incidents/incident-create/incident-create.component').then(m => m.IncidentCreateComponent)
      },
      {
        path: 'incidents/:id',
        loadComponent: () =>
          import('./features/incidents/incident-detail/incident-detail.component').then(m => m.IncidentDetailComponent)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
