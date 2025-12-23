import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../../core/services/auth.service';
import { TicketService } from '../../../core/services/ticket.service';
import { User } from '../../../core/models';

@Component({
  selector: 'app-profile-view',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDividerModule
  ],
  template: `
    <div class="profile-container">
      <div class="profile-header">
        <h1>My Profile</h1>
        <a mat-stroked-button routerLink="/profile/edit">
          <mat-icon>edit</mat-icon>
          Edit Profile
        </a>
      </div>

      @if (user) {
        <div class="profile-content">
          <!-- Profile Info Card -->
          <mat-card class="profile-card">
            <div class="avatar">
              {{ getInitials() }}
            </div>
            <h2>{{ user.fullName }}</h2>
            <p class="email">{{ user.email }}</p>
            
            <mat-divider></mat-divider>
            
            <div class="info-grid">
              <div class="info-item">
                <mat-icon>email</mat-icon>
                <div>
                  <span class="label">Email</span>
                  <span class="value">{{ user.email }}</span>
                </div>
              </div>
              
              <div class="info-item">
                <mat-icon>phone</mat-icon>
                <div>
                  <span class="label">Phone</span>
                  <span class="value">{{ user.phone || 'Not set' }}</span>
                </div>
              </div>
              
              <div class="info-item">
                <mat-icon>language</mat-icon>
                <div>
                  <span class="label">Language</span>
                  <span class="value">{{ getLanguageName(user.language) }}</span>
                </div>
              </div>
              
              <div class="info-item">
                <mat-icon>schedule</mat-icon>
                <div>
                  <span class="label">Timezone</span>
                  <span class="value">{{ user.timezone || 'UTC' }}</span>
                </div>
              </div>
            </div>
          </mat-card>

          <!-- Ticket Statistics -->
          <mat-card class="stats-card">
            <h3>Ticket Statistics</h3>
            
            @if (loadingStats) {
              <div class="loading-stats">
                <mat-spinner diameter="32"></mat-spinner>
              </div>
            } @else {
              <div class="stats-grid">
                <div class="stat-item">
                  <span class="stat-value">{{ stats.total }}</span>
                  <span class="stat-label">Total Tickets</span>
                </div>
                <div class="stat-item open">
                  <span class="stat-value">{{ stats.open }}</span>
                  <span class="stat-label">Open</span>
                </div>
                <div class="stat-item resolved">
                  <span class="stat-value">{{ stats.resolved }}</span>
                  <span class="stat-label">Resolved</span>
                </div>
                <div class="stat-item closed">
                  <span class="stat-value">{{ stats.closed }}</span>
                  <span class="stat-label">Closed</span>
                </div>
              </div>
            }
            
            <a mat-stroked-button routerLink="/tickets" class="view-tickets-btn">
              View All Tickets
            </a>
          </mat-card>

          <!-- Quick Actions -->
          <mat-card class="actions-card">
            <h3>Quick Actions</h3>
            <div class="actions-list">
              <a routerLink="/tickets/submit" class="action-item">
                <mat-icon>add_circle</mat-icon>
                <span>Submit New Ticket</span>
                <mat-icon class="arrow">chevron_right</mat-icon>
              </a>
              <a routerLink="/tickets" class="action-item">
                <mat-icon>list</mat-icon>
                <span>View My Tickets</span>
                <mat-icon class="arrow">chevron_right</mat-icon>
              </a>
              <a routerLink="/kb" class="action-item">
                <mat-icon>menu_book</mat-icon>
                <span>Browse Knowledge Base</span>
                <mat-icon class="arrow">chevron_right</mat-icon>
              </a>
              <a routerLink="/profile/edit" class="action-item">
                <mat-icon>settings</mat-icon>
                <span>Account Settings</span>
                <mat-icon class="arrow">chevron_right</mat-icon>
              </a>
            </div>
          </mat-card>
        </div>
      }
    </div>
  `,
  styles: [`
    .profile-container {
      max-width: 1000px;
      margin: 32px auto;
      padding: 0 16px;
    }
    
    .profile-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }
    
    .profile-header h1 {
      margin: 0;
    }
    
    .profile-content {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 24px;
    }
    
    .profile-card {
      grid-column: 1 / 2;
      grid-row: 1 / 3;
      padding: 32px;
      text-align: center;
    }
    
    .avatar {
      width: 100px;
      height: 100px;
      border-radius: 50%;
      background: linear-gradient(135deg, #3f51b5 0%, #303f9f 100%);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 2.5rem;
      font-weight: 500;
      margin: 0 auto 16px;
    }
    
    .profile-card h2 {
      margin: 0 0 8px;
      font-size: 1.5rem;
    }
    
    .profile-card .email {
      color: #666;
      margin: 0 0 24px;
    }
    
    .info-grid {
      display: grid;
      gap: 16px;
      text-align: left;
      margin-top: 24px;
    }
    
    .info-item {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    
    .info-item mat-icon {
      color: #666;
    }
    
    .info-item .label {
      display: block;
      font-size: 0.75rem;
      color: #999;
      text-transform: uppercase;
    }
    
    .info-item .value {
      display: block;
      font-size: 0.875rem;
    }
    
    .stats-card,
    .actions-card {
      padding: 24px;
    }
    
    .stats-card h3,
    .actions-card h3 {
      margin: 0 0 20px;
      font-size: 1rem;
      font-weight: 500;
    }
    
    .loading-stats {
      display: flex;
      justify-content: center;
      padding: 24px;
    }
    
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;
      margin-bottom: 20px;
    }
    
    .stat-item {
      text-align: center;
      padding: 16px;
      background: #f5f5f5;
      border-radius: 8px;
    }
    
    .stat-item.open {
      background: #fff3e0;
    }
    
    .stat-item.resolved {
      background: #e8f5e9;
    }
    
    .stat-item.closed {
      background: #eceff1;
    }
    
    .stat-value {
      display: block;
      font-size: 2rem;
      font-weight: 600;
      color: #333;
    }
    
    .stat-label {
      display: block;
      font-size: 0.75rem;
      color: #666;
      text-transform: uppercase;
    }
    
    .view-tickets-btn {
      width: 100%;
    }
    
    .actions-list {
      display: flex;
      flex-direction: column;
    }
    
    .action-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      text-decoration: none;
      color: inherit;
      border-radius: 8px;
      transition: background 0.2s;
    }
    
    .action-item:hover {
      background: #f5f5f5;
    }
    
    .action-item mat-icon {
      color: #3f51b5;
    }
    
    .action-item span {
      flex: 1;
    }
    
    .action-item .arrow {
      color: #ccc;
    }
    
    @media (max-width: 768px) {
      .profile-content {
        grid-template-columns: 1fr;
      }
      
      .profile-card {
        grid-column: auto;
        grid-row: auto;
      }
    }
  `]
})
export class ProfileViewComponent implements OnInit {
  user: User | null = null;
  loadingStats = true;
  stats = {
    total: 0,
    open: 0,
    resolved: 0,
    closed: 0
  };

