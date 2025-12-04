import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

import { AdminService } from '../../services/admin.service';
import { AdminTeam, TeamFilter } from '../../services/admin.models';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-team-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatMenuModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatDialogModule
  ],
  templateUrl: './team-list.component.html',
  styleUrls: ['./team-list.component.scss']
})
export class TeamListComponent implements OnInit {
  dataSource = new MatTableDataSource<AdminTeam>([]);
  loading = signal(true);
  totalElements = signal(0);
  pageSize = 20;
  pageIndex = 0;

  displayedColumns: string[] = ['name', 'description', 'membersCount', 'manager', 'createdAt', 'actions'];

  searchQuery = '';

  constructor(
    private adminService: AdminService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadTeams();
  }

  loadTeams(): void {
    this.loading.set(true);

    const filter: TeamFilter = {
      search: this.searchQuery || undefined
    };

    this.adminService.getTeams(this.pageIndex, this.pageSize, filter).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.dataSource.data = response.data.content;
          this.totalElements.set(response.data.totalElements);
        }
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading teams:', error);
        this.snackBar.open('Failed to load teams', 'Close', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadTeams();
  }

  applyFilter(): void {
    this.pageIndex = 0;
    this.loadTeams();
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.pageIndex = 0;
    this.loadTeams();
  }

  deleteTeam(team: AdminTeam): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Team',
        message: `Are you sure you want to delete "${team.name}"? This will remove all team memberships.`,
        confirmText: 'Delete',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.adminService.deleteTeam(team.id).subscribe({
          next: () => {
            this.snackBar.open('Team deleted successfully', 'Close', { duration: 3000 });
            this.loadTeams();
          },
          error: () => {
            this.snackBar.open('Failed to delete team', 'Close', { duration: 3000 });
          }
        });
      }
    });
  }
}
