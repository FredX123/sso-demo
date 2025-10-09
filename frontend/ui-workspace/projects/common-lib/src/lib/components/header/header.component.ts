import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthStateService } from '../../services/auth-state.service';

@Component({
  standalone: true,
  selector: 'mcl-header',
  imports: [CommonModule, RouterLink],
  templateUrl: 'header.component.html',
})
export class HeaderComponent implements OnInit, OnDestroy {
  @Input() appTitle = 'App';
  @Input() loginUrl = '';
  @Input() crossAppUrl = '';
  @Input() crossAppLabel = 'Go to Other App';

  @Output() logout = new EventEmitter<void>();

  isLoggedIn = false;
  username = '';
  private sub?: Subscription;

  constructor(private authState: AuthStateService) {}

  ngOnInit(): void {
      this.sub = this.authState.me$.subscribe(me => {
        console.log("authenticated: ", me.authenticated);

        this.isLoggedIn = !!me.authenticated;
        this.username = me.name || me.email || 'N/A';
      })
  }

  ngOnDestroy(): void {
      this.sub?.unsubscribe();
  }
}
