import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthService, HeaderComponent } from 'common-lib';
import { MyBService } from '../../services/myb.service';

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

  constructor(private auth: AuthService, private mybService: MyBService) {}

  whoAmI(): void {
    this.auth.me().pipe(
      takeUntil(this.destroy$)
    ).subscribe(resp => {
      this.me = resp;
    });
  }

  callMyB(): void {
    this.mybService.callMyb().pipe(
      takeUntil(this.destroy$)
    ).subscribe(resp => {
      this.data = resp;
    });
  }

  callSadaViaBackend(): void {
    this.mybService.callSadaViaBackend().pipe(
      takeUntil(this.destroy$)
    ).subscribe(resp => {
      this.data = resp;
    });
  }

  callSadaViaBackendNoToken(): void {
    this.mybService.callSadaViaBackendNoToken().pipe(
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
