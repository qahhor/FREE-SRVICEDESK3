import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule
  ],
  template: `
    <mat-toolbar color="primary" class="header">
      <a routerLink="/" class="logo">
        <mat-icon>support_agent</mat-icon>
        <span>Service Desk</span>
      </a>
      
      <nav class="nav-links">
        <a mat-button routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}">
          Home
        </a>
        <a mat-button routerLink="/kb" routerLinkActive="active">
          Knowledge Base
        </a>
        <a mat-button routerLink="/track" routerLinkActive="active">
          Track Ticket
        </a>
      </nav>
      
      <span class="spacer"></span>
      
      @if (authService.isAuthenticated()) {
        <a mat-button routerLink="/tickets/submit" class="submit-btn">
          <mat-icon>add</mat-icon>
          Submit Ticket
        </a>
        
        <button mat-icon-button [matMenuTriggerFor]="userMenu">
          <mat-icon>account_circle</mat-icon>
        </button>
        
        <mat-menu #userMenu="matMenu">
          <div class="menu-header">
            <strong>{{ authService.currentUser()?.fullName }}</strong>
            <small>{{ authService.currentUser()?.email }}</small>
          </div>
          <mat-divider></mat-divider>
          <a mat-menu-item routerLink="/tickets">
            <mat-icon>list</mat-icon>
            My Tickets
          </a>
          <a mat-menu-item routerLink="/profile">
            <mat-icon>person</mat-icon>
            Profile
          </a>
          <mat-divider></mat-divider>
          <button mat-menu-item (click)="authService.logout()">
            <mat-icon>logout</mat-icon>
            Logout
          </button>
        </mat-menu>
      } @else {
        <a mat-button routerLink="/login">
          Login
        </a>
        <a mat-flat-button color="accent" routerLink="/register">
          Sign Up
        </a>
      }
    </mat-toolbar>
  `,
  styles: [`
    .header {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      z-index: 1000;
    }
    
    .logo {
      display: flex;
      align-items: center;
      gap: 8px;
      color: white;
      text-decoration: none;
      font-weight: 500;
      font-size: 1.25rem;
    }
    
    .nav-links {
      margin-left: 32px;
      display: flex;
      gap: 8px;
    }
    
    .nav-links a {
      color: rgba(255, 255, 255, 0.9);
    }
    
    .nav-links a.active {
      background: rgba(255, 255, 255, 0.1);
    }
    
    .spacer {
      flex: 1;
    }
    
    .submit-btn {
      margin-right: 8px;
    }
    
    .menu-header {
      padding: 12px 16px;
      display: flex;
      flex-direction: column;
    }
    
    .menu-header small {
      color: #666;
      font-size: 0.875rem;
    }
    
    @media (max-width: 768px) {
      .nav-links {
        display: none;
      }
    }
  `]
})
export class HeaderComponent {
  constructor(public authService: AuthService) {}
}
