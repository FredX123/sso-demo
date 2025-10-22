import { Component } from '@angular/core';
import { HeaderComponent } from 'common-lib';
import { environment } from '../../../environments/environment';
import { RouterLink } from '@angular/router';

@Component({
  standalone: true,
  selector: 'app-my-applications',
  imports: [HeaderComponent, RouterLink],
  templateUrl: './my-applications.component.html',
  styleUrl: './my-applications.component.scss'
})
export class MyApplicationsComponent {
  env = environment;
}
