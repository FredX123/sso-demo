import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { MyApplicationsComponent } from './components/my-applications/my-applications.component';
import { AccessDeniedComponent } from 'common-lib';


export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'my-applications', component: MyApplicationsComponent },
  { path: 'access-denied', component: AccessDeniedComponent },
  { path: '**', redirectTo: '' },
];
