import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';

import { AdminService } from '../../services/admin.service';
import { AdminUser } from '../../services/admin.models';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTabsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatDividerModule
  ],
  templateUrl: './user-detail.component.html',
  styleUrls: ['./user-detail.component.scss']
})
export class UserDetailComponent implements OnInit {
  user = signal<AdminUser | null>(null);
  loading = signal(true);
  userId: string | null = null;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private adminService: AdminService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.userId = this.route.snapshot.paramMap.get('id');
    if (this.userId) {
      this.loadUser(this.userId);
    }
  }

  private loadUser(id: string): void {
    this.loading.set(true);

    this.adminService.getUser(id).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.user.set(response.data);
        }
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading user:', error);
        this.snackBar.open('Failed to load user', 'Close', { duration: 3000 });
        this.loading.set(false);
        this.router.navigate(['/admin/users']);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/users']);
  }

  editUser(): void {
    if (this.userId) {
      this.router.navigate(['/admin/users', this.userId, 'edit']);
    }
  }

  toggleStatus(): void {
    const currentUser = this.user();
    if (!currentUser) return;

    this.adminService.updateUserStatus(currentUser.id, !currentUser.active).subscribe({
      next: () => {
        this.snackBar.open(
          `User ${currentUser.active ? 'deactivated' : 'activated'}`,
          'Close',
          { duration: 3000 }
        );
        this.loadUser(currentUser.id);
      },
      error: () => {
        this.snackBar.open('Failed to update user status', 'Close', { duration: 3000 });
      }
    });
  }

  resetPassword(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Reset Password',
        message: 'Are you sure you want to reset this user\'s password? A new password will be generated.',
        confirmText: 'Reset',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.userId) {
        // Generate a temporary password
        const tempPassword = this.generateTempPassword();
        this.adminService.resetUserPassword(this.userId, tempPassword).subscribe({
          next: () => {
            this.snackBar.open(`Password reset. Temporary password: ${tempPassword}`, 'Copy', {
              duration: 10000
            });
          },
          error: () => {
            this.snackBar.open('Failed to reset password', 'Close', { duration: 3000 });
          }
        });
      }
    });
  }

  deleteUser(): void {
    const currentUser = this.user();
    if (!currentUser) return;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete User',
        message: `Are you sure you want to delete ${currentUser.fullName}? This action cannot be undone.`,
        confirmText: 'Delete',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.adminService.deleteUser(currentUser.id).subscribe({
          next: () => {
            this.snackBar.open('User deleted successfully', 'Close', { duration: 3000 });
            this.router.navigate(['/admin/users']);
          },
          error: () => {
            this.snackBar.open('Failed to delete user', 'Close', { duration: 3000 });
          }
        });
      }
    });
  }

  private generateTempPassword(): string {
    const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%';
    let password = '';
    for (let i = 0; i < 12; i++) {
      password += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return password;
  }

  getInitials(user: AdminUser): string {
    return `${user.firstName?.charAt(0) || ''}${user.lastName?.charAt(0) || ''}`.toUpperCase();
  }
}
