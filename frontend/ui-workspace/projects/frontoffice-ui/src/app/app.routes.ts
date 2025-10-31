import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { AccessDeniedComponent } from 'common-lib';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'access-denied', component: AccessDeniedComponent },
  { path: '**', redirectTo: '' },
];
