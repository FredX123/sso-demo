import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { NgIf } from '@angular/common';
import { DialogService } from '../../services/dialog.service';

@Component({
  standalone: true,
  selector: 'mcl-dialog',
  imports: [NgIf],
  templateUrl: './dialog.component.html'
})
export class DialogComponent implements OnInit, OnDestroy {
  visible = false;
  title = 'Notice';
  message = '';
  private sub?: Subscription;

  constructor(private dialog: DialogService) {}

  ngOnInit(): void {
    this.sub = this.dialog.state$.subscribe(s => {
      this.visible = s.visible;
      this.title = s.title ?? 'Notice';
      this.message = s.message ?? '';
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  close() {
    this.dialog.close();
  }
}
