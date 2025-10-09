import { Component, OnDestroy, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthService, DialogComponent } from 'common-lib';
import { Subscription } from 'rxjs';

@Component({
  standalone: true,
  selector: 'app-root',
  imports: [RouterOutlet, DialogComponent],
  template: `
    <div class="container">
      <router-outlet />
      <mcl-dialog></mcl-dialog>
    </div>
  `,
})
export class AppComponent implements OnInit, OnDestroy {

  private sub?: Subscription;

  constructor(private auth: AuthService) {}

  ngOnInit(): void {
      // Prime auth state on app load
      this.sub = this.auth.me().subscribe();
  }

  ngOnDestroy(): void {
      this.sub?.unsubscribe();
  }
}
