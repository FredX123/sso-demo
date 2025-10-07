import { Component } from '@angular/core';
import { HeaderComponent } from 'common-lib';
import { environment } from '../../../environments/environment';
import { RouterLink } from '@angular/router';

@Component({
  standalone: true,
  selector: 'app-dashboard',
  imports: [HeaderComponent, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  env = environment;
}
