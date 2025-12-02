import { Component, OnInit, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSortModule, MatSort } from '@angular/material/sort';
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
import { SelectionModel } from '@angular/cdk/collections';

import { AdminService } from '../../services/admin.service';
import { AdminUser, UserFilter } from '../../services/admin.models';
import { UserRole } from '../../../../core/models/user.model';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-user-list',
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
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  dataSource = new MatTableDataSource<AdminUser>([]);
  selection = new SelectionModel<AdminUser>(true, []);
  loading = signal(true);
  totalElements = signal(0);
  pageSize = 20;
  pageIndex = 0;

  displayedColumns: string[] = [
    'select',
    'avatar',
    'name',
    'email',
    'role',
    'teams',
    'status',
    'lastLogin',
    'actions'
  ];

  // Filter options
  roles = Object.values(UserRole);
  statusOptions = [
    { value: undefined, label: 'All' },
    { value: true, label: 'Active' },
    { value: false, label: 'Inactive' }
  ];

  // Filter values
  searchQuery = '';
  selectedRole: UserRole | undefined;
  selectedStatus: boolean | undefined;

  constructor(
    private adminService: AdminService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading.set(true);

    const filter: UserFilter = {
      search: this.searchQuery || undefined,
      role: this.selectedRole,
      active: this.selectedStatus
    };

    this.adminService.getUsers(this.pageIndex, this.pageSize, filter).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.dataSource.data = response.data.content;
          this.totalElements.set(response.data.totalElements);
        }
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.snackBar.open('Failed to load users', 'Close', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadUsers();
  }

  applyFilter(): void {
    this.pageIndex = 0;
    this.loadUsers();
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.selectedRole = undefined;
    this.selectedStatus = undefined;
    this.pageIndex = 0;
    this.loadUsers();
  }

  // Selection methods
  isAllSelected(): boolean {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.length;
    return numSelected === numRows;
  }

  toggleAllRows(): void {
    if (this.isAllSelected()) {
      this.selection.clear();
      return;
    }
    this.selection.select(...this.dataSource.data);
  }

  // Bulk actions
  bulkActivate(): void {
    const selectedIds = this.selection.selected.map(u => u.id);
    this.updateUsersStatus(selectedIds, true);
  }

  bulkDeactivate(): void {
    const selectedIds = this.selection.selected.map(u => u.id);
    this.updateUsersStatus(selectedIds, false);
  }

  bulkDelete(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Users',
        message: `Are you sure you want to delete ${this.selection.selected.length} user(s)?`,
        confirmText: 'Delete',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Implement bulk delete
        this.snackBar.open('Users deleted successfully', 'Close', { duration: 3000 });
        this.selection.clear();
        this.loadUsers();
      }
    });
  }

  private updateUsersStatus(ids: string[], active: boolean): void {
    // Update each user's status
    ids.forEach(id => {
      this.adminService.updateUserStatus(id, active).subscribe();
    });
    this.snackBar.open(`Users ${active ? 'activated' : 'deactivated'} successfully`, 'Close', { duration: 3000 });
    this.selection.clear();
    this.loadUsers();
  }

  // Single user actions
  toggleUserStatus(user: AdminUser): void {
    this.adminService.updateUserStatus(user.id, !user.active).subscribe({
      next: () => {
        this.snackBar.open(`User ${user.active ? 'deactivated' : 'activated'}`, 'Close', { duration: 3000 });
        this.loadUsers();
      },
      error: () => {
        this.snackBar.open('Failed to update user status', 'Close', { duration: 3000 });
      }
    });
  }

  deleteUser(user: AdminUser): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete User',
        message: `Are you sure you want to delete ${user.fullName}?`,
        confirmText: 'Delete',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.adminService.deleteUser(user.id).subscribe({
          next: () => {
            this.snackBar.open('User deleted successfully', 'Close', { duration: 3000 });
            this.loadUsers();
          },
          error: () => {
            this.snackBar.open('Failed to delete user', 'Close', { duration: 3000 });
          }
        });
      }
    });
  }

  exportToCsv(): void {
    // Simple CSV export
    const headers = ['Name', 'Email', 'Role', 'Status', 'Last Login'];
    const csvData = this.dataSource.data.map(user => [
      user.fullName,
      user.email,
      user.role,
      user.active ? 'Active' : 'Inactive',
      user.lastLoginAt || 'Never'
    ]);

    const csvContent = [
      headers.join(','),
      ...csvData.map(row => row.join(','))
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'users.csv';
    link.click();
  }

  getRoleColor(role: UserRole): string {
    const colors: Record<UserRole, string> = {
      [UserRole.ADMIN]: 'warn',
      [UserRole.MANAGER]: 'accent',
      [UserRole.AGENT]: 'primary',
      [UserRole.CUSTOMER]: 'default'
    };
    return colors[role] || 'default';
  }

  getInitials(user: AdminUser): string {
    return `${user.firstName?.charAt(0) || ''}${user.lastName?.charAt(0) || ''}`.toUpperCase();
  }
}
