import { Component, OnDestroy, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Subscription } from 'rxjs';

import { 
  AuthService,
  DialogComponent, 
  SessionDialogComponent, 
  SessionTimerService 
} from 'common-lib';

@Component({
  standalone: true,
  selector: 'app-root',
  imports: [ RouterOutlet, DialogComponent, SessionDialogComponent ],
  template: `
    <div class="container">
      <router-outlet />
      <mcl-dialog></mcl-dialog>                  <!-- generic messages (401, etc.) -->
      <mcl-session-dialog></mcl-session-dialog>  <!-- session expiring modal -->
    </div>
  `,
})
export class AppComponent implements OnInit, OnDestroy {

  private sub?: Subscription;

  constructor(private auth: AuthService, private timer: SessionTimerService) {}

  ngOnInit(): void {
    // prime auth state and start the session watcher
    this.sub = this.auth.loadAuth({ silent: true }).subscribe(() => {
      this.timer.startWatching();
    });
  }

  ngOnDestroy(): void {
      this.sub?.unsubscribe();
  }
}
