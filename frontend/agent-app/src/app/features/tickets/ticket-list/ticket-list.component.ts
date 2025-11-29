import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { TicketService } from '../../../core/services/ticket.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { Ticket } from '../../../core/models/ticket.model';

@Component({
  selector: 'app-ticket-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatPaginatorModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './ticket-list.component.html',
  styleUrls: ['./ticket-list.component.scss']
})
export class TicketListComponent implements OnInit {
  tickets = signal<Ticket[]>([]);
  loading = signal(true);
  totalElements = signal(0);
  pageSize = 20;
  pageIndex = 0;

  displayedColumns: string[] = ['ticketNumber', 'subject', 'status', 'priority', 'requester', 'assignee', 'createdAt', 'actions'];

  constructor(
    private ticketService: TicketService,
    private websocketService: WebSocketService
  ) {}

  ngOnInit(): void {
    this.loadTickets();
    this.subscribeToWebSocketEvents();
  }

  loadTickets(): void {
    this.loading.set(true);

    this.ticketService.getTickets(this.pageIndex, this.pageSize).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.tickets.set(response.data.content);
          this.totalElements.set(response.data.totalElements);
        }
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading tickets:', error);
        this.loading.set(false);
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadTickets();
  }

  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      'NEW': 'primary',
      'OPEN': 'accent',
      'IN_PROGRESS': 'warn',
      'RESOLVED': 'success',
      'CLOSED': 'default'
    };
    return colors[status] || 'default';
  }

  getPriorityColor(priority: string): string {
    const colors: Record<string, string> = {
      'LOW': 'default',
      'MEDIUM': 'primary',
      'HIGH': 'accent',
      'URGENT': 'warn',
      'CRITICAL': 'warn'
    };
    return colors[priority] || 'default';
  }

  private subscribeToWebSocketEvents(): void {
    this.websocketService.getTicketEvents().subscribe({
      next: (event) => {
        console.log('Received ticket event:', event);
        // Reload tickets when a new event is received
        this.loadTickets();
      },
      error: (error) => {
        console.error('WebSocket error:', error);
      }
    });
  }
}
