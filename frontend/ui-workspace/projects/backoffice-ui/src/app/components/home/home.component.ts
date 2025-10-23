import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthMe, AuthService, HeaderComponent } from 'common-lib';
import { BackOfficeService } from '../../services/back-office.service';

@Component({
  standalone: true,
  selector: 'app-home',
  imports: [CommonModule, RouterLink, HeaderComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {

  env = environment;
  me?: AuthMe;
  data: any;

  private destroy$ = new Subject<void>();

  constructor(private auth: AuthService, private backOfficeService: BackOfficeService) {}

  whoAmI(): void {
    this.auth.getAuthCache().pipe(
      takeUntil(this.destroy$)
    ).subscribe(resp => {
      this.me = resp;
    });
  }

  callBo(): void {
    this.backOfficeService.callBo().pipe(
      takeUntil(this.destroy$)
    ).subscribe(resp => {
      this.data = resp;
    });
  }

  callFoViaBackend(): void {
    this.backOfficeService.callFoViaBackend().pipe(
      takeUntil(this.destroy$)
    ).subscribe(resp => {
      this.data = resp;
    });
  }

  callFoViaBackendNoToken(): void {
    this.backOfficeService.callFoViaBackendNoToken().pipe(
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
