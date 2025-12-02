import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';
import { TicketStatus, TicketPriority } from '../../../core/models';

@Component({
  selector: 'app-ticket-status-badge',
  standalone: true,
  imports: [CommonModule, MatChipsModule],
  template: `
    @if (status) {
      <span class="badge status-badge" [class]="'status-' + status.toLowerCase()">
        {{ formatStatus(status) }}
      </span>
    }
    @if (priority) {
      <span class="badge priority-badge" [class]="'priority-' + priority.toLowerCase()">
        {{ priority }}
      </span>
    }
  `,
  styles: [`
    .badge {
      display: inline-block;
      padding: 4px 12px;
      border-radius: 16px;
      font-size: 0.75rem;
      font-weight: 500;
      text-transform: uppercase;
    }
    
    /* Status colors */
    .status-new { background: #E3F2FD; color: #1565C0; }
    .status-open { background: #FFF3E0; color: #E65100; }
    .status-in_progress { background: #F3E5F5; color: #6A1B9A; }
    .status-pending { background: #FFFDE7; color: #F57F17; }
    .status-resolved { background: #E8F5E9; color: #2E7D32; }
    .status-closed { background: #ECEFF1; color: #455A64; }
    .status-reopened { background: #FCE4EC; color: #AD1457; }
    .status-on_hold { background: #E0E0E0; color: #424242; }
    
    /* Priority colors */
    .priority-low { background: #E8F5E9; color: #2E7D32; }
    .priority-medium { background: #E3F2FD; color: #1565C0; }
    .priority-high { background: #FFF3E0; color: #E65100; }
    .priority-urgent { background: #FFEBEE; color: #C62828; }
    .priority-critical { background: #F3E5F5; color: #6A1B9A; }
  `]
})
export class TicketStatusBadgeComponent {
  @Input() status?: TicketStatus;
  @Input() priority?: TicketPriority;

  formatStatus(status: string): string {
    return status.replace(/_/g, ' ');
  }
}
