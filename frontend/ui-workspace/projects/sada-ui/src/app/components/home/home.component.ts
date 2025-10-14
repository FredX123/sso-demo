import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthService, HeaderComponent } from 'common-lib';
import { SadaService } from '../../services/sada.service';

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

  constructor(private auth: AuthService, private sadaService: SadaService) {}

  whoAmI(): void {
    this.auth.me().pipe(
      takeUntil(this.destroy$)
    ).subscribe(resp => {
      this.me = resp;
    });
  }

  callSada(): void {
    this.sadaService.callSada().pipe(
      takeUntil(this.destroy$)
    ).subscribe(resp => {
      this.data = resp;
    });
  }

  callMybViaBackend(): void {
    this.sadaService.callMybViaBackend().pipe(
      takeUntil(this.destroy$)
    ).subscribe(resp => {
      this.data = resp;
    });
  }

  callMybViaBackendNoToken(): void {
    this.sadaService.callMybViaBackendNoToken().pipe(
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
