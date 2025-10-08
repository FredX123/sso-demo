import { Component } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HeaderComponent } from 'common-lib';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { MyBService } from '../../services/myb.service';
import { RouterLink } from '@angular/router';
import { AuthService } from 'common-lib';

@Component({
  standalone: true,
  selector: 'app-home',
  imports: [CommonModule, RouterLink,  HeaderComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {

  env = environment;
  me: any; data: any;

  private destroy$ = new Subject<void>();

  constructor(private auth: AuthService,
    private mybService: MyBService) {}

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

  onLogout() {
    this.auth.logout().subscribe({
      next: () => window.location.href = this.env.loginUrl, // or navigate to your app root
      error: () => window.location.href = this.env.loginUrl
    });
  }
}
