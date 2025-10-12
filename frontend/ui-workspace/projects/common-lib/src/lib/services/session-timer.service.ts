import { Injectable, inject, OnDestroy } from '@angular/core';
import { AuthStateService } from './auth-state.service';
import { AuthService } from './auth.service';
import { BehaviorSubject, Subject, timer, interval, of } from 'rxjs';
import { switchMap, takeUntil, takeWhile, tap, catchError, filter } from 'rxjs/operators';
import { LOGIN_URL } from '../core/tokens';

export interface SessionDialogState {
  visible: boolean;
  secondsLeft: number;
}

@Injectable({ providedIn: 'root' })
export class SessionTimerService implements OnDestroy {
  private readonly authState = inject(AuthStateService);
  private readonly auth = inject(AuthService);
  private readonly loginUrl = inject(LOGIN_URL);

  readonly dialog$ = new BehaviorSubject<SessionDialogState>({ visible: false, secondsLeft: 60 });

  private readonly destroy$ = new Subject<void>();
  private readonly leadSeconds = 60;

  /** Begin watching for token expiry */
  startWatching(): void {
    this.authState.me$
      .pipe(
        takeUntil(this.destroy$),
        filter(me => !!me), // skip undefined initial
        switchMap(me => {
          // clear previous modal
          this.dialog$.next({ visible: false, secondsLeft: this.leadSeconds });

          if (!me.authenticated || !me.expiresAt) {
            return of(); // not logged in
          }

          const expiresAtMs = new Date(me.expiresAt as any).getTime();
          const now = Date.now();
          const dueInMs = Math.max(expiresAtMs - now - this.leadSeconds * 1000, 0);

          // wait until (expiry - 60s), then show dialog
          return timer(dueInMs).pipe(
            tap(() => {
              this.dialog$.next({ visible: true, secondsLeft: this.leadSeconds });
            }),
            switchMap(() =>
              // start countdown
              interval(1000).pipe(
                takeWhile(() => this.dialog$.value.visible && this.dialog$.value.secondsLeft > 0),
                tap(() => {
                  const next = Math.max(this.dialog$.value.secondsLeft - 1, 0);
                  this.dialog$.next({ ...this.dialog$.value, secondsLeft: next });
                }),
                takeUntil(this.destroy$)
              )
            )
          );
        })
      )
      .subscribe({
        complete: () => this.ngOnDestroy(),
      });
  }

  /** User clicked "Continue" */
  continue() {
    console.log('refresh token - on Continue');
    this.auth
      .refresh()
      .pipe(
        switchMap(() => this.auth.me()),
        catchError(() => of(null)),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: () => {
          this.hideDialog();
          // startWatching() stays active; next /me emission will reschedule timers
        },
        error: () => this.exit(),
      });
  }

  /** User clicked "Exit" */
  exit() {
    this.auth
      .logout()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => (window.location.href = this.loginUrl),
        error: () => (window.location.href = this.loginUrl),
      });
  }

  hideDialog() {
    this.dialog$.next({ visible: false, secondsLeft: this.leadSeconds });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}