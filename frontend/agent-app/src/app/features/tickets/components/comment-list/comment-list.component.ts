import { Component, Input, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Comment } from '../../../../core/models/comment.model';
import { CommentService } from '../../../../core/services/comment.service';
import { WebSocketService } from '../../../../core/services/websocket.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-comment-list',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatChipsModule,
    MatDividerModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './comment-list.component.html',
  styleUrl: './comment-list.component.scss'
})
export class CommentListComponent implements OnInit, OnDestroy {
  @Input() ticketId!: string;
  @Input() includeInternal: boolean = true;

  private readonly commentService = inject(CommentService);
  private readonly websocketService = inject(WebSocketService);

  comments = signal<Comment[]>([]);
  loading = signal<boolean>(false);
  error = signal<string | null>(null);

  private eventSubscription?: Subscription;

  ngOnInit(): void {
    this.loadComments();
    this.subscribeToWebSocketEvents();
  }

  ngOnDestroy(): void {
    this.eventSubscription?.unsubscribe();
  }

  loadComments(): void {
    this.loading.set(true);
    this.error.set(null);

    this.commentService.getComments(this.ticketId, this.includeInternal).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.comments.set(response.data);
        }
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading comments:', err);
        this.error.set('Failed to load comments');
        this.loading.set(false);
      }
    });
  }

  private subscribeToWebSocketEvents(): void {
    this.eventSubscription = this.websocketService.getTicketEvents().subscribe({
      next: (event) => {
        // Reload comments if this ticket received a comment event
        if (event.type === 'COMMENTED' && event.ticketId === this.ticketId) {
          this.loadComments();
        }
      }
    });
  }

  getAuthorInitials(comment: Comment): string {
    if (comment.author?.fullName) {
      const names = comment.author.fullName.split(' ');
      return names.map(n => n[0]).join('').toUpperCase().substring(0, 2);
    }
    return comment.author?.username?.substring(0, 2).toUpperCase() || '??';
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins} minutes ago`;
    if (diffHours < 24) return `${diffHours} hours ago`;
    if (diffDays < 7) return `${diffDays} days ago`;

    return date.toLocaleDateString();
  }
}