  constructor(
    private authService: AuthService,
    private ticketService: TicketService
  ) {}

  ngOnInit() {
    this.user = this.authService.currentUser();
    this.loadStats();
  }

  loadStats() {
    this.loadingStats = true;
    
    // Load all tickets to calculate stats
    this.ticketService.getMyTickets(0, 1000).subscribe({
      next: (response) => {
        this.loadingStats = false;
        if (response.success && response.data) {
          const tickets = response.data.content;
          this.stats.total = tickets.length;
          this.stats.open = tickets.filter(t => ['NEW', 'OPEN', 'IN_PROGRESS', 'PENDING'].includes(t.status)).length;
          this.stats.resolved = tickets.filter(t => t.status === 'RESOLVED').length;
          this.stats.closed = tickets.filter(t => t.status === 'CLOSED').length;
        }
      },
      error: () => {
        this.loadingStats = false;
      }
    });
  }

  getInitials(): string {
    if (this.user?.firstName && this.user?.lastName) {
      return (this.user.firstName[0] + this.user.lastName[0]).toUpperCase();
    }
    return '?';
  }

  getLanguageName(code?: string): string {
    const languages: Record<string, string> = {
      en: 'English',
      es: 'Spanish',
      fr: 'French',
      de: 'German',
      ru: 'Russian',
      uz: 'Uzbek'
    };
    return languages[code || 'en'] || 'English';
  }
}
