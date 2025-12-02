import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { TicketService } from '../../../core/services/ticket.service';
import { TicketStatusBadgeComponent } from '../../../shared/components/ticket-status-badge/ticket-status-badge.component';
import { Ticket, Comment } from '../../../core/models';

@Component({
  selector: 'app-track-ticket',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    TicketStatusBadgeComponent
  ],
  template: `
    <div class="track-ticket-container">
      <mat-card class="track-card">
        <mat-card-header>
          <mat-card-title>Track Your Ticket</mat-card-title>
          <mat-card-subtitle>Enter your email and ticket number to view status</mat-card-subtitle>
        </mat-card-header>
        
        <mat-card-content>
          @if (errorMessage) {
            <div class="error-message">
              <mat-icon>error</mat-icon>
              {{ errorMessage }}
            </div>
          }
          
          @if (!ticket) {
            <form [formGroup]="trackForm" (ngSubmit)="onSubmit()">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Email Address</mat-label>
                <input matInput type="email" formControlName="email" placeholder="Enter the email used to submit the ticket">
                <mat-icon matPrefix>email</mat-icon>
                @if (trackForm.get('email')?.hasError('required') && trackForm.get('email')?.touched) {
                  <mat-error>Email is required</mat-error>
                }
                @if (trackForm.get('email')?.hasError('email') && trackForm.get('email')?.touched) {
                  <mat-error>Please enter a valid email</mat-error>
                }
              </mat-form-field>
              
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Ticket Number</mat-label>
                <input matInput formControlName="ticketNumber" placeholder="e.g., DESK-123">
                <mat-icon matPrefix>confirmation_number</mat-icon>
                @if (trackForm.get('ticketNumber')?.hasError('required') && trackForm.get('ticketNumber')?.touched) {
                  <mat-error>Ticket number is required</mat-error>
                }
              </mat-form-field>
              
              <button mat-flat-button color="primary" type="submit" class="full-width submit-btn" [disabled]="loading">
                @if (loading) {
                  <mat-spinner diameter="20"></mat-spinner>
                } @else {
                  <mat-icon>search</mat-icon>
                  Track Ticket
                }
              </button>
            </form>
          } @else {
            <!-- Ticket Found -->
            <div class="ticket-found">
              <button mat-stroked-button (click)="resetSearch()" class="back-btn">
                <mat-icon>arrow_back</mat-icon>
                Search Another Ticket
              </button>
              
              <div class="ticket-header">
                <div class="ticket-info">
                  <span class="ticket-number">{{ ticket.ticketNumber }}</span>
                  <h2>{{ ticket.subject }}</h2>
                </div>
                <div class="ticket-badges">
                  <app-ticket-status-badge [status]="ticket.status"></app-ticket-status-badge>
                  <app-ticket-status-badge [priority]="ticket.priority"></app-ticket-status-badge>
                </div>
              </div>
              
              <mat-divider></mat-divider>
              
              <div class="ticket-meta">
                <div class="meta-item">
                  <span class="meta-label">Created</span>
                  <span class="meta-value">{{ formatDateTime(ticket.createdAt) }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">Last Updated</span>
                  <span class="meta-value">{{ formatDateTime(ticket.updatedAt) }}</span>
                </div>
                @if (ticket.resolvedAt) {
                  <div class="meta-item">
                    <span class="meta-label">Resolved</span>
                    <span class="meta-value">{{ formatDateTime(ticket.resolvedAt) }}</span>
                  </div>
                }
              </div>
              
              @if (ticket.description) {
                <div class="description-section">
                  <h3>Description</h3>
                  <p>{{ ticket.description }}</p>
                </div>
              }
              
              @if (comments.length > 0) {
                <div class="comments-section">
                  <h3>Updates</h3>
                  <div class="comments-list">
                    @for (comment of comments; track comment.id) {
                      <div class="comment">
                        <div class="comment-header">
                          <span class="comment-author">{{ comment.author.fullName }}</span>
                          <span class="comment-date">{{ formatDateTime(comment.createdAt) }}</span>
                        </div>
                        <p class="comment-content">{{ comment.content }}</p>
                      </div>
                    }
                  </div>
                </div>
              }
              
              <div class="login-prompt">
                <p>Want to reply or manage your tickets?</p>
                <a mat-flat-button color="primary" routerLink="/login">Login to Your Account</a>
              </div>
            </div>
          }
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .track-ticket-container {
      max-width: 700px;
      margin: 32px auto;
      padding: 0 16px;
    }
    
    .track-card {
      padding: 24px;
    }
    
    mat-card-header {
      display: block;
      text-align: center;
      margin-bottom: 24px;
    }
    
    mat-card-title {
      font-size: 1.5rem !important;
      margin-bottom: 8px;
    }
    
    .full-width {
      width: 100%;
    }
    
    .error-message {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px;
      margin-bottom: 16px;
      background: #ffebee;
      color: #c62828;
      border-radius: 4px;
    }
    
    .submit-btn {
      height: 48px;
      font-size: 1rem;
      margin-top: 16px;
    }
    
    .back-btn {
      margin-bottom: 24px;
    }
    
    .ticket-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 16px;
    }
    
    .ticket-number {
      font-family: monospace;
      font-size: 0.875rem;
      color: #666;
      display: block;
      margin-bottom: 8px;
    }
    
    .ticket-info h2 {
      margin: 0;
      font-size: 1.25rem;
    }
    
    .ticket-badges {
      display: flex;
      gap: 8px;
    }
    
    .ticket-meta {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 16px;
      padding: 16px 0;
    }
    
    .meta-item {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    
    .meta-label {
      font-size: 0.75rem;
      color: #999;
      text-transform: uppercase;
    }
    
    .meta-value {
      font-size: 0.875rem;
    }
    
    .description-section,
    .comments-section {
      margin-top: 24px;
    }
    
    .description-section h3,
    .comments-section h3 {
      margin: 0 0 12px;
      font-size: 1rem;
      font-weight: 500;
      color: #666;
    }
    
    .description-section p {
      margin: 0;
      white-space: pre-wrap;
    }
    
    .comments-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    
    .comment {
      padding: 12px;
      background: #f5f5f5;
      border-radius: 8px;
    }
    
    .comment-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
    }
    
    .comment-author {
      font-weight: 500;
    }
    
    .comment-date {
      color: #999;
      font-size: 0.875rem;
    }
    
    .comment-content {
      margin: 0;
      white-space: pre-wrap;
    }
    
    .login-prompt {
      margin-top: 32px;
      padding: 24px;
      background: #e8eaf6;
      border-radius: 8px;
      text-align: center;
    }
    
    .login-prompt p {
      margin: 0 0 16px;
      color: #666;
    }
    
    @media (max-width: 600px) {
      .ticket-header {
        flex-direction: column;
        gap: 12px;
      }
    }
  `]
})
export class TrackTicketComponent {
  trackForm: FormGroup;
  loading = false;
  errorMessage = '';
  ticket: Ticket | null = null;
  comments: Comment[] = [];

  constructor(
    private fb: FormBuilder,
    private ticketService: TicketService
  ) {
    this.trackForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      ticketNumber: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.trackForm.valid) {
      this.loading = true;
      this.errorMessage = '';
      
      const { email, ticketNumber } = this.trackForm.value;
      
      this.ticketService.trackTicket({ email, ticketNumber }).subscribe({
        next: (response) => {
          this.loading = false;
          if (response.success && response.data) {
            this.ticket = response.data.ticket;
            this.comments = response.data.comments.filter(c => !c.isInternal);
          } else {
            this.errorMessage = response.error || 'Ticket not found';
          }
        },
        error: (error) => {
          this.loading = false;
          this.errorMessage = error.error?.error || 'Ticket not found. Please check your email and ticket number.';
        }
      });
    }
  }

  resetSearch() {
    this.ticket = null;
    this.comments = [];
    this.trackForm.reset();
  }

  formatDateTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }
}
