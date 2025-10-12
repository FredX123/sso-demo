import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgIf } from '@angular/common';
import { Subscription } from 'rxjs';
import { SessionTimerService } from '../../services/session-timer.service';

@Component({
  standalone: true,
  selector: 'mcl-session-dialog',
  imports: [NgIf],
  templateUrl: './session-dialog.component.html',
  styleUrls: ['./session-dialog.component.css']
})
export class SessionDialogComponent implements OnInit, OnDestroy {

  visible = false;
  secondsLeft = 60;
  private sub?: Subscription;

  constructor(private timer: SessionTimerService) {}

  ngOnInit(): void {
    this.sub = this.timer.dialog$.subscribe(s => {
      this.visible = s.visible;
      this.secondsLeft = s.secondsLeft;
    });
  }
  
  ngOnDestroy(): void { 
    this.sub?.unsubscribe(); 
  }

  onContinue() { 
    this.timer.continue(); 
  }

  onExit()     { 
    this.timer.exit(); 
  }
}
