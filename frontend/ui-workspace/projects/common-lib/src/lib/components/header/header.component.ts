import { CommonModule } from '@angular/common';
import { Component, EventEmitter, HostListener, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthStateService } from '../../services/auth-state.service';

@Component({
  standalone: true,
  selector: 'mcl-header',
  imports: [CommonModule, RouterLink],
  templateUrl: 'header.component.html',
  styleUrls: ['header.component.css'],
})
export class HeaderComponent implements OnInit, OnDestroy {
  @Input() appTitle = 'App';
  @Input() loginUrl = '';
  @Input() crossAppUrl = '';
  @Input() crossAppLabel = 'Go to Other App';

  @Output() logout = new EventEmitter<void>();

  isLoggedIn = false;
  username = '';
  menuOpen = false;

  private readonly desktopBreakpoint = 992;
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

  toggleMenu(): void {
    if (this.isDesktopViewport()) {
      return;
    }
    this.menuOpen = !this.menuOpen;
  }

  onLogoutClick(): void {
    this.closeMenuForMobile();
    this.logout.emit();
  }

  onNavAction(): void {
    this.closeMenuForMobile();
  }

  @HostListener('window:resize')
  onWindowResize(): void {
    if (this.isDesktopViewport() && this.menuOpen) {
      this.menuOpen = false;
    }
  }

  private closeMenuForMobile(): void {
    if (!this.isDesktopViewport()) {
      this.menuOpen = false;
    }
  }

  private isDesktopViewport(): boolean {
    return typeof window !== 'undefined' && window.innerWidth >= this.desktopBreakpoint;
  }
}
