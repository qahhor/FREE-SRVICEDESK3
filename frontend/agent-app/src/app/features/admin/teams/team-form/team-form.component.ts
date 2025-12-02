import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { AdminService } from '../../services/admin.service';
import { AdminTeam, AdminUser, CreateTeamRequest, UpdateTeamRequest } from '../../services/admin.models';

@Component({
  selector: 'app-team-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './team-form.component.html',
  styleUrls: ['./team-form.component.scss']
})
export class TeamFormComponent implements OnInit {
  teamForm!: FormGroup;
  isEditMode = signal(false);
  loading = signal(false);
  saving = signal(false);
  teamId: string | null = null;
  team: AdminTeam | null = null;

  users = signal<AdminUser[]>([]);

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private adminService: AdminService,
    private snackBar: MatSnackBar
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.loadUsers();

    this.teamId = this.route.snapshot.paramMap.get('id');
    if (this.teamId && this.teamId !== 'new') {
      this.isEditMode.set(true);
      this.loadTeam(this.teamId);
    }
  }

  private initForm(): void {
    this.teamForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: [''],
      managerId: [null],
      active: [true]
    });
  }

  private loadUsers(): void {
    this.adminService.getUsers(0, 100).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.users.set(response.data.content);
        }
      }
    });
  }

  private loadTeam(id: string): void {
    this.loading.set(true);

    this.adminService.getTeam(id).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.team = response.data;
          this.patchForm(response.data);
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

  private patchForm(team: AdminTeam): void {
    this.teamForm.patchValue({
      name: team.name,
      description: team.description || '',
      managerId: team.manager?.id || null,
      active: team.active
    });
  }

  onSubmit(): void {
    if (this.teamForm.invalid) {
      this.teamForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const formValue = this.teamForm.value;

    if (this.isEditMode() && this.teamId) {
      const updateRequest: UpdateTeamRequest = {
        name: formValue.name,
        description: formValue.description || undefined,
        managerId: formValue.managerId || undefined,
        active: formValue.active
      };

      this.adminService.updateTeam(this.teamId, updateRequest).subscribe({
        next: () => {
          this.snackBar.open('Team updated successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/admin/teams']);
        },
        error: (error) => {
          console.error('Error updating team:', error);
          this.snackBar.open('Failed to update team', 'Close', { duration: 3000 });
          this.saving.set(false);
        }
      });
    } else {
      const createRequest: CreateTeamRequest = {
        name: formValue.name,
        description: formValue.description || undefined,
        managerId: formValue.managerId || undefined,
        active: formValue.active
      };

      this.adminService.createTeam(createRequest).subscribe({
        next: () => {
          this.snackBar.open('Team created successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/admin/teams']);
        },
        error: (error) => {
          console.error('Error creating team:', error);
          this.snackBar.open('Failed to create team', 'Close', { duration: 3000 });
          this.saving.set(false);
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/admin/teams']);
  }
}
