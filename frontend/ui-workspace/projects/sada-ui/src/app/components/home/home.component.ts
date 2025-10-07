import { Component } from '@angular/core';
import { AuthService, HeaderComponent } from 'common-lib';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SadaService } from '../../services/sada.service';
import { RouterLink } from '@angular/router';

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

  constructor(private authService: AuthService,
    private sadaService: SadaService) {}

  whoAmI(): void {
    this.authService.whoAmI().pipe(
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
}
