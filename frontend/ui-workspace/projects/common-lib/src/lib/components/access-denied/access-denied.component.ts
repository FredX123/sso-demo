import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  standalone: true,
  selector: 'sso-access-denied',
  imports: [RouterLink],
  templateUrl: 'access-denied.component.html',
  styleUrls: ['access-denied.component.css']
})
export class AccessDeniedComponent {}
