import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subject } from 'rxjs';

@Component({
  standalone: true,
  selector: 'mcl-header',
  imports: [CommonModule, RouterLink],
  templateUrl: 'header.component.html',
})
export class HeaderComponent {
  @Input() appTitle = 'App';
  @Input() loginUrl = '';
  @Input() crossAppUrl = '';
  @Input() crossAppLabel = 'Go to Other App';
  @Input() showLogout = true;

  @Output() logout = new EventEmitter<void>();
}
