import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthService, HeaderComponent } from 'common-lib';
import { FrontOfficeService } from '../../services/front-office.service';

@Component({
  standalone: true,
  selector: 'app-home',
  imports: [CommonModule, RouterLink, HeaderComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {

  env = environment;
  me: any; data: any;

  private destroy$ = new Subject<void>();

  constructor(private auth: AuthService, private frontOfficeService: FrontOfficeService) {}

  whoAmI(): void {
    this.auth.getAuthCache().pipe(
      takeUntil(this.destroy$)
    ).subscribe(resp => {
      this.me = resp;
    });
  }

  callFo(): void {
    this.frontOfficeService.callFo().pipe(
      takeUntil(this.destroy$)
    ).subscribe(resp => {
      this.data = resp;
    });
  }

  callBoViaBackend(): void {
    this.frontOfficeService.callBoViaBackend().pipe(
      takeUntil(this.destroy$)
    ).subscribe(resp => {
      this.data = resp;
    });
  }

  callBoViaBackendNoToken(): void {
    this.frontOfficeService.callBoViaBackendNoToken().pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: resp => this.data = resp,
      error: err => this.data = err.error ? err.error : err,
      complete: () => console.log('API call completed')
    });
  }

  onLogout() {
    this.auth.logout();
  }
}
