import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TicketService } from '../../../core/services/ticket.service';
import { TicketStatusBadgeComponent } from '../../../shared/components/ticket-status-badge/ticket-status-badge.component';
import { Ticket, Page } from '../../../core/models';

@Component({
  selector: 'app-my-tickets',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule,
    TicketStatusBadgeComponent
  ],
  template: `
    <div class="my-tickets-container">
      <div class="header">
        <h1>My Tickets</h1>
        <a mat-flat-button color="primary" routerLink="/tickets/submit">
          <mat-icon>add</mat-icon>
          New Ticket
        </a>
      </div>
      
      <!-- Filters -->
      <mat-card class="filters-card">
        <div class="filters">
          <mat-form-field appearance="outline">
            <mat-label>Search</mat-label>
            <input matInput [(ngModel)]="searchQuery" (keyup.enter)="applyFilters()" placeholder="Search by subject or ticket number">
            <mat-icon matPrefix>search</mat-icon>
          </mat-form-field>
          
          <mat-form-field appearance="outline">
            <mat-label>Status</mat-label>
            <mat-select [(ngModel)]="statusFilter" (selectionChange)="applyFilters()">
              <mat-option value="">All Statuses</mat-option>
              <mat-option value="NEW">New</mat-option>
              <mat-option value="OPEN">Open</mat-option>
              <mat-option value="IN_PROGRESS">In Progress</mat-option>
              <mat-option value="PENDING">Pending</mat-option>
              <mat-option value="RESOLVED">Resolved</mat-option>
              <mat-option value="CLOSED">Closed</mat-option>
            </mat-select>
          </mat-form-field>
          
          <button mat-stroked-button (click)="clearFilters()">
            <mat-icon>clear</mat-icon>
            Clear
          </button>
        </div>
      </mat-card>
      
      <!-- Tickets List -->
      @if (loading) {
        <div class="loading-container">
          <mat-spinner diameter="40"></mat-spinner>
        </div>
      } @else if (tickets.length === 0) {
        <mat-card class="empty-state">
          <mat-icon>inbox</mat-icon>
          <h2>No tickets found</h2>
          <p>You haven't submitted any tickets yet, or no tickets match your filters.</p>
          <a mat-flat-button color="primary" routerLink="/tickets/submit">Submit Your First Ticket</a>
        </mat-card>
      } @else {
        <div class="tickets-list">
          @for (ticket of tickets; track ticket.id) {
            <mat-card class="ticket-card" [routerLink]="['/tickets', ticket.id]">
              <div class="ticket-header">
                <span class="ticket-number">{{ ticket.ticketNumber }}</span>
                <app-ticket-status-badge [status]="ticket.status"></app-ticket-status-badge>
              </div>
              <h3 class="ticket-subject">{{ ticket.subject }}</h3>
              <div class="ticket-meta">
                <span>
                  <mat-icon>schedule</mat-icon>
                  {{ formatDate(ticket.createdAt) }}
                </span>
                <app-ticket-status-badge [priority]="ticket.priority"></app-ticket-status-badge>
              </div>
              @if (ticket.description) {
                <p class="ticket-description">{{ ticket.description | slice:0:150 }}{{ ticket.description.length > 150 ? '...' : '' }}</p>
              }
            </mat-card>
          }
        </div>
        
        <mat-paginator
          [length]="totalElements"
          [pageSize]="pageSize"
          [pageIndex]="currentPage"
          [pageSizeOptions]="[5, 10, 25]"
          (page)="onPageChange($event)"
          showFirstLastButtons>
        </mat-paginator>
      }
    </div>
  `,
  styles: [`
    .my-tickets-container {
      max-width: 1000px;
      margin: 32px auto;
      padding: 0 16px;
    }
    
    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }
    
    .header h1 {
      margin: 0;
    }
    
    .filters-card {
      margin-bottom: 24px;
      padding: 16px;
    }
    
    .filters {
      display: flex;
      gap: 16px;
      align-items: center;
      flex-wrap: wrap;
    }
    
    .filters mat-form-field {
      flex: 1;
      min-width: 200px;
    }
    
    .filters mat-form-field ::ng-deep .mat-mdc-form-field-subscript-wrapper {
      display: none;
    }
    
    .loading-container {
      display: flex;
      justify-content: center;
      padding: 64px;
    }
    
    .empty-state {
      text-align: center;
      padding: 64px 24px;
    }
    
    .empty-state mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #ccc;
    }
    
    .empty-state h2 {
      margin: 16px 0 8px;
      color: #666;
    }
    
    .empty-state p {
      margin: 0 0 24px;
      color: #999;
    }
    
    .tickets-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
      margin-bottom: 24px;
    }
    
    .ticket-card {
      padding: 20px;
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;
    }
    
    .ticket-card:hover {
      transform: translateX(4px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    .ticket-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
    }
    
    .ticket-number {
      font-family: monospace;
      font-size: 0.875rem;
      color: #666;
    }
    
    .ticket-subject {
      margin: 0 0 12px;
      font-size: 1.1rem;
    }
    
    .ticket-meta {
      display: flex;
      gap: 16px;
      align-items: center;
      margin-bottom: 8px;
    }
    
    .ticket-meta span {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #666;
      font-size: 0.875rem;
    }
    
    .ticket-meta mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }
    
    .ticket-description {
      margin: 0;
      color: #666;
      font-size: 0.875rem;
    }
    
    @media (max-width: 600px) {
      .header {
        flex-direction: column;
        align-items: stretch;
        gap: 16px;
      }
      
      .filters {
        flex-direction: column;
      }
      
      .filters mat-form-field {
        width: 100%;
      }
    }
  `]
})
export class MyTicketsComponent implements OnInit {
  tickets: Ticket[] = [];
  loading = true;
  searchQuery = '';
  statusFilter = '';
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;

  constructor(private ticketService: TicketService) {}

  ngOnInit() {
    this.loadTickets();
  }

  loadTickets() {
    this.loading = true;
    this.ticketService.getMyTickets(this.currentPage, this.pageSize, this.statusFilter, this.searchQuery).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success && response.data) {
          this.tickets = response.data.content;
          this.totalElements = response.data.totalElements;
        }
      },
      error: () => {
        this.loading = false;
        this.tickets = [];
      }
    });
  }

  applyFilters() {
    this.currentPage = 0;
    this.loadTickets();
  }

  clearFilters() {
    this.searchQuery = '';
    this.statusFilter = '';
    this.currentPage = 0;
    this.loadTickets();
  }

  onPageChange(event: PageEvent) {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadTickets();
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    
    if (days === 0) {
      const hours = Math.floor(diff / (1000 * 60 * 60));
      if (hours === 0) {
        const minutes = Math.floor(diff / (1000 * 60));
        return minutes <= 1 ? 'Just now' : `${minutes} minutes ago`;
      }
      return hours === 1 ? '1 hour ago' : `${hours} hours ago`;
    }
    if (days === 1) return 'Yesterday';
    if (days < 7) return `${days} days ago`;
    
    return date.toLocaleDateString();
  }
}
