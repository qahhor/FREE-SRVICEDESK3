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
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

import { AdminService } from '../../services/admin.service';
import { AdminProject, ProjectFilter } from '../../services/admin.models';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-project-list',
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
    MatSelectModule,
    MatChipsModule,
    MatMenuModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatDialogModule
  ],
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.scss']
})
export class ProjectListComponent implements OnInit {
  dataSource = new MatTableDataSource<AdminProject>([]);
  loading = signal(true);
  totalElements = signal(0);
  pageSize = 20;
  pageIndex = 0;

  displayedColumns: string[] = ['key', 'name', 'description', 'defaultTeam', 'ticketsCount', 'status', 'actions'];

  searchQuery = '';
  selectedStatus: boolean | undefined;

  statusOptions = [
    { value: undefined, label: 'All' },
    { value: true, label: 'Active' },
    { value: false, label: 'Archived' }
  ];

  constructor(
    private adminService: AdminService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.loading.set(true);

    const filter: ProjectFilter = {
      search: this.searchQuery || undefined,
      active: this.selectedStatus
    };

    this.adminService.getProjects(this.pageIndex, this.pageSize, filter).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.dataSource.data = response.data.content;
          this.totalElements.set(response.data.totalElements);
        }
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading projects:', error);
        this.snackBar.open('Failed to load projects', 'Close', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadProjects();
  }

  applyFilter(): void {
    this.pageIndex = 0;
    this.loadProjects();
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.selectedStatus = undefined;
    this.pageIndex = 0;
    this.loadProjects();
  }

  archiveProject(project: AdminProject): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: project.active ? 'Archive Project' : 'Restore Project',
        message: `Are you sure you want to ${project.active ? 'archive' : 'restore'} "${project.name}"?`,
        confirmText: project.active ? 'Archive' : 'Restore',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.adminService.updateProject(project.id, { active: !project.active }).subscribe({
          next: () => {
            this.snackBar.open(
              `Project ${project.active ? 'archived' : 'restored'} successfully`,
              'Close',
              { duration: 3000 }
            );
            this.loadProjects();
          },
          error: () => {
            this.snackBar.open('Failed to update project', 'Close', { duration: 3000 });
          }
        });
      }
    });
  }

  deleteProject(project: AdminProject): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Project',
        message: `Are you sure you want to delete "${project.name}"? This action cannot be undone.`,
        confirmText: 'Delete',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.adminService.deleteProject(project.id).subscribe({
          next: () => {
            this.snackBar.open('Project deleted successfully', 'Close', { duration: 3000 });
            this.loadProjects();
          },
          error: () => {
            this.snackBar.open('Failed to delete project', 'Close', { duration: 3000 });
          }
        });
      }
    });
  }
}
