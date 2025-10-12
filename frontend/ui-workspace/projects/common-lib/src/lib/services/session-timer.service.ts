import { Injectable, OnDestroy, inject } from '@angular/core';
import { AuthService } from './auth.service';
import { AuthStateService } from './auth-state.service';
import { BehaviorSubject, Subject, Subscription, fromEvent, interval, merge, of, timer } from 'rxjs';
import { catchError, filter, map, switchMap, takeUntil, tap, throttleTime } from 'rxjs/operators';
import { APP_URL } from '../core/tokens';
import { AuthMe } from '../model/auth.models';

export interface SessionDialogState {
  visible: boolean;
  secondsLeft: number;
}

@Injectable({ providedIn: 'root' })
export class SessionTimerService implements OnDestroy {
  private readonly authState = inject(AuthStateService);
  private readonly auth = inject(AuthService);
  private readonly appUrl = inject(APP_URL);

  readonly dialog$ = new BehaviorSubject<SessionDialogState>({ visible: false, secondsLeft: 60 });

  private readonly destroy$ = new Subject<void>();
  private readonly leadSeconds = 60;
  private readonly activityThrottleMs = 60_000; // refresh at most once per minute via activity

  private watchStarted = false;
  private refreshInFlight = false;
  private currentExpiryMs?: number;

  private activitySub?: Subscription;
  private countdownSub?: Subscription;
  private dialogTriggerSub?: Subscription;

  /** Begin watching for token expiry */
  startWatching(): void {
    if (this.watchStarted) {
      return;
    }
    this.watchStarted = true;

    this.authState.me$
      .pipe(
        takeUntil(this.destroy$),
        filter((me): me is AuthMe => !!me),
        tap(me => this.handleAuthState(me))
      )
      .subscribe();

    this.listenForActivity();
  }

  /** User clicked "Continue" */
  continue() {
    this.refreshSession('dialog');
  }

  /** User clicked "Exit" */
  exit() {
    this.auth
      .logout()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => (window.location.href = this.appUrl),
        error: () => (window.location.href = this.appUrl),
      });
  }

  hideDialog() {
    this.stopCountdown();
    this.dialog$.next({ visible: false, secondsLeft: this.leadSeconds });
  }

  ngOnDestroy(): void {
    this.stopCountdown();
    this.cancelDialogTrigger();
    this.activitySub?.unsubscribe();
    this.refreshInFlight = false;
    this.destroy$.next();
    this.destroy$.complete();
  }

  private handleAuthState(me: AuthMe) {
    if (!me.authenticated || !me.expiresAt) {
      this.currentExpiryMs = undefined;
      this.hideDialog();
      this.cancelDialogTrigger();
      return;
    }

    const expiresAtMs = Date.parse(me.expiresAt);
    if (Number.isNaN(expiresAtMs)) {
      return;
    }

    this.currentExpiryMs = expiresAtMs;
    if (this.dialog$.value.visible) {
      this.hideDialog();
    }
    this.scheduleDialog(expiresAtMs);
  }

  private scheduleDialog(expiresAtMs: number) {
    this.cancelDialogTrigger();

    const leadMs = this.leadSeconds * 1000;
    const delay = Math.max(expiresAtMs - Date.now() - leadMs, 0);

    if (delay === 0) {
      this.startCountdown();
      return;
    }

    this.dialogTriggerSub = timer(delay)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.startCountdown());
  }

  private startCountdown() {
    this.cancelDialogTrigger();
    this.dialog$.next({ visible: true, secondsLeft: this.leadSeconds });
    this.stopCountdown();

    this.countdownSub = interval(1000)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        const next = Math.max(this.dialog$.value.secondsLeft - 1, 0);
        this.dialog$.next({ visible: true, secondsLeft: next });

        if (next === 0) {
          this.exit();
        }
      });
  }

  private stopCountdown() {
    this.countdownSub?.unsubscribe();
    this.countdownSub = undefined;
  }

  private cancelDialogTrigger() {
    this.dialogTriggerSub?.unsubscribe();
    this.dialogTriggerSub = undefined;
  }

  private listenForActivity() {
    if (this.activitySub || typeof document === 'undefined') {
      return;
    }

    const eventTargets = ['click', 'keydown', 'touchstart', 'mousemove', 'scroll']
      .map(event => fromEvent(document, event));

    if (!eventTargets.length) {
      return;
    }

    this.activitySub = merge(...eventTargets)
      .pipe(
        throttleTime(this.activityThrottleMs, undefined, { leading: true, trailing: false }),
        takeUntil(this.destroy$)
      )
      .subscribe(() => this.onUserActivity());
  }

  private onUserActivity() {
    if (!this.currentExpiryMs || this.dialog$.value.visible) {
      return;
    }

    const timeRemaining = this.currentExpiryMs - Date.now();
    if (timeRemaining <= 0) {
      return;
    }

    this.refreshSession('activity');
  }

  private refreshSession(trigger: 'activity' | 'dialog') {
    if (this.refreshInFlight) {
      return;
    }
    this.refreshInFlight = true;

    this.auth
      .refresh()
      .pipe(
        switchMap(() => this.auth.me({ silent: true })),
        map(me => ({ me, success: true as const })),
        catchError(() => of({ success: false as const })),
        takeUntil(this.destroy$)
      )
      .subscribe(result => {
        this.refreshInFlight = false;

        if (!result.success || !result.me?.authenticated) {
          if (trigger === 'dialog') {
            this.exit();
          } else {
            this.startCountdown();
          }
          return;
        }

        if (result.me.expiresAt) {
          this.handleAuthState(result.me);
        }

        if (trigger === 'dialog') {
          this.hideDialog();
        }
      });
  }
}
