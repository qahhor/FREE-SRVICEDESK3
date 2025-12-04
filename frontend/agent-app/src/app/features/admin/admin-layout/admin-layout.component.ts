import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';

import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatToolbarModule,
    MatButtonModule,
    MatMenuModule
  ],
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.scss']
})
export class AdminLayoutComponent {
  sidenavOpened = true;

  navItems = [
    { path: '/admin/users', icon: 'people', label: 'Users' },
    { path: '/admin/teams', icon: 'groups', label: 'Teams' },
    { path: '/admin/projects', icon: 'folder', label: 'Projects' },
    { path: '/admin/settings', icon: 'settings', label: 'Settings' }
  ];

  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  toggleSidenav(): void {
    this.sidenavOpened = !this.sidenavOpened;
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  logout(): void {
    this.authService.logout();
  }
}
