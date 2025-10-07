import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subject } from 'rxjs';

@Component({
  standalone: true,
  selector: 'mcl-header',
  imports: [CommonModule, RouterLink],
  templateUrl: 'header.component.html',
})
export class HeaderComponent {
  @Input() appTitle = 'App';
  @Input() loginUrl = '';
  @Input() logoutUrl = '';
  @Input() crossAppUrl = '';
  @Input() crossAppLabel = 'Go to Other App';

  private destroy$ = new Subject<void>();

  loggedIn = true; // For demo; in real apps, check via /api/me or cookie

  constructor() {}

  logout(ev: Event) {
    const form = document.getElementById('logoutForm') as HTMLFormElement;
    form?.submit(); // ✅ Spring handles the logout, redirect, and session invalidation
  }
}
