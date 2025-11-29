import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatButtonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  stats = [
    { title: 'Open Tickets', value: 0, icon: 'confirmation_number', color: '#3f51b5' },
    { title: 'Assigned to Me', value: 0, icon: 'person', color: '#ff9800' },
    { title: 'Resolved Today', value: 0, icon: 'check_circle', color: '#4caf50' },
    { title: 'Overdue', value: 0, icon: 'warning', color: '#f44336' }
  ];

  constructor(private router: Router) {}

  ngOnInit(): void {
    // TODO: Load dashboard stats from API
    this.stats = [
      { title: 'Open Tickets', value: 24, icon: 'confirmation_number', color: '#3f51b5' },
      { title: 'Assigned to Me', value: 8, icon: 'person', color: '#ff9800' },
      { title: 'Resolved Today', value: 12, icon: 'check_circle', color: '#4caf50' },
      { title: 'Overdue', value: 3, icon: 'warning', color: '#f44336' }
    ];
  }

  navigateToTickets(): void {
    this.router.navigate(['/tickets']);
  }
}
