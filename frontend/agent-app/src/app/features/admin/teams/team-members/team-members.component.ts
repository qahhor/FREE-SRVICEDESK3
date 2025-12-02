import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatChipsModule } from '@angular/material/chips';

import { AdminService } from '../../services/admin.service';
import { AdminTeam, AdminUser } from '../../services/admin.models';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-team-members',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatListModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatAutocompleteModule,
    MatChipsModule
  ],
  templateUrl: './team-members.component.html',
  styleUrls: ['./team-members.component.scss']
})
export class TeamMembersComponent implements OnInit {
  team = signal<AdminTeam | null>(null);
  loading = signal(true);
  teamId: string | null = null;

  allUsers = signal<AdminUser[]>([]);
  filteredUsers = signal<AdminUser[]>([]);
  searchQuery = '';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private adminService: AdminService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.teamId = this.route.snapshot.paramMap.get('id');
    if (this.teamId) {
      this.loadTeam(this.teamId);
      this.loadAllUsers();
    }
  }

  private loadTeam(id: string): void {
    this.loading.set(true);

    this.adminService.getTeam(id).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.team.set(response.data);
        }
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading team:', error);
        this.snackBar.open('Failed to load team', 'Close', { duration: 3000 });
        this.loading.set(false);
        this.router.navigate(['/admin/teams']);
      }
    });
  }

  private loadAllUsers(): void {
    this.adminService.getUsers(0, 100).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.allUsers.set(response.data.content);
          this.filterAvailableUsers();
        }
      }
    });
  }

  filterAvailableUsers(): void {
    const currentTeam = this.team();
    const memberIds = currentTeam?.members?.map(m => m.id) || [];

    const available = this.allUsers().filter(
      user => !memberIds.includes(user.id) &&
              (user.fullName.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
               user.email.toLowerCase().includes(this.searchQuery.toLowerCase()))
    );

    this.filteredUsers.set(available);
  }

  onSearchChange(): void {
    this.filterAvailableUsers();
  }

  addMember(user: AdminUser): void {
    if (!this.teamId) return;

    this.adminService.addTeamMember(this.teamId, { userId: user.id }).subscribe({
      next: () => {
        this.snackBar.open(`${user.fullName} added to team`, 'Close', { duration: 3000 });
        this.loadTeam(this.teamId!);
        this.searchQuery = '';
        this.filterAvailableUsers();
      },
      error: () => {
        this.snackBar.open('Failed to add member', 'Close', { duration: 3000 });
      }
    });
  }

  removeMember(user: AdminUser): void {
    if (!this.teamId) return;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Remove Member',
        message: `Are you sure you want to remove ${user.fullName} from this team?`,
        confirmText: 'Remove',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.adminService.removeTeamMember(this.teamId!, user.id).subscribe({
          next: () => {
            this.snackBar.open(`${user.fullName} removed from team`, 'Close', { duration: 3000 });
            this.loadTeam(this.teamId!);
            this.filterAvailableUsers();
          },
          error: () => {
            this.snackBar.open('Failed to remove member', 'Close', { duration: 3000 });
          }
        });
      }
    });
  }

  setAsLead(user: AdminUser): void {
    if (!this.teamId) return;

    this.adminService.updateTeam(this.teamId, { managerId: user.id }).subscribe({
      next: () => {
        this.snackBar.open(`${user.fullName} set as team lead`, 'Close', { duration: 3000 });
        this.loadTeam(this.teamId!);
      },
      error: () => {
        this.snackBar.open('Failed to set team lead', 'Close', { duration: 3000 });
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/teams']);
  }

  getInitials(user: AdminUser): string {
    return `${user.firstName?.charAt(0) || ''}${user.lastName?.charAt(0) || ''}`.toUpperCase();
  }
}
