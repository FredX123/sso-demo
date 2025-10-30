import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { AuthMe } from './../model/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthStateService {

  private readonly _me$ = new BehaviorSubject<AuthMe>({ authenticated: false });

  get me$(): Observable<AuthMe> {
    return this._me$.asObservable();
  }

  get snapshot(): AuthMe {
    return this._me$.value;
  }

  update(me: AuthMe) {
    this._me$.next(me);
  }

  clear() {
    this._me$.next({ authenticated: false });
  }
}
