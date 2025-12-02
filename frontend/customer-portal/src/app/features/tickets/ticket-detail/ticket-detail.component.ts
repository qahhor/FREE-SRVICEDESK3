import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TicketService } from '../../../core/services/ticket.service';
import { TicketStatusBadgeComponent } from '../../../shared/components/ticket-status-badge/ticket-status-badge.component';
import { Ticket, Comment, Attachment } from '../../../core/models';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    MatSnackBarModule,
    TicketStatusBadgeComponent
  ],
  template: `
    <div class="ticket-detail-container">
      @if (loading) {
        <div class="loading-container">
          <mat-spinner diameter="40"></mat-spinner>
        </div>
      } @else if (!ticket) {
        <mat-card class="error-card">
          <mat-icon>error</mat-icon>
          <h2>Ticket Not Found</h2>
          <p>The ticket you're looking for doesn't exist or you don't have access to it.</p>
          <a mat-flat-button color="primary" routerLink="/tickets">Back to Tickets</a>
        </mat-card>
      } @else {
        <!-- Back link -->
        <a routerLink="/tickets" class="back-link">
          <mat-icon>arrow_back</mat-icon>
          Back to My Tickets
        </a>
        
        <!-- Ticket Header -->
        <mat-card class="ticket-header-card">
          <div class="ticket-header">
            <div class="ticket-info">
              <span class="ticket-number">{{ ticket.ticketNumber }}</span>
              <h1>{{ ticket.subject }}</h1>
            </div>
            <div class="ticket-badges">
              <app-ticket-status-badge [status]="ticket.status"></app-ticket-status-badge>
              <app-ticket-status-badge [priority]="ticket.priority"></app-ticket-status-badge>
            </div>
          </div>
          
          <mat-divider></mat-divider>
          
          <div class="ticket-meta-grid">
            <div class="meta-item">
              <span class="meta-label">Created</span>
              <span class="meta-value">{{ formatDateTime(ticket.createdAt) }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">Updated</span>
              <span class="meta-value">{{ formatDateTime(ticket.updatedAt) }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">Category</span>
              <span class="meta-value">{{ ticket.category || 'N/A' }}</span>
            </div>
            @if (ticket.assignee) {
              <div class="meta-item">
                <span class="meta-label">Assigned To</span>
                <span class="meta-value">{{ ticket.assignee.fullName }}</span>
              </div>
            }
          </div>
          
          @if (ticket.status === 'CLOSED' || ticket.status === 'RESOLVED') {
            <div class="reopen-section">
              <p>Need more help with this issue?</p>
              <button mat-stroked-button color="primary" (click)="reopenTicket()" [disabled]="reopening">
                @if (reopening) {
                  <mat-spinner diameter="18"></mat-spinner>
                } @else {
                  <mat-icon>refresh</mat-icon>
                  Reopen Ticket
                }
              </button>
            </div>
          }
        </mat-card>
        
        <!-- Description -->
        @if (ticket.description) {
          <mat-card class="description-card">
            <h3>Description</h3>
            <p class="description-text">{{ ticket.description }}</p>
          </mat-card>
        }
        
        <!-- Attachments -->
        @if (attachments.length > 0) {
          <mat-card class="attachments-card">
            <h3>Attachments</h3>
            <div class="attachments-list">
              @for (attachment of attachments; track attachment.id) {
                <a [href]="attachment.downloadUrl" target="_blank" class="attachment-item">
                  <mat-icon>{{ attachment.isImage ? 'image' : 'insert_drive_file' }}</mat-icon>
                  <span>{{ attachment.originalFilename }}</span>
                  <span class="file-size">{{ formatFileSize(attachment.size) }}</span>
                  <mat-icon class="download-icon">download</mat-icon>
                </a>
              }
            </div>
          </mat-card>
        }
        
        <!-- Comments/Conversation -->
        <mat-card class="comments-card">
          <h3>Conversation</h3>
          
          @if (comments.length === 0) {
            <p class="no-comments">No messages yet.</p>
          } @else {
            <div class="comments-list">
              @for (comment of comments; track comment.id) {
                <div class="comment" [class.system-comment]="comment.isAutomated">
                  <div class="comment-header">
                    <div class="comment-author">
                      <div class="avatar">{{ getInitials(comment.author) }}</div>
                      <div class="author-info">
                        <span class="author-name">{{ comment.author.fullName }}</span>
                        @if (comment.isAutomated) {
                          <span class="system-badge">System</span>
                        }
                      </div>
                    </div>
                    <span class="comment-date">{{ formatDateTime(comment.createdAt) }}</span>
                  </div>
                  <div class="comment-content">{{ comment.content }}</div>
                </div>
              }
            </div>
          }
          
          @if (ticket.status !== 'CLOSED') {
            <form [formGroup]="replyForm" (ngSubmit)="submitReply()" class="reply-form">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Add a reply</mat-label>
                <textarea matInput formControlName="content" rows="3" placeholder="Type your message..."></textarea>
                @if (replyForm.get('content')?.hasError('required') && replyForm.get('content')?.touched) {
                  <mat-error>Message is required</mat-error>
                }
              </mat-form-field>
              <button mat-flat-button color="primary" type="submit" [disabled]="submittingReply">
                @if (submittingReply) {
                  <mat-spinner diameter="18"></mat-spinner>
                } @else {
                  <mat-icon>send</mat-icon>
                  Send
                }
              </button>
            </form>
          }
        </mat-card>
      }
    </div>
  `,
  styles: [`
    .ticket-detail-container {
      max-width: 900px;
      margin: 32px auto;
      padding: 0 16px;
    }
    
    .loading-container {
      display: flex;
      justify-content: center;
      padding: 64px;
    }
    
    .error-card {
      text-align: center;
      padding: 64px 24px;
    }
    
    .error-card mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #f44336;
    }
    
    .back-link {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      color: #3f51b5;
      text-decoration: none;
      margin-bottom: 16px;
    }
    
    .back-link mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }
    
    mat-card {
      margin-bottom: 16px;
      padding: 24px;
    }
    
    mat-card h3 {
      margin: 0 0 16px;
      font-size: 1rem;
      font-weight: 500;
      color: #666;
    }
    
    .ticket-header-card .ticket-header {
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
    
    .ticket-info h1 {
      margin: 0;
      font-size: 1.5rem;
    }
    
    .ticket-badges {
      display: flex;
      gap: 8px;
    }
    
    .ticket-meta-grid {
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
    
    .reopen-section {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px;
      background: #f5f5f5;
      border-radius: 4px;
      margin-top: 16px;
    }
    
    .reopen-section p {
      margin: 0;
      color: #666;
    }
    
    .description-text {
      margin: 0;
      white-space: pre-wrap;
      line-height: 1.6;
    }
    
    .attachments-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    
    .attachment-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px;
      background: #f5f5f5;
      border-radius: 4px;
      text-decoration: none;
      color: inherit;
      transition: background 0.2s;
    }
    
    .attachment-item:hover {
      background: #e0e0e0;
    }
    
    .attachment-item span {
      flex: 1;
    }
    
    .file-size {
      color: #999;
      font-size: 0.875rem;
    }
    
    .download-icon {
      color: #3f51b5;
    }
    
    .no-comments {
      text-align: center;
      color: #999;
      padding: 24px;
    }
    
    .comments-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
      margin-bottom: 24px;
    }
    
    .comment {
      padding: 16px;
      background: #f5f5f5;
      border-radius: 8px;
    }
    
    .comment.system-comment {
      background: #fff3e0;
    }
    
    .comment-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
    }
    
    .comment-author {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    
    .avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: #3f51b5;
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.875rem;
      font-weight: 500;
    }
    
    .author-name {
      font-weight: 500;
    }
    
    .system-badge {
      font-size: 0.75rem;
      padding: 2px 6px;
      background: #ff9800;
      color: white;
      border-radius: 4px;
      margin-left: 8px;
    }
    
    .comment-date {
      color: #999;
      font-size: 0.875rem;
    }
    
    .comment-content {
      white-space: pre-wrap;
      line-height: 1.6;
    }
    
    .reply-form {
      display: flex;
      gap: 16px;
      align-items: flex-start;
    }
    
    .reply-form mat-form-field {
      flex: 1;
    }
    
    .reply-form button {
      height: 56px;
    }
    
    .full-width {
      width: 100%;
    }
    
    @media (max-width: 600px) {
      .ticket-header-card .ticket-header {
        flex-direction: column;
        gap: 16px;
      }
      
      .reopen-section {
        flex-direction: column;
        gap: 12px;
        text-align: center;
      }
      
      .reply-form {
        flex-direction: column;
      }
      
      .reply-form button {
        width: 100%;
      }
    }
  `]
})
export class TicketDetailComponent implements OnInit {
  ticket: Ticket | null = null;
  comments: Comment[] = [];
  attachments: Attachment[] = [];
  loading = true;
  reopening = false;
  submittingReply = false;
  replyForm: FormGroup;

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private ticketService: TicketService,
    private snackBar: MatSnackBar
  ) {
    this.replyForm = this.fb.group({
      content: ['', Validators.required]
    });
  }

  ngOnInit() {
    const ticketId = this.route.snapshot.paramMap.get('id');
    if (ticketId) {
      this.loadTicket(ticketId);
      this.loadComments(ticketId);
      this.loadAttachments(ticketId);
    }
  }

  loadTicket(ticketId: string) {
    this.ticketService.getTicket(ticketId).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success && response.data) {
          this.ticket = response.data;
        }
      },
      error: () => {
        this.loading = false;
        this.ticket = null;
      }
    });
  }

  loadComments(ticketId: string) {
    this.ticketService.getTicketComments(ticketId).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.comments = response.data.filter(c => !c.isInternal);
        }
      }
    });
  }

  loadAttachments(ticketId: string) {
    this.ticketService.getTicketAttachments(ticketId).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.attachments = response.data;
        }
      }
    });
  }

  reopenTicket() {
    if (!this.ticket) return;
    
    this.reopening = true;
    this.ticketService.reopenTicket(this.ticket.id).subscribe({
      next: (response) => {
        this.reopening = false;
        if (response.success && response.data) {
          this.ticket = response.data;
          this.snackBar.open('Ticket reopened successfully', 'OK', { duration: 3000 });
        }
      },
      error: () => {
        this.reopening = false;
        this.snackBar.open('Failed to reopen ticket', 'OK', { duration: 3000 });
      }
    });
  }

  submitReply() {
    if (!this.ticket || !this.replyForm.valid) return;
    
    this.submittingReply = true;
    const { content } = this.replyForm.value;
    
    this.ticketService.addComment(this.ticket.id, { content }).subscribe({
      next: (response) => {
        this.submittingReply = false;
        if (response.success && response.data) {
          this.comments.push(response.data);
          this.replyForm.reset();
          this.snackBar.open('Reply sent successfully', 'OK', { duration: 3000 });
        }
      },
      error: () => {
        this.submittingReply = false;
        this.snackBar.open('Failed to send reply', 'OK', { duration: 3000 });
      }
    });
  }

  getInitials(user: { firstName?: string; lastName?: string; fullName?: string }): string {
    if (user.firstName && user.lastName) {
      return (user.firstName[0] + user.lastName[0]).toUpperCase();
    }
    if (user.fullName) {
      const parts = user.fullName.split(' ');
      return parts.map(p => p[0]).join('').substring(0, 2).toUpperCase();
    }
    return '?';
  }

  formatDateTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }
}
