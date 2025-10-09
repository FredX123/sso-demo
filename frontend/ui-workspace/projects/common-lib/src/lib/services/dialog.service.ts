import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface DialogState {
  visible: boolean;
  title?: string;
  message?: string;
}

@Injectable({ providedIn: 'root' })
export class DialogService {
  readonly state$ = new BehaviorSubject<DialogState>({ visible: false });

  open(message: string, title = 'Notice') {
    this.state$.next({ visible: true, title, message });
  }

  close() {
    this.state$.next({ visible: false });
  }
}
